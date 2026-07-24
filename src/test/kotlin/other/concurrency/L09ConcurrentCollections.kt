package other.concurrency

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * LESSON 09 — Concurrent collections & ThreadLocal.
 *
 * THE CORE IDEA
 * -------------
 * A plain `HashMap`/`ArrayList` has ZERO internal synchronization. If several threads
 * write to the same one at the same time, the writes race exactly like `counter++` did in
 * Lesson 01: two threads can land in the same bucket, and one entry silently vanishes — or,
 * on resize, the internal array can be left in a corrupt state. The fix is NOT to wrap every
 * call in `synchronized` (that serializes all access and kills throughput). Instead the JDK
 * ships purpose-built thread-safe collections that allow real concurrency:
 *
 *   - `ConcurrentHashMap`   : lock-striped map. Reads are lock-free; writes lock only a small
 *                              slice of the table, so many threads write in parallel safely.
 *                              Also exposes ATOMIC compound ops: `merge`, `compute`,
 *                              `computeIfAbsent` — the whole read-modify-write happens as one
 *                              indivisible step (this is the part people get wrong).
 *   - `CopyOnWriteArrayList`: every mutation copies the entire backing array. Reads and
 *                              iteration touch an immutable snapshot — no locks, no
 *                              ConcurrentModificationException. Great for read-mostly data.
 *   - `ThreadLocal<T>`      : NOT a collection — a per-thread box. Each thread that touches it
 *                              gets its OWN value, so a non-thread-safe object (StringBuilder,
 *                              SimpleDateFormat, a scratch buffer) can be used without sharing.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * Same asymmetry as Lesson 01. The buggy plain-`HashMap` demo can only assert a *loose*
 * invariant (`size <= inserted`) and print the loss, because how much is lost depends on
 * nondeterministic scheduling — asserting it is *always* short would itself be flaky. Every
 * thread-safe version asserts EXACT results, deterministically, on every run. That contrast
 * is the lesson: correctness under concurrency must never depend on timing.
 */
class L09ConcurrentCollections {

    private val threads = 8
    private val perThread = 10_000
    private val totalInserted = threads * perThread

    /**
     * Run [work] once on each of [threads] threads, passing that thread's index (0-based).
     * A [CountDownLatch] is the starting gun: every worker blocks until it fires, so they all
     * begin hammering the shared collection at (roughly) the same instant — this maximizes
     * contention and makes races surface instead of hiding behind lucky scheduling.
     */
    private fun runConcurrently(work: (threadIndex: Int) -> Unit) {
        val startGun = CountDownLatch(1)
        val workers = (0 until threads).map { i ->
            thread {
                startGun.await()          // all workers block here...
                work(i)
            }
        }
        startGun.countDown()              // ...and are released together.
        workers.forEach { it.join() }     // wait for all to finish before reading the result.
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Plain HashMap under concurrent writes is UNSAFE.
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `plain HashMap loses entries under concurrent writes`() {
        val map = HashMap<Int, Int>()     // no synchronization whatsoever.

        // Every thread writes a DISJOINT set of keys (i*perThread .. i*perThread+perThread-1),
        // so with a *safe* map the final size would be exactly totalInserted — no key collisions.
        // Because HashMap is unsafe, concurrent puts race on shared buckets/resize and some
        // entries are dropped, so we usually end up SHORT.
        runConcurrently { i ->
            val base = i * perThread
            for (k in base until base + perThread) {
                map[k] = k
            }
        }

        // We can only guarantee we never *invented* entries; we typically lose some.
        val lost = totalInserted - map.size
        println("plain HashMap: size=${map.size}, expected=$totalInserted (lost $lost)")
        assertTrue(map.size <= totalInserted, "impossible to hold more distinct keys than were inserted")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. ConcurrentHashMap — the fix, plus its ATOMIC compound operations.
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `ConcurrentHashMap keeps every entry`() {
        val map = ConcurrentHashMap<Int, Int>()   // same workload, safe map.

        runConcurrently { i ->
            val base = i * perThread
            for (k in base until base + perThread) {
                map[k] = k
            }
        }

        // Exact, every run: no entry is ever lost.
        assertEquals(totalInserted, map.size)
    }

    @Test
    fun `merge makes a concurrent frequency counter atomic`() {
        // Now the interesting case: threads updating the SAME keys. This is a word/frequency
        // counter — every thread increments the same handful of buckets.
        val words = listOf("red", "green", "blue")
        val counts = ConcurrentHashMap<String, Int>()

        // WHY NOT `counts[w] = (counts[w] ?: 0) + 1`?
        // On a ConcurrentHashMap the get and the put are each individually atomic — but the PAIR
        // is not. Two threads can both read 41, both compute 42, both write 42: one increment is
        // lost. It is the exact same lost-update race as `counter++`; a thread-safe container does
        // NOT make a read-then-write sequence atomic.
        //
        // `merge(key, delta) { old, given -> old + given }` performs the whole read-modify-write
        // as ONE indivisible operation under the bucket lock: if the key is absent it stores
        // `delta`, otherwise it stores `remap(old, delta)`. No interleaving is possible.
        runConcurrently {
            repeat(perThread) {
                for (w in words) {
                    counts.merge(w, 1) { old, given -> old + given }
                }
            }
        }

        // Each word was incremented once per iteration, per thread — exact totals every run.
        val expectedPerWord = threads * perThread
        for (w in words) {
            assertEquals(expectedPerWord, counts[w], "count for '$w'")
        }
        assertEquals(words.size * expectedPerWord, counts.values.sum())
    }

    @Test
    fun `compute and computeIfAbsent are also atomic`() {
        val counts = ConcurrentHashMap<String, Int>()

        // `compute` is the general form: it hands you the current value (null if absent) and
        // stores whatever you return, atomically. `merge` is just the common "combine with a
        // default" shorthand for it.
        runConcurrently {
            repeat(perThread) {
                counts.compute("hits") { _, old -> (old ?: 0) + 1 }
            }
        }

        assertEquals(threads * perThread, counts["hits"])

        // `computeIfAbsent` runs the lambda AT MOST ONCE per key even under a stampede — the
        // classic way to lazily build a shared value (e.g. a cache entry) exactly one time.
        val expensiveInits = ConcurrentHashMap<String, Int>()
        val callCount = ConcurrentHashMap<String, Int>()
        runConcurrently {
            expensiveInits.computeIfAbsent("config") {
                callCount.merge("config", 1) { a, b -> a + b }   // count how many times the init runs
                42
            }
        }
        assertEquals(42, expensiveInits["config"])
        assertEquals(1, callCount["config"], "initializer runs exactly once despite the stampede")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. CopyOnWriteArrayList — cheap reads, expensive writes; safe iteration.
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `CopyOnWriteArrayList is safe for concurrent add and iteration`() {
        // TRADE-OFF: every mutating call (add/set/remove) copies the ENTIRE backing array under a
        // lock — O(n) per write. In exchange, reads and iteration are lock-free and operate on an
        // immutable snapshot. That makes it ideal for read-mostly, rarely-written data such as an
        // event-listener list: iterating to fire listeners never sees a half-mutated array and
        // never throws ConcurrentModificationException, even if a listener adds/removes during the
        // dispatch.
        val list = CopyOnWriteArrayList<Int>()

        runConcurrently { i ->
            val base = i * perThread
            for (k in base until base + perThread) {
                list.add(k)
            }
        }

        // Every add landed; nothing lost, nothing duplicated.
        assertEquals(totalInserted, list.size)
        assertEquals((0 until totalInserted).toSet(), list.toSet())

        // Iterating while another thread mutates does not blow up: the iterator holds its own
        // snapshot taken at creation time.
        val snapshotSize = list.size
        var seen = 0
        val iterator = list.iterator()
        list.add(999_999)                 // mutate mid-iteration — iterator won't see it, won't throw.
        while (iterator.hasNext()) {
            iterator.next(); seen++
        }
        assertEquals(snapshotSize, seen, "iterator reflects the snapshot from when it was created")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. ThreadLocal — give each thread its own instance of a non-thread-safe object.
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `ThreadLocal gives each thread its own isolated value`() {
        // A StringBuilder is NOT thread-safe: sharing one across threads corrupts it. Instead of
        // synchronizing, we give each thread its OWN via ThreadLocal.withInitial — the initializer
        // runs once per thread, the first time that thread calls get().
        val scratch = ThreadLocal.withInitial { StringBuilder() }

        // Each thread appends only its own index, many times, into its OWN builder. Because no
        // builder is shared, there is no interference — the result is deterministic per thread.
        val results = ConcurrentHashMap<Int, String>()
        runConcurrently { i ->
            val sb = scratch.get()        // this thread's private StringBuilder.
            repeat(5) { sb.append(i) }
            results[i] = sb.toString()
            scratch.remove()              // hygiene: see the leak caveat below.
        }

        // Each thread saw only what IT wrote — "iiiii" for thread i, never a mix of indices.
        for (i in 0 until threads) {
            assertEquals(i.toString().repeat(5), results[i], "thread $i's private buffer")
        }
    }

    /*
     * CANONICAL USE & THE LEAK CAVEAT
     * -------------------------------
     * The textbook use of ThreadLocal is caching a non-thread-safe helper that is expensive to
     * build and used a lot — e.g. `SimpleDateFormat` (famously not thread-safe) or a per-request
     * "context" object — so each thread reuses its own copy instead of allocating or locking:
     *
     *     val fmt = ThreadLocal.withInitial { SimpleDateFormat("yyyy-MM-dd") }
     *     fmt.get().format(date)   // safe: every thread formats with its own instance.
     *
     * CAVEAT with thread POOLS: pool threads are long-lived and reused across unrelated tasks. A
     * value you stash in a ThreadLocal outlives your task and stays bound to that thread — a
     * memory leak, and worse, the NEXT task on that thread inherits your stale value. Always call
     * `remove()` when the task is done (as the test does above), typically in a `finally` block.
     */
}
