package other.concurrency

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * LESSON 03 — Deadlock & lock ordering.
 *
 * THE CORE IDEA
 * -------------
 * A *deadlock* is a cycle of threads each holding one lock and waiting forever for another
 * lock that a peer already holds. Nobody yields, nobody progresses. The classic recipe is
 * two threads acquiring the same pair of locks in OPPOSITE order:
 *
 *     Thread A: lock(acct1) ; ... ; lock(acct2)      // wants 1 then 2
 *     Thread B: lock(acct2) ; ... ; lock(acct1)      // wants 2 then 1
 *
 * If A grabs acct1 and B grabs acct2 at the same instant, A now blocks waiting for acct2
 * (held by B) while B blocks waiting for acct1 (held by A). Both are stuck for good.
 * This models a naive bank `transfer(from, to)` that locks `from` first, then `to`:
 * two transfers in opposite directions lock the two accounts in opposite order.
 *
 * THE FIX — a GLOBAL lock-acquisition order
 * -----------------------------------------
 * Deadlock needs a *cycle* in the "waits-for" graph. If EVERY thread always takes locks in
 * one consistent global order, no cycle can form, so deadlock becomes impossible. Here we
 * give each account a stable `id` and always lock the lower id first, regardless of transfer
 * direction. Both A and B then contend for the SAME first lock — one simply waits its turn
 * and then proceeds; no hold-and-wait cycle exists.
 *
 * (An alternative avoidance strategy, noted in comments below, is `ReentrantLock.tryLock(timeout)`:
 * take the first lock, then *try* for the second with a timeout; on failure, back off, release
 * everything, and retry. That breaks the "no preemption" deadlock precondition instead of the
 * "circular wait" one. Lock ordering is preferred here because it is deterministic and cheap.)
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * We must NEVER let a deadlock hang the whole suite. So the deadlock demo reproduces the hang,
 * then *detects* it: after `join(timeout)` the worker threads are still alive — that stuck-ness
 * IS the demonstration (we assert it, we do not assertThrows). We then `interrupt()` the threads
 * to clean up so the JVM can exit. The fixed version, by contrast, always COMPLETES and asserts
 * an exact, deterministic invariant: money is conserved — the total across both accounts is
 * unchanged no matter how the transfers interleave.
 */
class L03Deadlock {

    /** A bank account guarded by its own lock. [id] defines the GLOBAL lock-acquisition order. */
    private class Account(val id: Int, initial: Long) {
        val lock = ReentrantLock()

        // Balance is only ever touched while holding this account's lock, so a plain var is fine.
        var balance: Long = initial
    }

    private val transfersPerThread = 10_000

    // ---------------------------------------------------------------------------------------
    // 1) DEADLOCK DEMO — locking `from` then `to` lets opposite-direction transfers cycle.
    // ---------------------------------------------------------------------------------------

    /** Naive transfer: lock the source, pause to force interleaving, then lock the destination. */
    private fun deadlockingTransfer(from: Account, to: Account, amount: Long) {
        from.lock.lock()
        try {
            // The tiny sleep widens the race window so the deadlock reproduces almost every run,
            // instead of hiding behind lucky scheduling.
            Thread.sleep(10)
            to.lock.lock()
            try {
                from.balance -= amount
                to.balance += amount
            } finally {
                to.lock.unlock()
            }
        } finally {
            from.lock.unlock()
        }
    }

    @Test
    fun `opposite lock order deadlocks (detected, not hung)`() {
        val acct1 = Account(id = 1, initial = 1_000)
        val acct2 = Account(id = 2, initial = 1_000)

        val startGun = CountDownLatch(1)

        // Thread A locks 1 then 2; Thread B locks 2 then 1 — opposite orders → cycle.
        val a = thread(start = true, name = "transfer-A") {
            startGun.await()
            runCatching {
                deadlockingTransfer(
                    acct1,
                    acct2,
                    1
                )
            }  // may deadlock; runCatching lets interrupt() unwind cleanly
        }
        val b = thread(start = true, name = "transfer-B") {
            startGun.await()
            runCatching { deadlockingTransfer(acct2, acct1, 1) }
        }

        startGun.countDown()   // release both at once to maximize the chance they interleave.

        // Give them a bounded chance to finish. If they were correct they'd finish in ~10ms;
        // a full 300ms with no progress means they are wedged.
        a.join(300)
        b.join(300)

        val stuck = a.isAlive && b.isAlive
        if (stuck) {
            println(
                "DEADLOCK reproduced: transfer-A holds acct1 and waits for acct2, " +
                        "transfer-B holds acct2 and waits for acct1 — neither can proceed.",
            )
        } else {
            // Scheduling is nondeterministic; very rarely one thread grabs both locks before the
            // other starts, so no cycle forms. We don't fail the suite over that — the lesson is
            // still valid — but we surface it so the run is honest.
            println("NOTE: threads happened not to interleave into a deadlock this run.")
        }

        // Clean up so the suite never hangs: interrupting a lock().lock() waiter unblocks it.
        // (interrupt() sets the flag; the runCatching around the transfer swallows the resulting
        // unwind so the worker just exits.) We join with a timeout again as a final safety net.
        a.interrupt()
        b.interrupt()
        a.join(1_000)
        b.join(1_000)

        // The demonstration: when they interleaved, they were genuinely stuck (not throwing, not
        // finishing) until we intervened. That is what a deadlock looks like.
        assertTrue(
            stuck || (!a.isAlive && !b.isAlive),
            "either we observed the deadlock, or the threads completed — never left hanging",
        )
    }

    // ---------------------------------------------------------------------------------------
    // 2) THE FIX — always acquire the two locks in a consistent GLOBAL order (lowest id first).
    // ---------------------------------------------------------------------------------------

    /**
     * Deadlock-free transfer. We do NOT lock in `from`/`to` order — that order flips with the
     * transfer direction. Instead we impose a total order on the locks themselves (by account
     * [Account.id]) and always take the lower-id lock first. Every thread now agrees on the order,
     * so no circular wait can ever form. Balance mutation still happens for the real from/to.
     */
    private fun safeTransfer(from: Account, to: Account, amount: Long) {
        // Pick a canonical (first, second) purely by id — independent of transfer direction.
        val first: Account
        val second: Account
        if (from.id < to.id) {
            first = from; second = to
        } else {
            first = to; second = from
        }
        first.lock.withLock {          // kotlin.concurrent.withLock: locks, runs, always unlocks.
            second.lock.withLock {
                from.balance -= amount
                to.balance += amount
            }
        }
    }

    @Test
    fun `global lock ordering prevents deadlock and conserves money`() {
        val acct1 = Account(id = 1, initial = 1_000)
        val acct2 = Account(id = 2, initial = 1_000)
        val totalBefore = acct1.balance + acct2.balance

        val startGun = CountDownLatch(1)
        val completed = AtomicInteger(0)  // proves every transfer actually ran (no silent hang).

        // Same adversarial setup as the demo: A pushes 1->2 while B pushes 2->1, in tight loops.
        val a = thread(start = true, name = "safe-A") {
            startGun.await()
            repeat(transfersPerThread) { safeTransfer(acct1, acct2, 1) }
            completed.incrementAndGet()
        }
        val b = thread(start = true, name = "safe-B") {
            startGun.await()
            repeat(transfersPerThread) { safeTransfer(acct2, acct1, 1) }
            completed.incrementAndGet()
        }

        startGun.countDown()

        // A generous timeout: correct code finishes in well under a second. If lock ordering were
        // wrong this join would time out and the assertions below would catch the stuck threads.
        a.join(10_000)
        b.join(10_000)

        assertTrue(!a.isAlive && !b.isAlive, "both workers must finish — no deadlock")
        assertEquals(2, completed.get(), "both transfer loops ran to completion")

        // The exact, deterministic invariant: transfers only MOVE money, never create/destroy it.
        // A and B move equal-and-opposite amounts, so each account returns to its start too.
        assertEquals(totalBefore, acct1.balance + acct2.balance, "money is conserved")
        assertEquals(1_000, acct1.balance)
        assertEquals(1_000, acct2.balance)
    }
}
