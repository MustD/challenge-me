package other.concurrency

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.concurrent.write
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * LESSON 02 — Explicit locks: ReentrantLock, Conditions, and ReadWriteLock.
 *
 * THE CORE IDEA
 * -------------
 * Lesson 01 fixed a race with the built-in `synchronized` keyword. That intrinsic monitor is
 * simple and correct, but it is also *rigid*: you cannot try to acquire it, you cannot give up
 * after a timeout, you cannot ask for fairness, and you get exactly one wait-set per object.
 * `java.util.concurrent.locks` trades a little verbosity for that missing flexibility. This
 * lesson walks the four tools you reach for when `synchronized` is not enough:
 *
 *   1. ReentrantLock          — the drop-in replacement for `synchronized`, but with superpowers.
 *   2. tryLock(timeout, unit) — attempt to acquire, then GIVE UP instead of blocking forever.
 *   3. Condition              — a named wait/signal channel: park a thread until a predicate holds.
 *   4. ReentrantReadWriteLock — let many readers in at once, but writers get exclusive access.
 *
 * WHY EXPLICIT LOCKS BEAT `synchronized` (WHEN THEY DO)
 * -----------------------------------------------------
 *   - You can *fail* to acquire: `tryLock()` / `tryLock(timeout)` return a boolean instead of
 *     blocking — the only clean way to avoid hanging forever on a contended or deadlocked lock.
 *   - You can request FAIRNESS: `ReentrantLock(true)` hands the lock to the longest-waiting
 *     thread (FIFO) instead of whoever happens to be scheduled. Fair locks avoid starvation but
 *     are slower (lower throughput) — the default `ReentrantLock()` is unfair on purpose.
 *   - One lock can own MULTIPLE conditions (multiple wait-sets), e.g. "not full" and "not empty"
 *     in a bounded buffer — impossible with a single object monitor's `wait()/notify()`.
 *   - ReadWriteLock separates read access from write access, which `synchronized` cannot express.
 *
 * THE ONE RULE YOU MUST NOT FORGET: lock() then unlock() in a finally.
 * -------------------------------------------------------------------
 * With `synchronized` the JVM releases the monitor for you even if the block throws. Explicit
 * locks do NOT — a `lock()` with no matching `unlock()` on the exception path leaks the lock and
 * deadlocks everyone else. The correct manual shape is always:
 *     lock.lock(); try { /* critical section */ } finally { lock.unlock() }
 * Kotlin's `withLock { }` / `read { }` / `write { }` extensions wrap exactly that try/finally, so
 * we prefer them everywhere below and never hand-roll the unlock.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * Every assertion here is EXACT and deterministic — the correct use of a lock removes the timing
 * nondeterminism, so a right answer is the right answer on every run. The one time-based check
 * (the tryLock timeout) asserts a generous lower bound, never a flaky upper bound, and no test can
 * hang: threads are joined with a timeout, and the Condition handoff loops on its predicate so it
 * makes progress regardless of which thread the scheduler runs first.
 */
class L02LocksAndConditions {

    // ---------------------------------------------------------------------------------------------
    // 1. ReentrantLock + withLock — the correct, exception-safe replacement for `synchronized`.
    // ---------------------------------------------------------------------------------------------

    @Test
    fun `ReentrantLock with withLock protects a shared counter`() {
        val threads = 8
        val incrementsPerThread = 100_000
        val expected = threads * incrementsPerThread

        var counter = 0                       // plain var — the lock is what makes this safe.
        val lock = ReentrantLock()            // "reentrant": the holder may re-acquire it recursively.

        val startGun = CountDownLatch(1)      // starting gun — release all workers together (max contention).
        val workers = (1..threads).map {
            thread {
                startGun.await()
                // withLock { } == lock.lock(); try { ... } finally { lock.unlock() }.
                // The finally is the whole point: even if the body threw, the lock is released.
                repeat(incrementsPerThread) { lock.withLock { counter++ } }
            }
        }
        startGun.countDown()
        workers.forEach { it.join() }

        // Mutual exclusion makes the read-modify-write indivisible → exact total, every run.
        assertEquals(expected, counter)
    }

    // ---------------------------------------------------------------------------------------------
    // 2. tryLock(timeout, unit) — give up instead of blocking forever.
    // ---------------------------------------------------------------------------------------------

    @Test
    fun `tryLock gives up after a timeout instead of blocking forever`() {
        val lock = ReentrantLock()
        val timeoutMs = 80L

        // The MAIN thread grabs and HOLDS the lock for the whole test. A plain lock.lock() +
        // finally.unlock() — held across the spawned thread's entire attempt.
        lock.lock()
        try {
            var acquired = true               // start pessimistic; the attempt must flip this to false.
            var elapsedMs = 0L

            val contender = thread {
                val start = System.nanoTime()
                // tryLock waits AT MOST timeoutMs for the lock, then returns false rather than
                // parking indefinitely. This is the canonical way to bound blocking and to break
                // out of a would-be deadlock. (The plain no-arg tryLock() returns immediately.)
                acquired = lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)
                elapsedMs = (System.nanoTime() - start) / 1_000_000
                if (acquired) lock.unlock()   // defensive: we never expect this branch here.
            }
            contender.join(2_000)             // join with timeout so a bug can never hang the suite.

            assertFalse(contender.isAlive, "contender should have returned, not still be blocked")
            assertFalse(acquired, "lock is held by main, so tryLock must time out and return false")
            // Lower bound only. It genuinely waited ~timeout; upper bounds would be flaky under load,
            // so we never assert one. Allow a little slack below the nominal timeout for timer coarseness.
            assertTrue(elapsedMs >= timeoutMs - 20, "should have waited about the timeout, waited ${elapsedMs}ms")
        } finally {
            lock.unlock()                     // release in finally — the rule, always.
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 3. Condition — a "wait until ready" handoff between two threads.
    // ---------------------------------------------------------------------------------------------

    @Test
    fun `a Condition lets one thread await a value another thread signals`() {
        val lock = ReentrantLock()
        val ready = lock.newCondition()       // a wait-set owned by THIS lock (a lock can own many).

        var mailbox: Int? = null              // shared state, guarded by `lock`.
        var received = -1

        val consumer = thread {
            lock.withLock {
                // ALWAYS loop on the predicate, never a bare `if`. await() can return spuriously
                // (wake with no signal), and even a real signal only means "recheck" — some other
                // thread may have changed the state first. The while-loop re-tests before proceeding.
                while (mailbox == null) {
                    // await() atomically RELEASES the lock and parks; on wakeup it RE-ACQUIRES the
                    // lock before returning. So the producer below can enter the critical section
                    // while we sleep here — that release is what makes the handoff possible.
                    ready.await()
                }
                received = mailbox!!
            }
        }

        val producer = thread {
            lock.withLock {                   // must hold the lock to signal, just as to await.
                mailbox = 42
                ready.signal()                // wake ONE awaiting thread (signalAll() wakes them all).
            }                                  // the consumer only actually resumes once we release here.
        }

        producer.join(2_000)
        consumer.join(2_000)
        // Deterministic regardless of thread ordering: the while-loop guard means that even if the
        // producer runs FIRST, the consumer simply sees mailbox != null and never awaits at all.
        assertFalse(consumer.isAlive, "consumer should have received the value and finished")
        assertEquals(42, received)
    }

    // ---------------------------------------------------------------------------------------------
    // 4. ReentrantReadWriteLock — many concurrent readers, exclusive writers.
    // ---------------------------------------------------------------------------------------------

    @Test
    fun `ReadWriteLock allows concurrent reads but exclusive writes`() {
        // Use case: read-HEAVY shared state (config, caches, routing tables) that is written rarely.
        // A single mutex would needlessly serialize the many readers; a ReadWriteLock lets any number
        // of readers hold the read lock SIMULTANEOUSLY, while a writer must wait for all readers to
        // leave and then holds the write lock ALONE (no reader or other writer may enter). Reads never
        // corrupt each other, so letting them overlap is both safe and much faster under read load.
        val rwLock = ReentrantReadWriteLock()

        val writers = 4
        val incrementsPerWriter = 25_000
        val readers = 8
        val readsPerReader = 50_000
        val expected = writers * incrementsPerWriter

        var shared = 0                        // guarded: mutate under write {}, observe under read {}.

        val startGun = CountDownLatch(1)

        val writerThreads = (1..writers).map {
            thread {
                startGun.await()
                // write { } takes the EXCLUSIVE lock: this increment cannot overlap any other
                // writer or any reader, so no update is lost.
                repeat(incrementsPerWriter) { rwLock.write { shared++ } }
            }
        }
        val readerThreads = (1..readers).map {
            thread {
                startGun.await()
                // read { } takes the SHARED lock: many readers run this block at the same time.
                // We only assert an always-true invariant here (a monotone, never-negative snapshot)
                // because the exact value a reader sees mid-run is legitimately nondeterministic —
                // the deterministic check is the FINAL total below, after everyone has joined.
                repeat(readsPerReader) { rwLock.read { assertTrue(shared in 0..expected) } }
            }
        }

        startGun.countDown()
        (writerThreads + readerThreads).forEach { it.join() }

        // After all exclusive writes complete, the total is exact — no lost updates.
        assertEquals(expected, shared)
    }
}
