package other.concurrency

import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * LESSON 05 — Visibility, `@Volatile`, and happens-before.
 *
 * THE CORE IDEA
 * -------------
 * Concurrency is not only about ATOMICITY (is a read-modify-write indivisible?). It is
 * ALSO about VISIBILITY: once one thread writes a field, is another thread guaranteed to
 * ever *see* that write?
 *
 * The surprising answer under the Java Memory Model is: NOT automatically. For a plain
 * (non-volatile, non-synchronized) field, the runtime is free to:
 *   - keep the value in a CPU register or a per-core cache that another core never refreshes, and
 *   - let the JIT HOIST a repeated read out of a loop, turning
 *         while (running) { ... }
 *     into effectively
 *         if (running) { while (true) { ... } }
 *     because nothing in the loop tells the compiler `running` could change underneath it.
 * Either way a worker can spin FOREVER on a stale `true` even after another thread has
 * clearly written `false`. The write happened; it was just never made visible.
 *
 * THE FIX: `@Volatile` + happens-before
 * -------------------------------------
 * Marking the flag `@Volatile var running` gives two guarantees:
 *   1. Every read goes to main memory (no hoisting, no stale cache) — so the worker sees the write.
 *   2. HAPPENS-BEFORE: a write to a volatile field happens-before any subsequent read of that
 *      same field. Crucially this is a *barrier*, not a single-variable trick — everything the
 *      writer did BEFORE the volatile write is visible to a reader that observes the volatile
 *      value. So if main sets `payload = 42` and THEN `running = false` (volatile), a worker that
 *      sees `running == false` is guaranteed to also see `payload == 42`. This is called
 *      "volatile piggybacking": a plain field rides along on the volatile flag's barrier.
 *
 * `synchronized`, the `Atomic*` classes, and `volatile` ALL establish happens-before edges —
 * they provide visibility, not merely mutual exclusion. Locking is as much about publishing
 * writes as it is about keeping threads out of each other's way.
 *
 * VISIBILITY vs ATOMICITY (don't conflate them)
 * ---------------------------------------------
 * `@Volatile` gives visibility but NOT atomicity for compound operations. `volatileCounter++`
 * is still the same three-step read-modify-write from Lesson 01 and still loses updates under
 * contention — volatile makes each individual read/write visible, but does nothing to make the
 * *sequence* indivisible. For that you need `synchronized`, a lock, or an `Atomic*` (CAS).
 * The last test demonstrates exactly this trap.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 *   - `volatile flag reliably stops the worker...` is the DETERMINISTIC, ASSERTED demo of the
 *     correct pattern: join with a timeout, then assert the thread actually died and observed the
 *     piggybacked payload. This must pass every single run.
 *   - `plain flag hazard is observational only` explains the stale-read hazard. We do NOT prove it
 *     by hanging (JIT hoisting is *allowed*, not *guaranteed* — relying on it would be flaky). The
 *     worker is hard-bounded by an iteration cap AND interrupted, so the suite can never block; we
 *     only println what happened and assert loose, always-true facts.
 *   - `volatile does not give atomicity` shows volatile is the wrong tool for `++`.
 */
class L05VolatileVisibility {

    /**
     * Shared state for the correct demo. Both fields are volatile:
     *   - `running` is the stop signal the worker spins on.
     *   - `payload` is a plain value we hand off; it is volatile here too, but the lesson is that
     *     even a *non*-volatile field written before the volatile `running = false` would be
     *     visible via happens-before. We keep it volatile so the demo is unambiguous and portable.
     *
     * NOTE: `@Volatile` is only legal on a `var` PROPERTY inside a class — never on a local
     * variable. That is the reason this state lives in a class instead of inside the test method.
     */
    private class Worker {
        @Volatile
        var running = true
        @Volatile
        var payload = 0
        @Volatile
        var lastSeenPayload = -1
    }

    @Test
    fun `volatile flag reliably stops the worker and publishes the payload`() {
        val state = Worker()

        val worker = thread {
            // Spin until someone flips the volatile flag. Because `running` is volatile, this
            // read cannot be hoisted or served from a stale cache — the write below is seen.
            while (state.running) {
                // busy-wait; a real system would park/yield, but spinning keeps the lesson focused.
            }
            // We observed running == false. By happens-before, every write main made BEFORE that
            // volatile store (i.e. payload = 42) is now guaranteed visible to us.
            state.lastSeenPayload = state.payload
        }

        state.payload = 42        // (1) plain-looking hand-off, published by...
        state.running = false     // (2) ...the volatile write — this is the happens-before edge.

        worker.join(1_000)        // wait up to 1s; the correct version stops well within this.

        // Deterministic assertions: the loop terminated, and the payload was seen via piggybacking.
        assertFalse(worker.isAlive, "volatile flag must reliably stop the worker")
        assertEquals(42, state.lastSeenPayload, "payload written before the volatile flag must be visible")
    }

    /**
     * The HAZARD, made safe. If `running` were a plain `Boolean`, the worker's `while (running)`
     * could be hoisted/cached and spin forever after main sets it false. We cannot *force* that to
     * happen on demand (it is legal but not required), so proving it via a hang would be flaky and
     * could block the suite. Instead we bound the worker two ways so it ALWAYS terminates:
     *   - a hard iteration cap, and
     *   - an interrupt from the main thread.
     * Then we merely observe (println) whether the flip was seen. No flaky assertions.
     */
    @Test
    fun `plain flag hazard is observational only`() {
        // A holder so the worker can read a plain (non-volatile) flag by reference.
        class PlainFlag {
            var running = true
        }

        val flag = PlainFlag()

        val maxSpins = 50_000_000L          // hard cap: the loop cannot outlive this many iterations.
        val sawFlip = AtomicInteger(0)      // cross-thread signal for the println; correctness never depends on it.

        val worker = thread {
            var spins = 0L
            while (flag.running && spins < maxSpins && !Thread.currentThread().isInterrupted) {
                spins++
            }
            // If we exited because the flag was actually observed false, note it. Under a real
            // hoist this branch might never run — which is precisely the bug we're describing.
            if (!flag.running) sawFlip.set(1)
        }

        flag.running = false                // may or may not become visible to the worker in time.
        worker.join(500)                    // give it a short, bounded window.
        worker.interrupt()                  // belt-and-suspenders: guarantee it can never hang the suite.
        worker.join(500)

        // Observational only — DO NOT assert the flip was seen; that is exactly what's not guaranteed.
        println(
            if (sawFlip.get() == 1) "plain flag: worker observed the write (allowed, not guaranteed)"
            else "plain flag: worker did NOT observe the write in time (the visibility hazard)"
        )
        assertFalse(worker.isAlive, "the demo must never hang regardless of visibility")
    }

    @Test
    fun `volatile gives visibility but NOT atomicity for compound ops`() {
        // A volatile counter: each read and each write is visible, but `++` is still three steps.
        class Counter {
            @Volatile
            var value = 0
        }

        val counter = Counter()

        val threads = 8
        val perThread = 50_000
        val workers = (1..threads).map {
            thread { repeat(perThread) { counter.value++ } }  // read-modify-write: races despite volatile.
        }
        workers.forEach { it.join() }

        // The expected total is threads * perThread, but volatile does nothing to prevent lost
        // updates — so we almost always fall short. Loose assertion, same reasoning as Lesson 01.
        val expected = threads * perThread
        println("volatile ++: got ${counter.value}, expected $expected (volatile != atomic)")
        assertFalse(
            counter.value > expected,
            "volatile cannot make ++ exceed the total; it also cannot stop it losing updates",
        )
    }
}
