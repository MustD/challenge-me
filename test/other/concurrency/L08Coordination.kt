package other.concurrency

import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * LESSON 08 — Coordinating threads: CountDownLatch, CyclicBarrier, Semaphore.
 *
 * THE CORE IDEA
 * -------------
 * Lessons 01–07 were about making a *shared value* safe (locks, atomics, visibility).
 * This lesson is about making *threads wait for each other* at the right moments. Three
 * tools from `java.util.concurrent` cover the vast majority of real coordination needs:
 *
 *   - CountDownLatch — a ONE-SHOT gate that opens when a counter hits zero. Threads
 *     `await()` (block) until enough `countDown()` calls have happened. Once it reaches
 *     zero it stays open forever — it CANNOT be reset. Two classic shapes:
 *         (a) START GUN     : latch(1); every worker awaits; main countDown() once to
 *                             release them all together (this is the `runConcurrently`
 *                             pattern from lesson 01).
 *         (b) COMPLETION GATE: latch(n); each worker countDown() as it finishes; main
 *                             await()s until the last one is done. (This is essentially
 *                             a manual `join()` over an aggregate.)
 *
 *   - CyclicBarrier — a REUSABLE rendezvous for a FIXED party of N threads. Each thread
 *     calls `await()` and blocks; when the Nth arrives, ALL are released simultaneously,
 *     and the barrier automatically RESETS for the next round — hence "cyclic". Perfect
 *     for phased/round-based algorithms where no thread may start phase K+1 until every
 *     thread has finished phase K. An optional *barrier action* runs exactly once, on the
 *     last-arriving thread, in the gap between "all arrived" and "all released" — a handy
 *     place to aggregate the round. Contrast with CountDownLatch: barrier is reusable and
 *     counts *arrivals of the same parties*; latch is one-shot and counts *events*.
 *
 *   - Semaphore — a counter of PERMITS that caps how many threads may be inside a section
 *     at once. `acquire()` takes a permit (blocking if none are free); `release()` returns
 *     one. A semaphore of 1 behaves like a lock; a semaphore of K throttles to K concurrent
 *     occupants. Real uses: connection pools, rate limiting, bounding parallelism. Use
 *     `tryAcquire(timeout, unit)` when you'd rather fail/back off than block forever.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * Every test asserts an EXACT, deterministic result and never hangs (latches/barriers are
 * always driven to completion and threads are always joined). The invariants are chosen so
 * a correct program passes on *every* run regardless of scheduling:
 *   - latch tests assert an exact aggregate total,
 *   - the barrier test asserts a per-round invariant that only holds if everyone rendezvoused,
 *   - the semaphore test asserts an UPPER BOUND on observed concurrency — the high-water mark
 *     of simultaneous occupants must never exceed the permit count. That bound is guaranteed
 *     by the semaphore no matter how threads interleave, so it is safe to assert deterministically.
 */
class L08Coordination {

    // ---------------------------------------------------------------------------------------
    // 1. CountDownLatch
    // ---------------------------------------------------------------------------------------

    @Test
    fun `CountDownLatch as a start gun releases all workers together`() {
        val workers = 8
        val perWorker = 50_000
        val total = AtomicInteger(0)

        // latch(1): a single countDown() flips the gate open for everyone at once.
        val startGun = CountDownLatch(1)

        val threads = (1..workers).map {
            thread {
                startGun.await()                       // all workers block here...
                repeat(perWorker) { total.incrementAndGet() }
            }
        }
        startGun.countDown()                           // ...and are released together (one shot).
        threads.forEach { it.join() }                  // join so the aggregate is fully settled.

        // Exact aggregate: every worker's contribution is accounted for.
        assertEquals(workers * perWorker, total.get())
    }

    @Test
    fun `CountDownLatch as a completion gate waits for every worker to finish`() {
        val workers = 6
        val total = AtomicInteger(0)

        // latch(n): each worker counts down once when done; main awaits until it hits zero.
        val done = CountDownLatch(workers)

        (1..workers).forEach { id ->
            thread {
                try {
                    total.addAndGet(id)                // this worker's contribution: 1 + 2 + ... + 6
                } finally {
                    done.countDown()                   // signal completion even if work threw.
                }
            }
        }

        done.await()                                   // block until all `workers` have finished.

        // Sum 1..6 = 21. Because we awaited the gate, the read is guaranteed complete.
        assertEquals((1..workers).sum(), total.get())
    }

    @Test
    fun `CountDownLatch is one-shot and cannot be reset`() {
        val latch = CountDownLatch(1)

        latch.countDown()
        assertEquals(0, latch.count)                   // gate is open...

        latch.countDown()                              // ...further counts down are simply ignored;
        assertEquals(0, latch.count)                   // the count never goes negative and never resets.

        // await() on an already-open latch returns immediately — proof the gate stays open.
        latch.await()
    }

    // ---------------------------------------------------------------------------------------
    // 2. CyclicBarrier
    // ---------------------------------------------------------------------------------------

    @Test
    fun `CyclicBarrier synchronizes N threads across repeated rounds`() {
        val parties = 4
        val rounds = 5

        // arrivalsThisRound is reset to 0 by the barrier action once per round, on the last
        // thread to arrive — the safe window where every thread is parked at the barrier.
        val arrivalsThisRound = AtomicInteger(0)
        val roundsCompleted = AtomicInteger(0)

        // Barrier action: runs ONCE per round, on the last-arriving thread, before any is released.
        val barrier = CyclicBarrier(parties) {
            // Invariant check: exactly `parties` threads must have arrived to trip the barrier.
            assertEquals(parties, arrivalsThisRound.get(), "every party must reach the barrier")
            arrivalsThisRound.set(0)                    // reset for the next (cyclic) round.
            roundsCompleted.incrementAndGet()
        }

        val threads = (1..parties).map {
            thread {
                repeat(rounds) {
                    arrivalsThisRound.incrementAndGet() // announce arrival for this round...
                    barrier.await()                     // ...then block until ALL parties arrive.
                    // Past this line every thread has rendezvoused; next loop iteration is a new round.
                }
            }
        }
        threads.forEach { it.join() }

        // The barrier is reusable: it tripped once per round for all rounds.
        assertEquals(rounds, roundsCompleted.get())
        assertEquals(0, arrivalsThisRound.get())        // fully drained after the final round.
    }

    // ---------------------------------------------------------------------------------------
    // 3. Semaphore
    // ---------------------------------------------------------------------------------------

    @Test
    fun `Semaphore caps concurrency at the permit count`() {
        val permits = 3
        val workers = 20

        val semaphore = Semaphore(permits)
        val currentlyInside = AtomicInteger(0)          // live count of threads in the section.
        val highWaterMark = AtomicInteger(0)            // max concurrency ever observed.

        // Start gun so all workers pile into acquire() together — maximizing contention and
        // making it easy to breach the cap if the semaphore didn't work.
        val startGun = CountDownLatch(1)

        val threads = (1..workers).map {
            thread {
                startGun.await()
                semaphore.acquire()                     // blocks until a permit is free.
                try {
                    val now = currentlyInside.incrementAndGet()
                    // Record the running maximum (compare-and-set loop, lock-free).
                    highWaterMark.updateAndGet { prev -> maxOf(prev, now) }
                    // A little busy work while "inside" the guarded section.
                    var acc = 0
                    repeat(1_000) { acc += it }
                    currentlyInside.decrementAndGet()
                } finally {
                    semaphore.release()                 // always return the permit.
                }
            }
        }
        startGun.countDown()
        threads.forEach { it.join() }

        // DETERMINISTIC UPPER BOUND: the semaphore guarantees we never exceeded `permits`,
        // no matter how threads were scheduled.
        assertTrue(
            highWaterMark.get() <= permits,
            "observed max concurrency ${highWaterMark.get()} must not exceed $permits permits",
        )
        // And every thread must have exited cleanly (all permits returned, section empty).
        assertEquals(0, currentlyInside.get())
        assertEquals(permits, semaphore.availablePermits())
    }

    @Test
    fun `Semaphore tryAcquire with timeout fails fast when no permit is free`() {
        val semaphore = Semaphore(1)

        semaphore.acquire()                             // take the only permit.
        try {
            // No permit left: tryAcquire waits up to the timeout, then returns false instead
            // of blocking forever — the basis of back-off / rate-limiting strategies.
            val got = semaphore.tryAcquire(50, TimeUnit.MILLISECONDS)
            assertTrue(!got, "should not acquire a permit that is already held")
        } finally {
            semaphore.release()
        }

        // Once released, the permit is immediately available again.
        assertTrue(semaphore.tryAcquire(50, TimeUnit.MILLISECONDS))
        semaphore.release()
    }
}
