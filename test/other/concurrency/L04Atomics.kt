package other.concurrency

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.LongAdder
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * LESSON 04 — Atomics & Compare-And-Swap (CAS): lock-free concurrency.
 *
 * THE CORE IDEA
 * -------------
 * A lock makes threads take turns. CAS lets them all charge ahead and *retry on collision*.
 * The one primitive underneath every lock-free algorithm is compare-and-set:
 *
 *     compareAndSet(expected, new)  →  "IF the value is still `expected`, atomically swap in
 *                                       `new` and return true; OTHERWISE change nothing and
 *                                       return false."
 *
 * That whole compare-and-swap is ONE indivisible hardware instruction (x86 `LOCK CMPXCHG`,
 * ARM LL/SC). No other thread can slip between the compare and the swap. So the pattern for
 * any atomic update — even ones with no dedicated method like `incrementAndGet` — is a loop:
 *
 *     do {
 *         val cur  = ref.get()          // 1. READ the current value
 *         val next = f(cur)             // 2. COMPUTE the new value from it
 *     } while (!ref.compareAndSet(cur, next))   // 3. SWAP only if nobody changed it meanwhile;
 *                                               //    else another thread won the race → RETRY.
 *
 * WHY THE LOOP? Between your READ and your SWAP, another thread may have updated the value.
 * If so, `cur` is stale, `compareAndSet` sees a mismatch, returns false, and you loop with a
 * fresh read. It is optimistic concurrency: assume no conflict, detect it, redo the small bit.
 *
 * THE TOOLS IN THIS FILE
 * ----------------------
 *   - AtomicInteger + explicit CAS loop : "multiply-by-2 up to a cap" — impossible as one
 *                                          incrementAndGet, so we hand-roll read-compute-CAS.
 *   - updateAndGet { } / accumulateAndGet : the idiomatic shorthand that hides the loop.
 *   - AtomicReference<Node>              : a genuine LOCK-FREE STACK (Treiber stack) — push/pop
 *                                          by CAS-ing an immutable head pointer.
 *   - LongAdder vs AtomicLong            : same correctness, very different scaling under contention.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * Every implementation here is CORRECT, so every assert is EXACT and holds on every run.
 * A [CountDownLatch] "starting gun" releases all 8 threads at once to maximize contention and
 * force the CAS retry loops to actually collide — then we `join` and assert the precise result.
 * If any assert were only "approximately right", the lesson would be broken, not the code.
 */
class L04Atomics {

    private val threads = 8
    private val opsPerThread = 100_000

    /**
     * Fan out [work] across [threads] threads, all released simultaneously by a starting gun so
     * they genuinely contend. `work` receives its thread index (0-based) in case it needs it.
     */
    private fun runConcurrently(work: (threadIndex: Int) -> Unit) {
        val startGun = CountDownLatch(1)
        val workers = (0 until threads).map { idx ->
            thread {
                startGun.await()                 // all workers block here...
                work(idx)
            }
        }
        startGun.countDown()                     // ...and are released together.
        workers.forEach { it.join() }            // wait for all before reading the result.
    }

    // ─────────────────────────────────────────────────────────────────────────────────────────
    // 1. AtomicInteger with an EXPLICIT CAS retry loop.
    //    Some updates have no single-instruction helper. "Double the value, but never past a cap"
    //    depends on the current value in a way incrementAndGet can't express — so we read, compute,
    //    and compareAndSet, retrying whenever another thread beat us to the swap.
    // ─────────────────────────────────────────────────────────────────────────────────────────

    @Test
    fun `explicit CAS loop - multiply by 2 up to a cap`() {
        val cap = 1_000_000
        val value = AtomicInteger(1)

        // Many threads all try to double the value. Doubling is deterministic and idempotent
        // once the cap is hit, so no matter how the retries interleave the final value is exact.
        runConcurrently {
            repeat(opsPerThread) {
                while (true) {
                    val cur = value.get()                    // 1. READ
                    if (cur >= cap) break                    //    already capped — nothing to do
                    val next = minOf(cur * 2, cap)           // 2. COMPUTE (clamp to the cap)
                    if (value.compareAndSet(cur, next))      // 3. SWAP iff unchanged since READ
                        break                                //    won the race → done
                    // lost the race: `cur` is stale, loop and re-read. THIS is why CAS loops.
                }
            }
        }

        // Starting at 1 and repeatedly doubling: 1→2→4→…→1_048_576, but clamped at 1_000_000.
        assertEquals(cap, value.get())
    }

    @Test
    fun `updateAndGet is the idiomatic shorthand for the same loop`() {
        val cap = 1_000_000
        val value = AtomicInteger(1)

        // updateAndGet runs the read-compute-compareAndSet-retry loop FOR you. The lambda must be
        // pure and side-effect-free, because it can be invoked MULTIPLE times (once per retry).
        runConcurrently {
            repeat(opsPerThread) {
                value.updateAndGet { cur -> if (cur >= cap) cur else minOf(cur * 2, cap) }
            }
        }

        assertEquals(cap, value.get())
    }

    @Test
    fun `accumulateAndGet - atomic running max`() {
        val max = AtomicInteger(Int.MIN_VALUE)

        // "Keep the maximum value ever seen" is another update with no dedicated method.
        // accumulateAndGet(x) { acc, given -> ... } folds `given` into the accumulator atomically,
        // again via an internal CAS retry loop. Each thread offers values 0..opsPerThread-1.
        runConcurrently {
            repeat(opsPerThread) { i -> max.accumulateAndGet(i) { acc, given -> maxOf(acc, given) } }
        }

        assertEquals(opsPerThread - 1, max.get())
    }

    // ─────────────────────────────────────────────────────────────────────────────────────────
    // 2. A LOCK-FREE STACK (Treiber stack) via AtomicReference<Node>.
    //    The whole stack is ONE atomic pointer to the head. Nodes are IMMUTABLE (value + next),
    //    so publishing a node = CAS-ing the head. No locks anywhere.
    // ─────────────────────────────────────────────────────────────────────────────────────────

    /** Immutable cons cell. Immutability is what makes the CAS safe: a node never changes once built. */
    private class Node<T>(val value: T, val next: Node<T>?)

    private class LockFreeStack<T> {
        private val head = AtomicReference<Node<T>?>(null)

        fun push(value: T) {
            while (true) {
                val oldHead = head.get()                     // 1. READ current head
                val newHead = Node(value, oldHead)           // 2. COMPUTE: new node points at it
                if (head.compareAndSet(oldHead, newHead))    // 3. SWAP iff head still == oldHead
                    return                                   //    won → linked in
                // another push/pop moved head → retry with a fresh read.
            }
        }

        /** Returns the popped value, or null if the stack was empty. */
        fun pop(): T? {
            while (true) {
                val oldHead = head.get() ?: return null      // 1. READ (empty? done)
                val newHead = oldHead.next                   // 2. COMPUTE: drop the top node
                if (head.compareAndSet(oldHead, newHead))    // 3. SWAP iff head unchanged
                    return oldHead.value
                // lost the race → retry.
            }
        }
        // ─ THE ABA PROBLEM ─────────────────────────────────────────────────────────────────────
        // CAS checks the head is the SAME reference, not that it never MOVED. A thread could read
        // head=A, stall, and meanwhile others pop A, pop B, then push A back. Head is A again, so
        // this thread's compareAndSet(A, ...) succeeds — even though the stack changed underneath
        // it, possibly corrupting the structure. That's "ABA". It bites CAS algorithms that reuse
        // nodes. Here we allocate a fresh immutable Node per push (the GC keeps recycled addresses
        // from reappearing while still referenced), so plain reference-CAS is safe. The general
        // fix is a versioned pointer — java.util.concurrent.atomic.AtomicStampedReference — which
        // CAS-es (reference, stamp) together so an A→B→A cycle bumps the stamp and is detected.
    }

    @Test
    fun `lock-free stack preserves every pushed value under contention`() {
        val stack = LockFreeStack<Int>()
        val perThread = 50_000

        // Phase 1: 8 threads each push a disjoint block of values, all racing on the same head.
        // Thread t pushes t*perThread .. t*perThread + perThread - 1.
        runConcurrently { t ->
            val base = t * perThread
            repeat(perThread) { i -> stack.push(base + i) }
        }

        // Phase 2: 8 threads pop everything back out (also contending on the head). Each thread
        // records what it popped into its OWN list — no shared mutable collection, no extra races.
        val total = threads * perThread
        val drained = AtomicInteger(0)
        val perThreadResults = arrayOfNulls<MutableList<Int>>(threads)
        runConcurrently { t ->
            val local = ArrayList<Int>()
            while (drained.getAndIncrement() < total) {      // hand out exactly `total` pop tickets
                val v = stack.pop()!!                        // guaranteed non-null: we own a ticket
                local += v
            }
            perThreadResults[t] = local
        }

        // The stack is now empty, and the MULTISET of popped values equals exactly what was pushed:
        // 0 .. total-1 each once. We verify via exact size + sum + set identity (order is irrelevant
        // for a concurrent stack, so we assert the collection, not a sequence).
        assertNull(stack.pop(), "stack must be fully drained")
        val popped = perThreadResults.filterNotNull().flatten()
        assertEquals(total, popped.size, "every push must yield exactly one pop")
        assertEquals((0 until total).sum().toLong(), popped.sumOf { it.toLong() })
        assertEquals((0 until total).toSet(), popped.toSet(), "no value lost or duplicated")
    }

    // ─────────────────────────────────────────────────────────────────────────────────────────
    // 3. LongAdder vs AtomicLong under HIGH CONTENTION.
    //    Both give the exact right total. The difference is scaling.
    //
    //    AtomicLong keeps ONE 64-bit value. Under heavy contention every thread CAS-es the SAME
    //    memory word, so that one cache line ping-pongs between CPU cores and CAS failures/retries
    //    pile up — the hot line becomes the bottleneck.
    //
    //    LongAdder SPREADS the count across an array of per-thread Cells (striping). Threads mostly
    //    hit DIFFERENT cells → different cache lines → little contention. `sum()` adds the cells up
    //    at the end. The trade-off: sum() is only a snapshot and there is no atomic read-modify of
    //    the whole total. So: LongAdder for write-heavy counters/metrics where you read rarely;
    //    AtomicLong when you need a single always-consistent value or atomic compound updates.
    // ─────────────────────────────────────────────────────────────────────────────────────────

    @Test
    fun `AtomicLong is exact under contention`() {
        val counter = AtomicLong(0)

        runConcurrently { repeat(opsPerThread) { counter.incrementAndGet() } }

        assertEquals((threads * opsPerThread).toLong(), counter.get())
    }

    @Test
    fun `LongAdder is exact too - and scales better under contention`() {
        val adder = LongAdder()

        // increment() bumps this thread's own cell (usually) — cheap, contention-light.
        runConcurrently { repeat(opsPerThread) { adder.increment() } }

        // sum() folds all the cells into the final total. Correct here because all writers have
        // joined (a quiescent snapshot); mid-flight sum() would be an estimate, not a fixed point.
        assertEquals((threads * opsPerThread).toLong(), adder.sum())
    }
}
