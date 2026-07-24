package other.concurrency

import kotlinx.coroutines.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * LESSON K02 — Dispatchers & `withContext`: where does a coroutine run?
 *
 * THE CORE IDEA
 * -------------
 * A coroutine is *not* bound to a thread. Instead, a coroutine is scheduled onto a thread by its
 * CoroutineDispatcher. The dispatcher is the "where does this code physically run" knob: it decides
 * which thread (or pool of threads) picks up the coroutine's work each time it resumes. Change the
 * dispatcher and you change the thread — without changing a single line of your business logic.
 *
 * A coroutine can even hop between threads over its lifetime: it runs on thread A, suspends at an
 * `await`, and later resumes on thread B. That is fine and expected — coroutines are about *what*
 * runs, dispatchers are about *where*.
 *
 * THE BUILT-IN DISPATCHERS
 * ------------------------
 *   - `Dispatchers.Default`    : CPU-bound work (parsing, sorting, number-crunching). Backed by a
 *                                shared pool sized to the number of CPU cores (min 2). Sizing it to
 *                                cores is deliberate: more threads than cores would just thrash the
 *                                CPU with context switches, not compute faster.
 *   - `Dispatchers.IO`         : blocking I/O (files, sockets, JDBC, `Thread.sleep`). Backed by a
 *                                LARGER, ELASTIC pool (default cap 64+) because I/O threads spend
 *                                most of their time parked waiting, so it is cheap and useful to
 *                                have many of them. IO shares threads with Default under the hood.
 *   - `Dispatchers.Unconfined` : advanced / rarely used. Starts in the caller's thread and, after a
 *                                suspension, resumes in whatever thread resumed it. Not "no thread" —
 *                                just "no confinement". Handy for certain operators/tests; a footgun
 *                                in ordinary application code. Avoid unless you know why you need it.
 *   - a confined UI / Main     : Android's `Dispatchers.Main`, JavaFX, Swing EDT, etc. A single,
 *                                CONFINED thread that owns the UI. You must touch UI state only from
 *                                it. (Not exercised here — it requires a UI platform on the classpath —
 *                                but the mental model is: "one special thread, everything UI goes there".)
 *
 * `withContext(dispatcher) { ... }` is the workhorse for switching. It SUSPENDS the current
 * coroutine, moves the block onto `dispatcher`, runs it, then switches BACK to the original
 * dispatcher when the block finishes — and it RETURNS the block's value. It is a normal expression,
 * not a fire-and-forget launch, so it composes like any other suspend call.
 *
 * THE GOLDEN RULE
 * ---------------
 * Never make a BLOCKING call (Thread.sleep, blocking I/O, a long lock wait) on `Dispatchers.Default`.
 * Default has only ~#cores threads; a blocked thread is a wasted core, and enough of them STARVE the
 * pool so other CPU coroutines can't run. Wrap blocking work in `withContext(Dispatchers.IO)` — IO's
 * elastic pool is built to absorb parked threads. Contrast the two words carefully:
 *   - BLOCKING  a thread : the OS thread is stuck, unusable by anyone until the call returns.
 *   - SUSPENDING a coroutine (e.g. `delay`) : the coroutine steps aside and FREES its thread for
 *                                             other coroutines; nothing is blocked. This is the
 *                                             whole point of coroutines.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * Every test runs inside `runBlocking { }` so it is self-contained and never leaks a coroutine or
 * hangs. We inspect `Thread.currentThread().name` to *prove* which pool executed a block. Thread
 * names are an implementation detail, so assertions are deliberately ROBUST: we check that a name
 * CONTAINS a pool marker (e.g. "DefaultDispatcher", "IO") or simply that it CHANGED from the caller —
 * never an exact string. Correctness assertions on computed values, by contrast, are EXACT and
 * deterministic every run.
 */
class K02Dispatchers {

    @Test
    fun `withContext(Default) runs CPU work on the Default pool`() = runBlocking {
        // runBlocking's own coroutine runs on the "main"/test thread that called it.
        val callerThread = Thread.currentThread().name

        // Switch onto the CPU pool for this block; withContext RETURNS the block's result.
        val threadInside = withContext(Dispatchers.Default) {
            // Some trivial CPU-bound work so there is a real reason to be on Default.
            val sum = (1..1_000).sum()
            assertEquals(500_500, sum) // exact, deterministic
            Thread.currentThread().name
        }

        // ROBUST assert: the block ran on a Default-pool thread, not the caller thread.
        assertTrue(
            threadInside.contains("DefaultDispatcher"),
            "expected a Default-pool thread, was '$threadInside'",
        )
        assertNotEquals(callerThread, threadInside, "work should have moved off the caller thread")
    }

    @Test
    fun `withContext(IO) runs blocking-style work on the IO pool`() = runBlocking {
        val callerThread = Thread.currentThread().name

        // IO is where blocking calls belong. We simulate a blocking read with a tiny Thread.sleep —
        // legal here precisely BECAUSE we are on the IO pool, not on Default.
        val threadInside = withContext(Dispatchers.IO) {
            Thread.sleep(1) // stand-in for a blocking file/socket/JDBC call
            Thread.currentThread().name
        }

        // ROBUST assert: IO-pool thread names contain "IO". Fall back to "changed thread" reasoning
        // in the message so the intent is clear even if the naming scheme ever shifts.
        assertTrue(
            threadInside.contains("IO"),
            "expected an IO-pool thread, was '$threadInside'",
        )
        assertNotEquals(callerThread, threadInside, "blocking work should have moved off the caller thread")
    }

    @Test
    fun `withContext returns the block's value and switches back`() = runBlocking {
        val before = Thread.currentThread().name

        // The value of a withContext expression is the value of its last line.
        val doubled: Int = withContext(Dispatchers.Default) {
            21 * 2
        }
        assertEquals(42, doubled) // proves the result flows back out

        val after = Thread.currentThread().name
        // After the block, we are back on the ORIGINAL dispatcher (the runBlocking thread).
        // Robust: assert we returned to the same caller thread, not any exact pool name.
        assertEquals(before, after, "withContext should switch the dispatcher back when the block ends")
    }

    @Test
    fun `Default parallelizes CPU work across the pool`() = runBlocking(Dispatchers.Default) {
        // Fan out N independent CPU tasks as coroutines. On the Default pool the runtime spreads them
        // across cores, so they progress in parallel. `async` starts each eagerly; `awaitAll` joins.
        val n = 100
        val partials = (1..n).map { i ->
            async {
                // Each coroutine computes i*i on some Default-pool thread.
                i * i
            }
        }

        val results: List<Int> = partials.awaitAll()

        // EXACT aggregate assert: sum of squares 1..100 = n(n+1)(2n+1)/6 = 338350.
        val total = results.sum()
        assertEquals(338_350, total)
        assertEquals(n, results.size)

        // Sanity: every task actually ran on the Default pool (not the caller by accident).
        // We can't assert *how many* distinct threads without flakiness, so we only assert the marker.
        val whereAmI = Thread.currentThread().name
        assertTrue(
            whereAmI.contains("DefaultDispatcher"),
            "the parent coroutine should live on Default, was '$whereAmI'",
        )
    }

    @Test
    fun `IO is the right home for blocking work while Default is reserved for CPU`() = runBlocking {
        // Demonstrate the golden rule as a pattern, deterministically: do the BLOCKING part on IO,
        // then hop to Default for the CPU part. This is the idiomatic "offload then compute" shape.

        // 1) Blocking-ish step on IO (simulated slow source).
        val raw = withContext(Dispatchers.IO) {
            Thread.sleep(1)                       // blocking — safe on IO's elastic pool
            listOf(3, 1, 4, 1, 5, 9, 2, 6)
        }

        // 2) CPU-bound step on Default (sorting/aggregating).
        val summary = withContext(Dispatchers.Default) {
            assertTrue(Thread.currentThread().name.contains("DefaultDispatcher"))
            raw.sorted().sum()
        }

        assertEquals(31, summary) // exact result: 3+1+4+1+5+9+2+6 = 31
    }
}
