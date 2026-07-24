package other.concurrency

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * LESSON 07 — Thread pools: ExecutorService, Callable, and Future.
 *
 * THE CORE IDEA
 * -------------
 * In real code you almost NEVER write `Thread { ... }.start()` by hand. Creating a fresh OS
 * thread per task is expensive, unbounded, and hard to reason about (who joins them? how many
 * exist right now?). Instead you hand *tasks* to a managed **thread pool** — an
 * `ExecutorService` — and let it reuse a fixed set of worker threads to run them.
 *
 * You describe a unit of work two ways:
 *   - `Runnable`     : `() -> Unit`  — does something, returns nothing.
 *   - `Callable<T>`  : `() -> T`     — computes and returns a value (and may throw).
 *
 * And you hand it off two ways:
 *   - `execute(Runnable)`  : FIRE-AND-FORGET. No handle, no result. If the task throws, the
 *                            exception goes to the thread's *uncaught exception handler* — you
 *                            will not see it at the call site.
 *   - `submit(Callable<T>)`: returns a `Future<T>` IMMEDIATELY — a handle to a result that does
 *                            not exist yet. `future.get()` BLOCKS until the task finishes, then
 *                            returns its value (or re-throws its exception, see below).
 *
 * So `submit` is asynchronous at the point of call (it returns right away) but `get()` is where
 * you synchronize and collect the answer. This "submit now, get later" split is the whole point
 * of a Future: you can fire off many tasks, let them run in parallel on the pool, then gather.
 *
 * COMMON POOL TYPES (java.util.concurrent.Executors)
 * --------------------------------------------------
 *   - newFixedThreadPool(n)          : exactly n reusable workers; extra tasks queue and wait.
 *   - newCachedThreadPool()          : grows/shrinks on demand, reuses idle threads (bursty work).
 *   - newSingleThreadExecutor()      : one worker — tasks run one at a time, in submission order.
 *   - newScheduledThreadPool(n)      : run tasks after a delay or on a repeating schedule.
 *   - newVirtualThreadPerTaskExecutor(): modern JVMs (Project Loom) — one cheap *virtual* thread
 *                                        per task, so you can have millions of blocking tasks.
 *
 * ALWAYS SHUT THE POOL DOWN
 * -------------------------
 * A default pool's worker threads are NON-DAEMON. A non-daemon thread keeps the JVM alive, so if
 * you forget to shut the pool down your program (or test run) can hang forever after main is done.
 * The discipline: `shutdown()` (stop accepting new tasks, let queued ones finish) then
 * `awaitTermination(timeout)` (block until they do), always in a `finally`. This file wraps that
 * in a small [withPool] helper so every test is guaranteed to release its pool.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * Everything here is DETERMINISTIC despite running in parallel. Order of *execution* is
 * nondeterministic, but the *results* are not: sums, squares and the wrapped-exception behaviour
 * are fixed facts, so we assert exact equality — never "roughly" or "eventually". Every test runs
 * inside [withPool], which shuts the executor down in a finally, so the suite can never hang on a
 * lingering worker.
 */
class L07Executors {

    /**
     * Borrow a pool, run [block] with it, and ALWAYS shut it down afterwards.
     *
     * This is the manual equivalent of a try-with-resources / Kotlin `use`: the `finally` runs
     * `shutdown()` + `awaitTermination()` no matter how [block] exits (normal return OR exception),
     * so we never leak a non-daemon worker that would keep the JVM alive.
     */
    private fun <R> withPool(threads: Int, block: (ExecutorService) -> R): R {
        val pool = Executors.newFixedThreadPool(threads)
        try {
            return block(pool)
        } finally {
            pool.shutdown()                                   // stop accepting new tasks; finish queued ones.
            // Block until the queued tasks drain. The boolean says whether they finished in time;
            // a well-behaved pool always does, so we assert it to catch a runaway task early.
            val drained = pool.awaitTermination(5, TimeUnit.SECONDS)
            assertTrue(drained, "pool must terminate within the timeout — no runaway tasks")
        }
    }

    // ---------------------------------------------------------------------------------------
    // 1) submit(Callable) -> Future -> get(). Fan several tasks out, gather the results.
    // ---------------------------------------------------------------------------------------

    @Test
    fun `submit returns a Future and get blocks for the result`() = withPool(threads = 4) { pool ->
        // Four independent Callables, each returning an Int. submit() returns instantly with a
        // Future handle; the task itself runs on some pool worker, possibly not yet started.
        val futures: List<Future<Int>> = (1..4).map { n ->
            pool.submit(Callable { n * 10 })                  // Callable<Int>: computes and returns.
        }

        // get() BLOCKS this thread until each task has finished, then yields its value. By the time
        // the fold completes we have synchronized with all four workers.
        val total = futures.fold(0) { acc, f -> acc + f.get() }

        // 10 + 20 + 30 + 40 — an exact, deterministic fact regardless of who ran first.
        assertEquals(100, total)
    }

    // ---------------------------------------------------------------------------------------
    // 2) invokeAll — submit a whole batch, block until ALL finish, results stay in order.
    // ---------------------------------------------------------------------------------------

    @Test
    fun `invokeAll runs a batch in parallel and preserves order`() = withPool(threads = 8) { pool ->
        // A batch of 100 Callables: task i computes i*i. Build them as a collection up front.
        val tasks: List<Callable<Int>> = (1..100).map { i -> Callable { i * i } }

        // invokeAll BLOCKS until every task has completed, then returns a List<Future> in the SAME
        // order as the input collection (Future #0 corresponds to task #0), regardless of the order
        // the workers actually finished in.
        val results: List<Future<Int>> = pool.invokeAll(tasks)

        val sumOfSquares = results.sumOf { it.get() }         // get() here never blocks — all are done.

        // sum_{i=1..100} i^2 = 100*101*201/6 = 338350 — closed form, so we know the exact answer.
        assertEquals(338_350, sumOfSquares)
        // Order preservation is part of the contract: the first future is 1*1, the last is 100*100.
        assertEquals(1, results.first().get())
        assertEquals(10_000, results.last().get())
    }

    // ---------------------------------------------------------------------------------------
    // 3) execute (fire-and-forget) vs submit (Future + wrapped exception).
    // ---------------------------------------------------------------------------------------

    @Test
    fun `execute is fire-and-forget with no result handle`() = withPool(threads = 1) { pool ->
        // execute takes a Runnable and returns Unit — there is NO Future, so no way to get a result
        // or observe completion through the call site. We use an external AtomicInteger as a
        // side-channel purely to prove the task ran (and to synchronize deterministically).
        val sideEffect = AtomicInteger(0)

        pool.execute { sideEffect.addAndGet(7) }              // Runnable: does work, returns nothing.

        // execute hands back NO Future, so we cannot wait on the Runnable directly. This is a
        // SINGLE-thread pool, so tasks run strictly in submission order on the one worker: the
        // follow-up Callable below cannot start until the Runnable has finished. get() blocks until
        // that follow-up completes, which deterministically means the Runnable's write is done and
        // visible to us here.
        pool.submit(Callable { sideEffect.get() }).get()

        assertEquals(7, sideEffect.get())
        // KEY CONTRAST: if that Runnable had THROWN, the exception would go to the thread's uncaught
        // handler — silently, from execute's perspective. There is no Future to carry it back.
    }

    @Test
    fun `submit wraps a thrown exception in ExecutionException on get`() = withPool(threads = 2) { pool ->
        // A Callable that fails. submit() still returns a Future immediately — the failure is
        // captured and stored INSIDE the Future, not thrown here at the submission site.
        val future: Future<Int> = pool.submit(Callable<Int> {
            throw IllegalStateException("boom")               // the task's own exception.
        })

        // The exception surfaces only when we call get(): the executor RE-THROWS it, wrapped in an
        // ExecutionException. That wrapping is how a Future carries a failure across thread
        // boundaries — the original exception is available via .cause.
        val ex = assertFailsWith<ExecutionException> { future.get() }

        // The real cause is preserved, so callers can inspect what actually went wrong.
        assertTrue(ex.cause is IllegalStateException)
        assertEquals("boom", ex.cause?.message)
    }
}
