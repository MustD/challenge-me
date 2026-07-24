package other.concurrency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * LESSON K06 — Flow: cold asynchronous streams.
 *
 * THE CORE IDEA
 * -------------
 * A `Flow<T>` is the coroutine equivalent of a LAZY, ASYNCHRONOUS `Sequence`: a stream that can
 * produce MANY values OVER TIME, on demand. Where `suspend fun` returns a SINGLE value later, a
 * `Flow` returns a STREAM of values later. The producer body runs inside a coroutine, so it may
 * `suspend` (e.g. `delay`, a network call) between emissions without blocking a thread.
 *
 * The defining property is that flows are COLD:
 *     - Nothing runs when you *build* a flow. `flow { ... }` just captures the producer block.
 *     - The block only starts executing when a TERMINAL operator (`collect`, `toList`, `first`,
 *       `reduce`, `fold`, ...) is applied — and it runs AGAIN, from scratch, for every new terminal
 *       call. A flow is a recipe, not a running pipeline.
 *
 *     COLD (Flow)             : each collector triggers its own fresh run of the producer.
 *     HOT  (Channel/SharedFlow): the source produces independently of collectors; values are pushed
 *                                whether or not anyone is listening, and are shared/not replayed.
 * (Compare lesson L06 producer/consumer over a Channel — a Channel is HOT: send() pushes eagerly.)
 *
 * THE TOOLS IN THIS FILE
 * ----------------------
 *   - `flow { emit(x) }`  : the cold-stream builder; `emit` is a suspend fun that hands a value down.
 *   - operators           : `map`, `filter`, `take`, `transform` — pure transformations on the
 *                           stream. Each returns a NEW cold flow; they are suspending-aware (may call
 *                           suspend funs) and do nothing until a terminal operator pulls values.
 *   - terminal operators  : `toList()`, `first()`, `reduce`/`fold`, `collect {}` — these actually
 *                           run the pipeline and are themselves `suspend`.
 *   - `flowOn(dispatcher)`: shifts the UPSTREAM (producer + operators above it) onto another
 *                           dispatcher, WITHOUT changing the collector's context (context preservation).
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * Every test runs inside `runBlocking { }`, and every flow is drained with a terminal operator
 * (`toList()` / `first()` / `reduce` / `fold`) into a concrete value we assert EXACTLY — contents and
 * order. Flows are ordered and, here, finite and fast, so results are fully deterministic and nothing
 * can hang: `take(n)` bounds infinite producers and `delay`s are tiny. The coldness lesson is proven
 * with a side-effect counter that records how many times the producer body actually ran.
 */
class K06Flow {

    /**
     * 1) COLD: nothing runs until you collect, and each collect re-runs the producer from scratch.
     *
     * We wire a side-effect counter into the producer body. Building the flow must NOT touch it;
     * only a terminal operator does — and every terminal call runs the whole body again.
     */
    @Test
    fun `a flow is cold - it runs once per terminal collection`() = runBlocking {
        val producerRuns = AtomicInteger(0)

        // Building the flow executes NOTHING. This just captures the recipe.
        val numbers: Flow<Int> = flow {
            producerRuns.incrementAndGet()   // side effect: proves the body actually ran
            emit(1)                          // emit is a suspend fun — hands a value downstream
            emit(2)
            emit(3)
        }

        // Not collected yet, so the producer has not run at all.
        assertEquals(0, producerRuns.get(), "cold flow must not run before a terminal operator")

        // First terminal collection -> body runs once, producing the full stream in order.
        val first = numbers.toList()
        assertEquals(listOf(1, 2, 3), first)
        assertEquals(1, producerRuns.get())

        // Second terminal collection -> body runs AGAIN from scratch (this is what "cold" means).
        val second = numbers.toList()
        assertEquals(listOf(1, 2, 3), second)
        assertEquals(2, producerRuns.get(), "each new collection re-runs the producer")
    }

    /**
     * 2) OPERATORS: build a pipeline, then drain it with terminal operators.
     *
     * `map`/`filter`/`take`/`transform` are just per-element transformations layered on the stream;
     * each returns a new cold flow. `transform` is the general form — it can emit zero, one, or many
     * values per input (map = exactly one, filter = zero or one), so everything else is a special case.
     */
    @Test
    fun `operators map filter take transform build a pipeline`() = runBlocking {
        // An INFINITE producer — safe because `take` will cut it short downstream.
        val naturals = flow {
            var i = 1
            while (true) emit(i++)           // 1, 2, 3, ... forever
        }

        val result = naturals
            .map { it * it }                 // squares: 1, 4, 9, 16, 25, 36, 49, ...
            .filter { it % 2 == 1 }          // keep odd squares: 1, 9, 25, 49, ...
            .take(3)                         // stop after 3 -> cancels the infinite upstream cleanly
            .toList()                        // terminal: actually runs the pipeline into a list

        assertEquals(listOf(1, 9, 25), result)

        // `transform` is the Swiss-army operator: emit any number of values per input. Here we fan
        // each element out into two — the value and its negation — to show >1 emission per input.
        val fanned = flowOf(1, 2, 3)         // flowOf: a tiny cold flow from fixed values
            .transform { v ->
                emit(v)
                emit(-v)
            }
            .toList()
        assertEquals(listOf(1, -1, 2, -2, 3, -3), fanned)

        // Other terminal operators, each running the pipeline independently:
        assertEquals(1, flowOf(1, 2, 3).first())                 // first(): take the first, then stop
        assertEquals(6, flowOf(1, 2, 3).reduce { a, b -> a + b }) // reduce: fold with first as seed
        assertEquals(16, flowOf(1, 2, 3).fold(10) { a, b -> a + b }) // fold: explicit initial accumulator
    }

    /**
     * 3) `flowOn`: context preservation — shift the UPSTREAM without moving the collector.
     *
     * `flowOn(Dispatchers.Default)` moves the producer (and any operators ABOVE it) onto the Default
     * dispatcher's threads. Crucially it does NOT change where `collect` runs: the downstream stays on
     * the caller's context. This is why flows never "leak" their execution context to the consumer the
     * way a raw callback might. Results are identical regardless of dispatcher — we just prove the
     * values are correct and note the thread split in comments.
     */
    @Test
    fun `flowOn shifts upstream context but not the collector`() = runBlocking {
        // Name the caller's coroutine so we can spot the collector's thread/context in the println.
        val upstreamThreads = mutableSetOf<String>()

        val values = flow {
            // This body runs on Dispatchers.Default (set by flowOn below), NOT on runBlocking's thread.
            upstreamThreads += Thread.currentThread().name
            emit(10)
            emit(20)
            emit(30)
        }
            .map { it + 1 }                  // still upstream of flowOn -> also on Default
            .flowOn(Dispatchers.Default)     // <-- everything ABOVE runs on Default

        val collectorThread = Thread.currentThread().name
        val collected = values.toList()      // collect runs HERE, on the caller's (runBlocking) thread

        // Correctness is unaffected by the dispatcher shift: order and values are preserved.
        assertEquals(listOf(11, 21, 31), collected)

        // The producer ran off the collector's thread — context was preserved, not merged.
        println("flowOn: upstream=$upstreamThreads  collector=$collectorThread")
        assertTrue(upstreamThreads.isNotEmpty(), "producer should have recorded its own thread")
    }

    /**
     * 4) ASYNCHRONOUS emission: values arrive over time, gathered in order.
     *
     * The producer `delay`s between emissions — modelling a stream that trickles in (ticks, network
     * pages, sensor readings). Because the coroutine SUSPENDS rather than blocks during each delay,
     * the thread is free meanwhile; `toList()` still collects everything IN EMISSION ORDER once done.
     *
     * HOT counterparts (not shown, just for orientation):
     *   - `StateFlow`  : a hot flow that always holds the LATEST value — ideal for observable STATE.
     *   - `SharedFlow` : a hot broadcast of EVENTS to multiple collectors, with configurable replay.
     *   - backpressure : `buffer` lets a fast producer run ahead of a slow collector; `conflate`
     *                    drops intermediate values, keeping only the newest.
     */
    @Test
    fun `flow emits asynchronously over time and toList preserves order`() = runBlocking {
        val ticks = flow {
            for (n in 1..4) {
                delay(5)                     // suspend ~5ms between values — non-blocking wait
                emit(n * 100)                // 100, 200, 300, 400 spaced out in time
            }
        }

        // toList() suspends until the whole (finite) stream has arrived, then returns it in order.
        val gathered = ticks.toList()
        assertEquals(listOf(100, 200, 300, 400), gathered)

        // first() short-circuits: it needs only the earliest value, so it stops after one emission
        // (~5ms) instead of waiting for all four. Same cold flow, re-run from scratch.
        assertEquals(100, ticks.first())
    }
}
