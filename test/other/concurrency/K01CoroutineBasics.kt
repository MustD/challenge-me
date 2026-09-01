package other.concurrency

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

/**
 * LESSON K01 — Coroutine basics: `launch`, `async`/`await`, and structured concurrency.
 *
 * THE CORE IDEA
 * -------------
 * A coroutine is a LIGHTWEIGHT, cooperatively-scheduled task — not an OS thread. A thread carries
 * a big fixed stack (~1 MB), so a few thousand of them exhaust memory; a coroutine is just a small
 * heap object, so you can run hundreds of thousands on a handful of threads. The trick is
 * SUSPENSION: when a coroutine hits a suspension point (a `suspend fun` such as `delay`) it does not
 * BLOCK the underlying thread — it saves its state, RELEASES the thread so other coroutines can run,
 * and resumes later where it left off. "Suspend, don't block" is the whole mental model.
 *
 *     blocking  (Thread.sleep) : the thread sits idle, parked, useful to nobody.
 *     suspending (delay)       : the thread is handed back to the scheduler and keeps working.
 *
 * A `suspend` function can only be called from another `suspend` function or from a coroutine
 * BUILDER (`launch`, `async`, `runBlocking`, `coroutineScope`). That's why suspension "colors" the
 * call chain — the compiler needs a coroutine context to save/restore state into.
 *
 * THE TOOLS IN THIS FILE
 * ----------------------
 *   - `runBlocking { }`   : bridges normal blocking code (like a @Test) into the coroutine world.
 *                           It BLOCKS the current thread until everything inside finishes — which is
 *                           exactly what we want in a test: deterministic, no hanging, no leaks.
 *   - `launch { }`        : fire-and-forget. Starts a child coroutine, returns a `Job` (no result).
 *   - `async { } + await`: starts a child that PRODUCES a value; returns a `Deferred<T>` (a future).
 *                           `await()` is a SUSPENSION point that yields the result — not a blocking get.
 *   - `coroutineScope { }`: creates a scope that suspends until ALL its children complete.
 *
 * STRUCTURED CONCURRENCY
 * ----------------------
 * Children are launched INTO a parent scope, and the parent scope does not complete until every
 * child it started has completed. No coroutine is ever "orphaned" or leaked; you never have to
 * manually track and join a pile of handles the way you do with raw threads. If a child fails, the
 * parent (and its siblings) are cancelled too. That guarantee is what makes the asserts below safe:
 * by the time the scope returns, all the work is provably done.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * Every test wraps its coroutines in `runBlocking { }`, so the test method only returns once all
 * child coroutines have finished — the results are therefore fully settled and we assert EXACT
 * values, deterministically, every run. Nothing here can hang: `delay` timeouts are tiny and the
 * scope always joins its children. The one timing-based check uses a deliberately generous upper
 * bound so it demonstrates concurrency without being flaky.
 */
class K01CoroutineBasics {

    /**
     * 1) `launch` (fire-and-forget) + structured concurrency.
     *
     * We start a child coroutine with `launch`. It suspends on `delay` (releasing the thread), then
     * updates shared state. The KEY point: `runBlocking` is itself a scope, and it does NOT return
     * until its child is done — so reading `result` AFTER the scope is guaranteed to see the update.
     * We never call `job.join()` by hand; structured concurrency joins for us.
     */
    @Test
    fun `launch starts a child and the scope waits for it`() = runBlocking {
        var result = 0

        val job = launch {              // schedule a child coroutine in runBlocking's scope
            delay(10.milliseconds)                   // suspend WITHOUT blocking the thread for ~10 ms
            result = 42                 // then update shared state
        }

        // `launch` does NOT block the caller: control reaches this line while the child is still
        // suspended on delay(), so the update is not visible yet.
        assertTrue(job.isActive, "child was launched but has not completed yet")

        // Structured concurrency in action: join() suspends until the child finishes. Even without
        // this explicit join, the runBlocking scope would wait for `job` before returning — no
        // orphaned coroutines, ever. We join here just to assert the settled result inline.
        job.join()
        assertEquals(42, result)        // the update is now guaranteed to be visible.
    }

    /**
     * 2) `async` + `await`: run two computations CONCURRENTLY and combine their results.
     *
     * `async` returns a `Deferred<T>` — a future/promise. `await()` is a suspension point that
     * resumes with the value once it's ready; it is NOT a blocking `.get()`. Because both `async`
     * blocks start before we `await` either one, the two `delay`s overlap: total time is ≈ the
     * MAX of the two delays, not their SUM.
     */
    @Test
    fun `async returns a Deferred and two run concurrently`() = runBlocking {
        lateinit var combined: Pair<Int, Int>

        val elapsed = measureTimeMillis {
            val a = async { delay(50.milliseconds); 20 }   // starts immediately, computes 20 after ~50 ms
            val b = async { delay(50.milliseconds); 22 }   // starts immediately too — overlaps with `a`

            // Suspend here until each result is ready. Order of await() doesn't change the timing:
            // both coroutines were already running concurrently the moment async{} returned.
            combined = a.await() to b.await()
        }

        assertEquals(20 to 22, combined)
        assertEquals(42, combined.first + combined.second)

        // Concurrency proof: if these ran SEQUENTIALLY it would take ~100 ms (50 + 50).
        // Running concurrently it's ~50 ms. We assert a generous upper bound (< 2x one delay = 100 ms)
        // so the test stays non-flaky under scheduler jitter while still ruling out sequential exec.
        assertTrue(elapsed < 100, "expected concurrent (~50ms), took ${elapsed}ms")
    }

    /**
     * 3) Structured concurrency at scale: launch MANY cheap coroutines and let the scope join them.
     *
     * We fire 1000 coroutines, each incrementing a shared AtomicInteger, inside `coroutineScope`.
     * `coroutineScope` suspends until every child has finished, so once it returns the total is
     * complete and we assert the EXACT count. (We use AtomicInteger because the children may run on
     * multiple threads — see lesson L04 for why plain `count++` would lose updates.)
     *
     * Contrast: spawning 1000 OS threads would allocate ~1000 stacks (hundreds of MB) and thrash the
     * scheduler; 1000 coroutines are tiny heap objects multiplexed onto a small thread pool. Push
     * this to 100_000 and threads simply fall over — coroutines don't.
     */
    @Test
    fun `coroutineScope joins a thousand cheap coroutines`() = runBlocking {
        val count = AtomicInteger(0)
        val n = 1000

        coroutineScope {                     // this scope will not return until all n children finish
            repeat(n) {
                launch {
                    delay(1.milliseconds)                 // suspend to force real interleaving across the pool
                    count.incrementAndGet()  // atomic: safe regardless of which thread resumes us
                }
            }
        }                                    // <-- suspends here until every launched child completes

        // Because the scope joined all children, the total is fully settled and exact.
        assertEquals(n, count.get())
    }
}
