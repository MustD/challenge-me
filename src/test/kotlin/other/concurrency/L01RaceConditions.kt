package other.concurrency

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * LESSON 01 — Race conditions & the "lost update" problem.
 *
 * THE CORE IDEA
 * -------------
 * `counter++` looks atomic in source, but on the JVM it is THREE steps:
 *     1. read  counter        (load the current value into a register)
 *     2. add   1              (compute value + 1)
 *     3. write counter        (store the new value back)
 * This read-modify-write is not atomic. When two threads interleave — both read 41,
 * both compute 42, both write 42 — one increment is silently lost. With many threads
 * hammering the same variable, thousands of updates vanish. This is a *race condition*:
 * the result depends on the (nondeterministic) timing of thread scheduling.
 *
 * THREE WAYS THIS FILE FIXES IT
 * -----------------------------
 *   - `synchronized`     : mutual exclusion — only one thread in the critical section at a time.
 *   - `AtomicInteger`     : a single hardware CAS instruction makes read-modify-write atomic, lock-free.
 *   - (see later lessons) : explicit locks, and coroutine `Mutex`.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * The buggy demo can only assert a *loose* invariant (result <= expected), because "how many
 * updates are lost" is nondeterministic — asserting it is *always* wrong would itself be flaky.
 * The fixed versions assert exact equality, deterministically, every run. That asymmetry is the
 * whole lesson: correctness under concurrency must not depend on timing.
 */
class L01RaceConditions {

    private val threads = 8
    private val incrementsPerThread = 100_000
    private val expected = threads * incrementsPerThread

    /**
     * Run [work] on [threads] threads at once. A [CountDownLatch] acts as a starting gun so every
     * thread begins pounding the shared state at (roughly) the same instant — this maximizes
     * contention and makes races show up reliably instead of hiding behind lucky scheduling.
     */
    private fun runConcurrently(work: () -> Unit) {
        val startGun = CountDownLatch(1)
        val workers = (1..threads).map {
            thread {
                startGun.await()          // all workers block here...
                repeat(incrementsPerThread) { work() }
            }
        }
        startGun.countDown()              // ...and are released together.
        workers.forEach { it.join() }     // wait for all to finish before reading the result.
    }

    @Test
    fun `unsynchronized counter loses updates`() {
        var counter = 0                   // plain var: no protection at all.

        runConcurrently { counter++ }     // the classic non-atomic read-modify-write.

        // We can only guarantee we never *gained* updates; we almost always LOSE some.
        println("unsynchronized: got $counter, expected $expected (lost ${expected - counter})")
        assertTrue(counter <= expected, "impossible to exceed the expected total")
    }

    @Test
    fun `synchronized block is correct`() {
        var counter = 0
        val lock = Any()                  // any object can serve as an intrinsic lock/monitor.

        // Only one thread at a time may hold `lock`, so the read-modify-write is indivisible.
        runConcurrently { synchronized(lock) { counter++ } }

        assertEquals(expected, counter)
    }

    @Test
    fun `AtomicInteger is correct and lock-free`() {
        val counter = AtomicInteger(0)

        // incrementAndGet() is a single atomic CAS-backed operation — no lock, no lost updates.
        runConcurrently { counter.incrementAndGet() }

        assertEquals(expected, counter.get())
    }
}
