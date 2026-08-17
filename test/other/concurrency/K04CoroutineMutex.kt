package other.concurrency

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * LESSON K04 — Shared mutable state in coroutines: Mutex and confinement.
 *
 * THE CORE IDEA
 * -------------
 * Coroutines are NOT immune to race conditions. This is the single most common
 * misconception about them. `launch { }` starts a concurrent job; it does NOT
 * serialize access to anything. When many coroutines run on a MULTI-THREADED
 * dispatcher (`Dispatchers.Default` has one worker thread per CPU core) and they
 * all touch the same `var`, you get the exact same "lost update" race that plain
 * threads suffer from (see LESSON 01):
 *     1. read  counter     (suspend / yield here, and another coroutine reads the SAME value)
 *     2. add   1
 *     3. write counter     (both write back the same +1 — one increment vanishes)
 * A suspension point (`yield()`, any `suspend` call) in the middle of a
 * read-modify-write turns a rare race into an almost-certain one, because it hands
 * the thread to another coroutine right when the value is stale in a local.
 *
 * FOUR WAYS THIS FILE FIXES IT
 * ----------------------------
 *   - FIX A — coroutine `Mutex` + `withLock { }` : mutual exclusion that SUSPENDS
 *             (never blocks the underlying thread) while waiting for the lock.
 *   - FIX B — `AtomicInteger`                    : a single hardware CAS; simplest &
 *             fastest for a plain counter — no lock needed at all.
 *   - FIX C — confinement                        : run every mutation on a
 *             single-threaded context so the updates physically cannot overlap.
 *             "Don't share — confine."
 *
 * WHICH TO REACH FOR
 * ------------------
 * Prefer confinement, atomics, or immutable state over locks wherever you can — they
 * are simpler and harder to misuse. Reach for a `Mutex` when your critical section
 * spans MULTIPLE suspend calls (e.g. read a cache, await a network fetch, then write
 * back) and must stay indivisible across those suspensions — that is the one thing an
 * atomic cannot express.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * The buggy demo asserts only a *loose* invariant (result <= expected) plus a println,
 * because exactly how many updates are lost is nondeterministic — asserting it is
 * *always* wrong would itself be flaky. Every fixed version asserts EXACT equality,
 * deterministically, on every run. That asymmetry is the lesson: correctness under
 * concurrency must never depend on scheduling timing.
 */
class K04CoroutineMutex {

    private val coroutines = 100          // many concurrent jobs...
    private val incrementsPerCoroutine = 1_000
    private val expected = coroutines * incrementsPerCoroutine

    /**
     * Launch [coroutines] jobs on the multi-threaded [Dispatchers.Default] and wait for
     * all of them. Each job runs [work] [incrementsPerCoroutine] times. Because Default
     * spreads coroutines across several real threads, unguarded shared state races.
     */
    private fun runConcurrently(work: suspend () -> Unit) = runBlocking {
        // coroutineScope { } here would also work; launching into `this` (the runBlocking
        // scope) is enough — runBlocking waits for all its children before returning.
        val jobs = (1..coroutines).map {
            launch(Dispatchers.Default) {
                repeat(incrementsPerCoroutine) { work() }
            }
        }
        jobs.forEach { it.join() }        // wait for every coroutine before reading the result.
    }

    @Test
    fun `plain var on a multi-threaded dispatcher loses updates`() {
        var counter = 0                   // plain var: no protection, shared across threads.

        runConcurrently {
            // Spell the increment out across a suspension point to force interleaving:
            val seen = counter            // 1. read (into a local)
            yield()                       //    suspend — another coroutine now reads the SAME `seen`
            counter = seen + 1            // 2+3. write back a stale value -> updates are lost
        }

        // `launch` did NOT serialize anything, so we almost always lose increments.
        println("plain var: got $counter, expected $expected (lost ${expected - counter})")
        assertTrue(counter <= expected, "impossible to exceed the expected total")
    }

    @Test
    fun `Mutex withLock is correct`() {
        var counter = 0
        val mutex = Mutex()               // the coroutine-friendly lock.

        runConcurrently {
            // withLock SUSPENDS while waiting (it does NOT block the thread the way a JVM
            // `synchronized`/ReentrantLock would), so the worker thread stays free to run
            // other coroutines. The body is a critical section: one coroutine at a time.
            // WARNING: Mutex is NOT reentrant — calling withLock again while already holding
            // it (directly or via a nested call) deadlocks. Don't lock inside a locked section.
            mutex.withLock {
                val seen = counter
                yield()                   // even suspending inside the lock is safe: the lock is held.
                counter = seen + 1
            }
        }

        assertEquals(expected, counter)   // exact, deterministic, every run.
    }

    @Test
    fun `AtomicInteger is simpler and faster for a plain counter`() {
        val counter = AtomicInteger(0)

        // For a single-variable read-modify-write, an atomic beats a Mutex: it's a lock-free
        // CAS, no suspension, less code. Prefer this whenever the critical section is one op.
        runConcurrently { counter.incrementAndGet() }

        assertEquals(expected, counter.get())
    }

    @Test
    fun `confinement to a single-threaded context is correct`() {
        var counter = 0

        // limitedParallelism(1) carves a single-threaded view out of Dispatchers.Default.
        // Every mutation runs on that ONE thread, so two updates can never overlap — no lock
        // is needed because there is no true parallelism on the shared state. "Don't share,
        // confine": funnel all access to a resource through a single execution context.
        val confined = Dispatchers.Default.limitedParallelism(1)

        runConcurrently {
            withContext(confined) {       // hop onto the confining context for the mutation.
                val seen = counter
                yield()                   // suspending is fine: work resumes on the same single thread.
                counter = seen + 1
            }
        }

        assertEquals(expected, counter)
    }
}
