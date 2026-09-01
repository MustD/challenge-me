package other.concurrency

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * LESSON K03 — Cooperative cancellation & timeouts (coroutines).
 *
 * THE CORE IDEA
 * -------------
 * Cancelling a coroutine does NOT forcibly kill it. `job.cancel()` merely flips the job's
 * state to "cancelling" and asks the coroutine to stop. The coroutine actually stops only
 * when it reaches a *cancellation checkpoint* and notices the request. Cancellation is
 * therefore COOPERATIVE — a coroutine that never checks will run to the end regardless.
 *
 * WHERE THE CHECKPOINTS ARE
 * -------------------------
 *   - Every suspending function in `kotlinx.coroutines` (`delay`, `yield`, `withContext`, …)
 *     checks for cancellation on resume and throws `CancellationException` if cancelled.
 *   - A tight CPU loop with NO suspension point has no checkpoint, so it ignores cancellation.
 *     You add a checkpoint yourself with `ensureActive()`, an `if (!isActive) return` guard,
 *     or `yield()`.
 *
 * THE TOOLS IN THIS FILE
 * ----------------------
 *   - `job.cancelAndJoin()`         : request cancellation, then wait until the coroutine has
 *                                      actually unwound (including its `finally` blocks).
 *   - `ensureActive()` / `isActive` : manual checkpoints for CPU-bound loops.
 *   - `withTimeout(ms) { }`         : cancels the block after a budget and THROWS
 *                                      `TimeoutCancellationException`.
 *   - `withTimeoutOrNull(ms) { }`   : same budget, but returns `null` instead of throwing —
 *                                      the idiomatic "did it finish in time?" pattern.
 *   - `withContext(NonCancellable)` : lets a suspend call in a `finally` block still run even
 *                                      though the coroutine is already cancelled.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * Every test runs inside `runBlocking { }` so the whole body is deterministic and the test
 * thread waits for all coroutines to settle. Timeouts and small `delay`s keep everything
 * fast and hang-proof: a coroutine either stops at a checkpoint or is bounded by a finite
 * loop, so no test can run forever. Counters use `Atomic*` because the work may run on a
 * background dispatcher thread while the test thread observes it.
 */
class K03Cancellation {

    /**
     * 1) A coroutine that periodically suspends (`delay`) is trivially cancellable: `delay`
     *    is a checkpoint, so `cancelAndJoin()` stops it mid-way. We prove it stopped EARLY by
     *    showing it never completed all of its intended iterations.
     */
    @Test
    fun `cancelAndJoin stops a suspending coroutine early`() = runBlocking {
        val iterations = AtomicInteger(0)

        val job = launch {
            repeat(1_000) {
                delay(10.milliseconds)                      // suspension point == cancellation checkpoint
                iterations.incrementAndGet()   // only counts iterations that fully ran
            }
        }

        delay(35.milliseconds)                              // let ~3 iterations happen...
        job.cancelAndJoin()                    // ...then request stop and wait for the unwind.

        // It was cancelled long before the 1000th iteration. The exact count depends on timing,
        // so we assert the invariant that matters: it did NOT run to completion.
        println("suspending coroutine ran ${iterations.get()} of 1000 iterations before cancel")
        assertTrue(iterations.get() < 1_000, "delay() checkpoint should have stopped it early")
    }

    /**
     * 2a) A tight CPU loop with NO suspension point has no checkpoint. Even after we cancel it,
     *     it keeps running to the end — cancellation is ignored. We keep the loop FINITE so the
     *     demo can never hang, and assert it reached the maximum (proving cancel had no effect).
     */
    @Test
    fun `a tight loop without checkpoints ignores cancellation`() = runBlocking {
        val max = 100_000
        val counter = AtomicInteger(0)

        // Run on a background thread so the loop and our cancel can proceed independently.
        val job = launch(Dispatchers.Default) {
            var i = 0
            while (i < max) {
                counter.incrementAndGet()      // pure CPU work — never suspends, never checks
                i++
            }
        }

        job.cancelAndJoin()                    // request stop... but there is no checkpoint to notice it.

        // Because nothing in the loop cooperates with cancellation, every iteration still ran.
        assertEquals(max, counter.get(), "no checkpoint means cancellation is silently ignored")
    }

    /**
     * 2b) THE FIX: add a checkpoint. `ensureActive()` throws `CancellationException` the moment
     *     the job is cancelled, so the loop bails out early. `isActive` (a soft guard) or
     *     `yield()` would work too. We assert the cooperative version stops BELOW the max.
     */
    @Test
    fun `ensureActive makes a CPU loop cooperative`() = runBlocking {
        val max = 100_000_000                  // big enough that it can't finish during our delay
        val counter = AtomicInteger(0)

        val job = launch(Dispatchers.Default) {
            var i = 0
            while (i < max) {
                ensureActive()                 // checkpoint: throws CancellationException if cancelled
                counter.incrementAndGet()
                i++
            }
        }

        delay(20.milliseconds)                              // let it spin for a bit...
        job.cancelAndJoin()                    // ...then cancel; ensureActive() sees it next iteration.

        // Unlike 2a, the cooperative loop bails out far short of `max`.
        println("cooperative loop stopped after ${counter.get()} of $max iterations")
        assertTrue(counter.get() < max, "ensureActive() should have stopped the loop early")
    }

    /**
     * 3) The idiomatic timeout pattern. Both variants cancel the block once the budget elapses;
     *    they differ only in how they REPORT a timeout:
     *      - `withTimeoutOrNull` returns `null`  (branch on the result — no try/catch),
     *      - `withTimeout`       throws          (`TimeoutCancellationException`).
     */
    @Test
    fun `withTimeoutOrNull returns null past the budget, value within it`() = runBlocking {
        // Work that FITS the budget returns its real value.
        val fits: String? = withTimeoutOrNull(100.milliseconds) {
            delay(10.milliseconds)
            "finished"
        }
        assertEquals("finished", fits)

        // Work that EXCEEDS the budget is cancelled and yields null instead of a value.
        val exceeds: String? = withTimeoutOrNull(20.milliseconds) {
            delay(200.milliseconds)                         // will be cancelled at 20ms, long before this returns
            "finished"
        }
        assertNull(exceeds)

        // The non-null variant throws instead of returning a sentinel.
        assertFailsWith<TimeoutCancellationException> {
            withTimeout(20.milliseconds) {
                delay(200.milliseconds)
            }
        }
        Unit
    }

    /**
     * 4) Cleanup on cancellation. A `try { } finally { }` inside a coroutine still runs its
     *    `finally` block when the coroutine is cancelled — perfect for releasing resources.
     *    CAVEAT: the coroutine is ALREADY cancelled, so any *suspending* call in `finally`
     *    would immediately throw. Wrap such calls in `withContext(NonCancellable) { }` to let
     *    them complete.
     */
    @Test
    fun `finally runs on cancellation and NonCancellable allows suspend cleanup`() = runBlocking {
        val cleanedUp = AtomicBoolean(false)

        val job = launch {
            try {
                delay(1_000.milliseconds)                   // will be cancelled well before this elapses
            } finally {
                // Without NonCancellable, this `delay` would throw and skip the cleanup below,
                // because we are unwinding an already-cancelled coroutine.
                withContext(NonCancellable) {
                    delay(5.milliseconds)                   // simulate an async resource release (flush/close)
                    cleanedUp.set(true)        // record that cleanup actually ran to completion
                }
            }
        }

        delay(20.milliseconds)                              // let it reach the delay(1000) suspension point...
        job.cancelAndJoin()                    // ...cancel, and wait for the finally block to finish.

        assertTrue(cleanedUp.get(), "finally must run on cancellation, even for suspend cleanup")
    }
}
