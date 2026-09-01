package cases.cache

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Concurrency tests for [LruCache] — the coroutine analogue of `other.concurrency.L01RaceConditions`
 * / `K04CoroutineMutex`, applied to a real data structure instead of a counter.
 *
 * THE ONE TRAP TO KNOW ABOUT
 * --------------------------
 * `runTest { launch { ... } }` **cannot** expose a data race. `runTest` drives everything on a
 * single-threaded `TestScheduler`, and coroutines on one thread can only interleave at
 * *suspension points*. Every `LruCache` method is an ordinary (non-`suspend`) function, so once
 * `add()` starts it runs to completion — there is no window for another coroutine to observe a
 * half-relinked list. `runTest` is for testing *timing* (delays, timeouts, virtual time), not
 * thread safety. See [runTest cannot expose a data race], which passes deterministically against
 * a cache that is definitely not thread-safe.
 *
 * To get real parallelism you need a multi-threaded dispatcher: `Dispatchers.Default` has one
 * worker thread per CPU core. That is what [hammer] uses.
 *
 * THE TEST DESIGN
 * ---------------
 * Each worker owns a **disjoint** slice of the keyspace and inserts every key in its slice, so:
 *   - the workers never fight over a *logical* key — every race is over the *shared structure*
 *     (the `HashMap` and the intrusive doubly-linked list);
 *   - `capacity == keyspace`, so a correct cache **never evicts** and the expected final state is
 *     exact: all [keyspace] keys readable. Every missing key is proof of corruption, with no
 *     LRU-ordering reasoning needed. That's the trick for making a "did concurrency break it?"
 *     assertion exact instead of hand-wavy.
 *
 * Following the convention in `other/concurrency/CLAUDE.md`: the **broken** demo asserts only
 * loose, always-true invariants and `println`s what it observed (how much corruption you get is
 * nondeterministic — asserting "this always breaks" would itself be flaky), while the **guarded**
 * version asserts exact equality on every run.
 *
 * NOTE ON HANGS: there is no `withTimeout` here on purpose. Cancellation is cooperative — it only
 * takes effect at suspension points — so it could not interrupt a spin inside a non-`suspend`
 * `add()` anyway. If you ever hit a real infinite loop in a corrupted list, the honest tool is a
 * separate thread plus `join(timeout)`, the way `L02LocksAndConditions` does it.
 */
class LruCacheConcurrencyTest {

    private val workers = 64
    private val keysPerWorker = 64
    private val keyspace = workers * keysPerWorker   // 4096
    private val passes = 4                           // re-insert each key this many times

    /** Keys owned by [worker] — disjoint across workers. */
    private fun keysOf(worker: Int) = (worker * keysPerWorker) until ((worker + 1) * keysPerWorker)

    /** With `capacity == keyspace` a correct cache evicts nothing, so this must equal [keyspace]. */
    private fun survivors(cache: LruCache<Int, Int>) = (0 until keyspace).count { cache.get(it) != null }

    /**
     * Run every worker concurrently on [Dispatchers.Default] — real threads, real parallelism.
     * When [guard] is non-null every cache call is serialized through it.
     *
     * Returns the number of operations that threw. Failures are counted per-op rather than
     * propagated because structured concurrency would otherwise let the first exception cancel
     * every sibling, and we want to see the whole picture. `HashMap.computeIfAbsent` is
     * fail-fast — a concurrent modification typically surfaces as `ConcurrentModificationException`.
     */
    private fun hammer(cache: LruCache<Int, Int>, guard: Mutex? = null): Int = runBlocking {
        val failures = AtomicInteger()
        withContext(Dispatchers.Default) {
            (0 until workers).map { worker ->
                launch {
                    repeat(passes) {
                        for (key in keysOf(worker)) {
                            try {
                                if (guard == null) cache.add(key, key) else guard.withLock { cache.add(key, key) }
                            } catch (e: CancellationException) {
                                throw e                       // never swallow cancellation
                            } catch (e: Throwable) {
                                failures.incrementAndGet()
                            }
                        }
                    }
                }
            }.joinAll()
        }
        failures.get()
    }

    @Test
    fun `runTest cannot expose a data race`() = runTest {
        val cache = LruCache<Int, Int>(keyspace.toUInt())

        (0 until workers).map { worker ->
            launch { for (key in keysOf(worker)) cache.add(key, key) }
        }.joinAll()

        // Passes on EVERY run despite the cache having no synchronization at all — one thread,
        // and no suspension point inside add(). This assertion is the trap, not the proof.
        assertEquals(keyspace, survivors(cache), "single-threaded execution cannot corrupt the cache")
    }

    @Test
    fun `unguarded cache is not thread-safe`() {
        val cache = LruCache<Int, Int>(keyspace.toUInt())

        val failures = hammer(cache)
        val survived = survivors(cache)

        println(
            "unguarded: $survived/$keyspace keys survived (lost ${keyspace - survived}), " +
                    "$failures operations threw"
        )
        // Loose invariants only: we can never gain keys, but how many we lose is scheduling-dependent.
        assertTrue(survived <= keyspace, "impossible to hold more keys than were ever inserted")
    }

    @Test
    fun `mutex makes the cache safe`() {
        val cache = LruCache<Int, Int>(keyspace.toUInt())
        val guard = Mutex()

        val failures = hammer(cache, guard)

        // Exact, deterministic: mutual exclusion means every add() sees a consistent structure,
        // and joinAll() gives the happens-before edge that makes those writes visible here.
        assertEquals(0, failures, "no operation should throw under mutual exclusion")
        assertEquals(keyspace, survivors(cache), "no key may be lost under mutual exclusion")
    }

    @Test
    fun `unguarded cache can break its capacity bound`() {
        val capacity = keyspace / 8
        val cache = LruCache<Int, Int>(capacity.toUInt())

        hammer(cache)
        val survived = survivors(cache)

        // A cache that silently exceeds its capacity is a memory leak, not just a wrong answer.
        println("unguarded with capacity=$capacity: $survived keys retained" + if (survived > capacity) "  <-- BOUND VIOLATED" else "")
        assertTrue(survived <= keyspace, "impossible to hold more keys than were ever inserted")
    }
}
