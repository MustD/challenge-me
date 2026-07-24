package other.concurrency

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * LESSON 10 — Asynchronous composition with CompletableFuture.
 *
 * THE CORE IDEA
 * -------------
 * A `CompletableFuture<T>` is a *promise* of a value that will arrive later. Instead of a thread
 * sitting and blocking on `future.get()` until the answer is ready, you attach CALLBACKS that fire
 * when the value completes, building a pipeline:
 *
 *     supplyAsync { fetch() }        // step 1 runs on a background thread, returns CF<A>
 *         .thenApply { transform(it) }   // step 2 runs when step 1 finishes, returns CF<B>
 *         .thenCompose { anotherAsync(it) }  // step 3 kicks off a dependent async call
 *
 * No thread is parked between steps. Work hops from one pool thread to the next as each stage
 * completes. This is *non-blocking composition*: you describe the dataflow ("when A is done, do B")
 * and the runtime schedules it. By default the async work runs on the common ForkJoinPool
 * (`ForkJoinPool.commonPool()`), so we never create or shut down an executor here.
 *
 * THE OPERATORS (each returns a NEW future — the originals are immutable)
 * ----------------------------------------------------------------------
 *   - `supplyAsync { }`  : start an async computation on a pool thread; yields CF<T>.
 *   - `thenApply { }`    : the async `map` — take the result, return a plain VALUE (CF<T> -> CF<R>).
 *   - `thenCompose { }`  : the async `flatMap` — take the result, return ANOTHER FUTURE; the chain
 *                          is flattened so you get CF<R>, not CF<CF<R>>. Use it when the next step
 *                          is itself asynchronous AND depends on the previous result.
 *   - `thenCombine { }`  : join TWO independent futures running in parallel; the combiner fires only
 *                          once BOTH complete.
 *   - `allOf(...)`       : fan out N futures; returns CF<Void> that completes when ALL finish. Gather
 *                          each result with `.join()` afterwards (safe — they're already done).
 *   - `exceptionally { }`: catch a failure anywhere upstream and substitute a fallback value.
 *   - `handle { v, e }`  : like exceptionally but sees BOTH outcomes (value OR throwable).
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * Every test builds a pipeline, then calls `.get()` / `.join()` ONCE at the very end to await the
 * final result and assert it EXACTLY. Blocking only at the boundary (to observe the answer) is the
 * normal shape — the composition itself never blocks a thread. Because each pipeline is
 * deterministic (same inputs -> same output), we assert exact equality on every run; nothing here
 * can hang because every future is guaranteed to complete.
 *
 * thenApply vs thenCompose — the key distinction:
 *     thenApply  { x -> f(x)       }   // f returns a VALUE  -> CF<Value>
 *     thenApply  { x -> asyncF(x)  }   // WRONG: returns CF   -> CF<CF<Value>> (nested!)
 *     thenCompose{ x -> asyncF(x)  }   // f returns a FUTURE -> CF<Value> (flattened)
 *
 * CONTRAST WITH KOTLIN COROUTINES
 * -------------------------------
 * Coroutines (see lessons K01–K06) solve the same problem — non-blocking composition — but read as
 * plain sequential code: `val a = fetch(); val b = transform(a)` inside a `suspend fun`, with
 * `async {}` / `await()` for parallelism. CompletableFuture makes the callback graph explicit;
 * coroutines hide it behind suspension points. Same non-blocking behaviour, different ergonomics.
 */
class L10CompletableFuture {

    // ---------------------------------------------------------------------------------------------
    // 1. supplyAsync + thenApply — run a computation, then transform its result (the async `map`).
    // ---------------------------------------------------------------------------------------------
    @Test
    fun `supplyAsync then thenApply transforms the result`() {
        // supplyAsync runs { 21 } on the common ForkJoinPool and returns CF<Int> immediately.
        // thenApply is the async map: it receives 21 (when ready) and returns a plain Int, 42.
        val pipeline: CompletableFuture<Int> =
            CompletableFuture.supplyAsync { 21 }
                .thenApply { it * 2 }             // 21 -> 42, still CF<Int> (no nesting)

        // get() blocks HERE, only to observe the final answer for the assertion.
        assertEquals(42, pipeline.get())
    }

    // ---------------------------------------------------------------------------------------------
    // 2. thenCompose — flat-map a dependent async call (avoids CF<CF<T>>).
    // ---------------------------------------------------------------------------------------------

    /** Pretend-async lookup: doubling a number is modelled as its own future. */
    private fun doubleAsync(n: Int): CompletableFuture<Int> =
        CompletableFuture.supplyAsync { n * 2 }

    @Test
    fun `thenCompose chains a dependent future without nesting`() {
        // Step 2 (doubleAsync) DEPENDS on step 1's result (10) and is itself asynchronous.
        // If we used thenApply here we'd get CF<CF<Int>>; thenCompose flattens it to CF<Int>.
        val pipeline: CompletableFuture<Int> =
            CompletableFuture.supplyAsync { 10 }
                .thenCompose { doubleAsync(it) }  // 10 -> CF<20>, flattened to CF<20>

        assertEquals(20, pipeline.get())
    }

    // ---------------------------------------------------------------------------------------------
    // 3. thenCombine — run two INDEPENDENT futures in parallel, combine when both complete.
    // ---------------------------------------------------------------------------------------------
    @Test
    fun `thenCombine merges two parallel futures`() {
        val a = CompletableFuture.supplyAsync { 7 }    // these two start concurrently on the pool;
        val b = CompletableFuture.supplyAsync { 5 }    // neither waits for the other.

        // The combiner fires only once BOTH a and b have completed.
        val combined: CompletableFuture<Int> = a.thenCombine(b) { x, y -> x + y }  // 7 + 5

        assertEquals(12, combined.get())
    }

    // ---------------------------------------------------------------------------------------------
    // 4. allOf — fan out N independent futures, await all, then gather their results.
    // ---------------------------------------------------------------------------------------------
    @Test
    fun `allOf waits for every future then gathers results`() {
        // Five independent computations, all running in parallel on the common pool.
        val futures: List<CompletableFuture<Int>> =
            (1..5).map { n -> CompletableFuture.supplyAsync { n * n } }  // 1,4,9,16,25

        // allOf completes when ALL of them do. It carries no value itself (CF<Void>),
        // so we .join() it purely to await, then harvest each result.
        CompletableFuture.allOf(*futures.toTypedArray()).join()

        // Safe to .join() each one now — they are all already complete, so this never blocks long.
        val total = futures.sumOf { it.join() }

        assertEquals(1 + 4 + 9 + 16 + 25, total)  // 55
    }

    // ---------------------------------------------------------------------------------------------
    // 5. exceptionally / handle — recover from a failure in the chain.
    // ---------------------------------------------------------------------------------------------

    @Test
    fun `exceptionally recovers a failed future with a fallback`() {
        // An exception thrown inside a stage completes the future EXCEPTIONALLY and propagates
        // DOWN the chain, skipping normal stages, until a handler catches it.
        val recovered: CompletableFuture<Int> =
            CompletableFuture.supplyAsync<Int> { throw IllegalStateException("boom") }
                .thenApply { it + 1 }              // SKIPPED — upstream already failed.
                .exceptionally { -1 }              // caught here: substitute the fallback value.

        assertEquals(-1, recovered.get())          // fallback observed, get() does NOT throw.
    }

    @Test
    fun `handle sees both the value and the throwable`() {
        // handle runs on EITHER outcome: (value, null) on success, (null, throwable) on failure.
        // Here the chain succeeds, so the throwable is null and we pass the value through.
        val ok: CompletableFuture<String> =
            CompletableFuture.supplyAsync { 100 }
                .handle { value, error ->
                    if (error != null) "failed: ${error.message}" else "ok: $value"
                }

        assertEquals("ok: 100", ok.get())
    }

    @Test
    fun `get on an uncaught failure throws ExecutionException`() {
        // Without a handler, the failure surfaces at the boundary: get() wraps the original
        // exception in an ExecutionException. This is why exceptionally/handle exist — to catch
        // failures INSIDE the pipeline instead of at every call site.
        val failing: CompletableFuture<Int> =
            CompletableFuture.supplyAsync<Int> { throw IllegalStateException("boom") }

        var thrown = false
        try {
            failing.get()
        } catch (e: java.util.concurrent.ExecutionException) {
            thrown = true
            // The original cause is preserved as the ExecutionException's cause.
            assertTrue(e.cause is IllegalStateException)
            assertEquals("boom", e.cause?.message)
        }
        assertTrue(thrown, "get() must throw ExecutionException when the future failed")

        // NOTE: join() (used earlier) throws the *unchecked* CompletionException instead — same
        // cause, no checked-exception ceremony. Referenced here so the import is meaningful.
        assertTrue(CompletionException(IllegalStateException()).cause is IllegalStateException)
    }
}
