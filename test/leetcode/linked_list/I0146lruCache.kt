package leetcode.linked_list

import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * 146. LRU Cache  (https://leetcode.com/problems/lru-cache/)
 *
 * Design a data structure that follows the constraints of a Least Recently Used (LRU) cache.
 *
 * Implement the `LRUCache` class:
 * - `LRUCache(capacity: Int)` — initialize the cache with a positive size `capacity`.
 * - `get(key: Int): Int` — return the value of `key` if it exists, otherwise `-1`.
 * - `put(key: Int, value: Int)` — update the value of `key` if it exists; otherwise add the
 *   key-value pair. If the number of keys exceeds `capacity` as a result of this operation,
 *   evict the least recently used key.
 *
 * Both `get` and `put` must run in **O(1) average time complexity**.
 *
 * Constraints:
 * - 1 <= capacity <= 3000
 * - 0 <= key <= 10^4
 * - 0 <= value <= 10^5
 * - At most 2 * 10^5 calls will be made to `get` and `put`
 *
 * Note: this is a *design* problem, so the `testCases` / `args` / `expects` DSL does not apply —
 * a call sequence is exercised with plain JUnit assertions below. Add more tests freely.
 */
class I0146lruCache {

    /**
     * Minimal common surface so the shared test battery in [Solution] can run against every
     * implementation. [LRUCache] does not declare it — it is adapted in the test class instead, so
     * your code stays exactly as you wrote it.
     */
    private interface Cache {
        fun get(key: Int): Int
        fun put(key: Int, value: Int)
    }

    /**
     * ## Analysis of your solution — HashMap + doubly-linked sentinel ring
     *
     * **Verdict: correct and optimal.** Passes the whole battery, including
     * [Solution.reInsertExistingKeyKeepsListIntact] and the 300-sequence differential test
     * [Solution.matchesNaiveOracleOnRandomSequences]. The earlier "relink without unlinking" bug is
     * gone: every recency update now goes through [move2front], which is `extract` *then*
     * `insertAfterGuard`, so a node is never linked in two places at once.
     *
     * **Pattern: "hash map for lookup + doubly-linked list for order".** The canonical way to get two
     * different O(1) abilities at once. The map answers *where is key k* in O(1); a doubly-linked
     * list can splice a node out in O(1) *given the node* — which is exactly what the map hands you.
     * Singly linked would break it: unlinking needs the predecessor, and finding it is O(n). Order
     * convention here is front (`guard.next`) = MRU, back (`guard.prev`) = LRU, so the eviction
     * victim is always one hop from the sentinel.
     *
     * ### Time complexity — O(1) per operation
     * - [get]: one `HashMap` lookup + [move2front], which is 6 unconditional pointer writes. No loop.
     * - [put]: one lookup, then either 6 pointer writes (existing key) or 4 writes + one map insert +
     *   at most one map remove and 2 writes (new key). No loop.
     * - Over `m` calls: **O(m)** total. Strictly speaking O(1) *average*, not worst case — the map
     *   lookup degrades on collisions (Java 8+ treeifies a bin, so the pathological bound is
     *   O(log n), not O(n)). With `key <= 10^4` and `Integer.hashCode() == value`, collisions are
     *   spread by `HashMap`'s bit-mixing and never bite in practice.
     *
     * ### Space complexity — O(min(capacity, distinct keys))
     * One `HashMap` entry + one [Node] per cached key, plus the single [guard]. Iterative, so **no
     * recursion stack**. Nothing here is output space — it is all live state, which is the point of a
     * cache.
     *
     * ### Why it is sound
     * - **The sentinel ring removes every edge case.** [Node.prev]/[Node.next] start self-referential,
     *   and [guard] closes the list, so every node always has non-null neighbours. [extract] and
     *   [insertAfterGuard] are therefore branchless — no empty-list case, no single-element case, no
     *   `!!`. This is exactly why the pointer juggling has nothing left to get wrong.
     * - **Eviction happens after insertion, not before.** `index.size > capacity` is checked once the
     *   new node is already registered, so `size >= 2` whenever the branch fires and `guard.prev`
     *   cannot be [guard] itself. Even the out-of-constraints `capacity == 0` degenerates correctly:
     *   `size == 1 > 0`, `guard.prev` is the node just inserted, and it is immediately evicted.
     * - **The map is the single source of truth for size.** No separate counter to drift out of sync
     *   with the list — a classic source of bugs in hand-rolled caches.
     * - **`key` is a `val`.** `index.remove(lruNode.key)` reads the victim's own key, so it cannot
     *   remove the wrong entry after rewiring.
     * - **Updating an existing key evicts nothing**, and a successful [get] refreshes recency. Both
     *   are handled, and both are the cases most attempts miss.
     *
     * ### Nits worth knowing (none affect correctness here)
     * - **`data class Node` is a loaded gun.** It works *only* because `prev`/`next` are declared in
     *   the body, not the primary constructor — generated `equals`/`hashCode`/`toString` cover
     *   constructor properties only. Move `prev`/`next` into the constructor and all three recurse
     *   infinitely on a cyclic list (`StackOverflowError`). It also gives you structural equality on
     *   `(key, value)`, so two *different* nodes can compare equal, and a `copy()` silently yields a
     *   self-linked orphan. Nodes are only ever map *values* here, never keys or set members, so none
     *   of that is reachable — but a plain `private class` costs nothing and closes the door.
     * - **`?.let { } ?: …let { }` as an if/else.** Correct, but it is load-bearing on a *return type*
     *   rather than on logic: the first branch is skipped only because [move2front] returns `Unit`
     *   (non-null). If the last expression in that `let` ever became nullable, both branches would
     *   run and the key would be double-inserted. A plain `val existing = index[key]; if (…)` says
     *   the same thing with the safety in the control flow instead of in the types.
     * - **Visibility.** [Node], [insertAfterGuard], [extract] and [move2front] are public, so a caller
     *   can splice the list from outside and break the map/list invariant. `private` for all four.
     * - **[extract] leaves the removed node pointing at its old neighbours** ("node itself links
     *   remain"). Harmless for [move2front], which overwrites both. For eviction it means a discarded
     *   node still references live ones — a *loitering* reference. Irrelevant here (nothing holds the
     *   evicted node), but production caches null the pointers out precisely so one leaked reference
     *   cannot pin the whole ring.
     *
     * ### Alternatives
     * - **`LinkedHashMap(capacity, 0.75f, accessOrder = true)` + `removeEldestEntry`** — see
     *   [LRUCacheLinkedHashMap]. Same O(1)/O(capacity); it *is* this structure, already spliced
     *   together in the JDK. What you would ship; not what an interviewer wants.
     * - **Array-based intrusive list.** Replace object nodes with parallel `IntArray`s of size
     *   `capacity` (`prevIdx`, `nextIdx`, `keys`, `values`) and store `key -> slot` in the map. Same
     *   asymptotics, materially better constants: zero per-entry allocation, no GC pressure, and the
     *   pointer chase becomes contiguous index arithmetic instead of scattered heap reads. This is how
     *   real buffer pools are written.
     * - **Timestamp + priority queue.** Stamp each access with a counter and pop the min. O(log n) per
     *   op and it accumulates stale entries — strictly worse. A useful thing to be able to reject.
     * - **Approximate LRU** (CLOCK / second-chance, or sampled eviction): drop the exact ordering, keep
     *   one reference bit per entry. O(1) amortised with a fraction of the metadata and no list
     *   maintenance on reads — the trade that matters once reads dominate (below).
     *
     * No asymptotically better exact solution exists: both operations already touch a constant number
     * of words, and any correct cache must at minimum hash the key.
     *
     * ### Parallelism — genuinely not applicable to this problem
     * The op sequence is the *specification*: `get(1)` after `put(3,3)` means something different than
     * before it, so calls cannot be reordered or batched. Every single operation — including reads —
     * writes to the shared recency order, so there is no read-only phase to fan out and no independent
     * subproblem to split. Serial fraction ≈ 1, so Amdahl's law caps the speedup at ≈ 1×. Even the
     * per-op work (one hash + six stores) is far below thread-handoff cost. This is the honest answer,
     * and the interesting part is *why*: LRU is a mutable-shared-state structure, the opposite of the
     * embarrassingly-parallel map/reduce shape.
     *
     * What production does instead is **weaken the guarantee to buy concurrency**:
     * - **Shard by key hash** into N independent caches, each with its own lock and its own LRU list.
     *   Contention drops ~N×; global eviction becomes approximate (a hot shard evicts entries a truly
     *   global LRU would have kept).
     * - **Buffer the reads.** Caffeine's trick: a `get` records the access into a per-stripe lock-free
     *   ring buffer and returns immediately — it never touches the eviction list. A single thread
     *   drains the buffers and replays them against the policy later. Reads become effectively
     *   contention-free, and recency lags slightly. This is the standard resolution of "every read is
     *   a write" and worth remembering as a design pattern.
     *
     * ### Real-world notes
     * This exact structure is everywhere: OS page caches, DB buffer pools, CPU cache replacement,
     * HTTP/CDN caches, ORM and connection caches, memoisation layers. But the interview version is a
     * stripped-down model, and every difference matters at scale:
     * - **LRU is not scan-resistant.** One sequential scan of a large table touches every key once and
     *   flushes the entire working set — pure LRU's defining weakness. Hence ARC, 2Q, LIRS and
     *   **W-TinyLFU** (Caffeine's policy: a tiny frequency sketch admits a candidate only if it is
     *   likely hotter than the victim), which reach much higher hit ratios on real traces.
     * - **Nobody runs exact LRU at scale.** Redis's `allkeys-lru` samples ~5 random keys and evicts the
     *   oldest of those, because the per-key linked-list pointers cost more RAM than the accuracy is
     *   worth. Postgres uses a clock sweep. CPUs use pseudo-LRU trees. The "optimal" answer loses to
     *   the cheap approximation once metadata size and cache-line behaviour dominate.
     * - **The real constraints are the ones LeetCode omits:** thread safety, size-based rather than
     *   count-based bounds (weigh entries by bytes), TTL/expiry alongside recency, loading caches with
     *   single-flight so a miss storm does not stampede the backend, and hit-ratio metrics. In Kotlin
     *   on the JVM, reach for **Caffeine** (or `LinkedHashMap` for a single-threaded local cache) and
     *   spend your effort on eviction *policy* and sizing, not on the pointer work.
     */
    class LRUCache(private val capacity: Int) {

        data class Node(val key: Int, var value: Int) {
            var prev: Node = this
            var next: Node = this
        }

        private val index = HashMap<Int, Node>()
        private val guard = Node(0, 0)

        fun get(key: Int): Int {
            return index[key]?.let { foundNode ->
                move2front(foundNode)
                foundNode.value
            } ?: -1
        }

        fun put(key: Int, value: Int) {
            index[key]?.let { existingNode ->
                existingNode.value = value
                move2front(existingNode)
            } ?: Node(key, value).let { newNode ->
                index[newNode.key] = newNode
                insertAfterGuard(newNode)
                if (index.size > capacity) guard.prev.let { lruNode ->
                    index.remove(lruNode.key)
                    extract(lruNode)
                }
            }
        }

        fun insertAfterGuard(node: Node) {
            //node links
            node.prev = guard
            node.next = guard.next
            //guard.next links
            guard.next.prev = node
            //guard
            guard.next = node
        }

        fun extract(node: Node) {
            //unlink node from ring
            node.prev.next = node.next
            node.next.prev = node.prev
            //node itself links remain
        }

        fun move2front(node: Node) {
            extract(node)
            insertAfterGuard(node)
        }

    }


    /**
     * ## Reference solution — HashMap + doubly-linked list (sentinel ring)
     *
     * **Restated.** Build a fixed-size cache that answers `get`/`put` in O(1) *and* remembers the
     * order in which keys were last touched, so that when it overflows it can throw out the key
     * nobody has used for the longest time.
     *
     * **Pattern: "hash map for lookup + linked list for order".** This is the canonical way to make
     * a data structure that needs *two* different O(1) abilities at once. Ask what each half buys:
     * - A `HashMap` gives O(1) *find by key*, but a hash map has no order.
     * - A **doubly**-linked list gives O(1) *insert/remove at a known node*, and it holds order —
     *   but finding a node in it is O(n).
     *
     * Neither alone is enough, so use both and let them point at each other: the map stores
     * `key -> node`, and the node lives in the list. That is the whole trick — the map hands you the
     * node in O(1), which is exactly the precondition the list needs to unlink in O(1). The list
     * must be *doubly* linked precisely because unlinking a node requires knowing its predecessor;
     * with a singly-linked list you would have to scan for it and lose O(1).
     *
     * Order convention here: front (`guard.next`) = most recently used, back (`guard.prev`) = least
     * recently used, i.e. the eviction victim is always one hop away.
     *
     * **Approach.**
     * 1. `get(key)`: miss -> `-1`. Hit -> move that node to the front (it is now the MRU) and
     *    return its value.
     * 2. `put(key, value)`: if the key exists, overwrite the value and move the node to the front —
     *    an update counts as a use, and **nothing is evicted** (the size did not grow). Otherwise
     *    create a node, link it at the front, register it in the map, and *then* — if the map now
     *    exceeds `capacity` — unlink `guard.prev` and remove that key from the map.
     * 3. Every mutation is "unlink, then link at front", so both halves stay in lockstep.
     *
     * **The sentinel ring.** Instead of nullable `head`/`tail` fields, a single dummy node [guard]
     * closes the list into a ring (`prev`/`next` start pointing at itself). Then *every* real node
     * always has a non-null neighbour, so `unlink`/`linkAfterGuard` are four unconditional pointer
     * writes — no empty-list case, no single-element case, no null checks, no `!!`. This is the
     * standard cure for exactly the class of edge-case bug that bit the first attempt; the pointer
     * juggling stops having special cases to get wrong.
     *
     * **Complexity.** `get` and `put` are O(1) — one hash lookup plus a constant number of pointer
     * writes; no loops anywhere. Space is O(capacity): one map entry and one node per cached key.
     *
     * **Common pitfalls.**
     * - Re-inserting an existing key **without unlinking it first** — always unlink before relinking.
     * - Evicting on an update of an existing key, or checking the size *before* inserting: only a
     *   genuinely new key can overflow the cache.
     * - Forgetting that a successful `get` also refreshes recency — a read is a use.
     * - Removing the node from the list but not from the map (or vice versa) — the map would grow
     *   without bound and later return values for evicted keys.
     * - Evicting by `node.key` after the node was already rewired: capture `lru.key` from the victim
     *   node, never from a pointer you have since moved. (This is why [Node.key] is a `val` here.)
     * - Using a singly-linked list, or `LinkedList`/`ArrayList` + `indexOf`/`remove` — that is O(n)
     *   per operation and fails the O(1) requirement.
     */
    class LRUCacheReference(private val capacity: Int) : Cache {

        private class Node(val key: Int, var value: Int) {
            // Self-linked on creation, so an unlinked node is always a valid 1-element ring.
            var prev: Node = this
            var next: Node = this
        }

        private val map = HashMap<Int, Node>()

        /** Dummy node closing the list into a ring: `guard.next` is the MRU, `guard.prev` the LRU. */
        private val guard = Node(key = 0, value = 0)

        override fun get(key: Int): Int {
            val node = map[key] ?: return -1
            moveToFront(node)
            return node.value
        }

        override fun put(key: Int, value: Int) {
            val existing = map[key]
            if (existing != null) {
                existing.value = value
                moveToFront(existing) // an update is a use; size unchanged, so nothing is evicted
                return
            }

            val node = Node(key, value)
            map[key] = node
            linkAfterGuard(node)

            if (map.size > capacity) {
                val lru = guard.prev // one hop from the sentinel; safe because size >= 2 here
                unlink(lru)
                map.remove(lru.key)
            }
        }

        private fun unlink(node: Node) {
            node.prev.next = node.next
            node.next.prev = node.prev
        }

        private fun linkAfterGuard(node: Node) {
            node.prev = guard
            node.next = guard.next
            guard.next.prev = node
            guard.next = node
        }

        private fun moveToFront(node: Node) {
            unlink(node)
            linkAfterGuard(node)
        }
    }

    /**
     * ## Reference solution 2 — what you would actually write on the JVM
     *
     * `LinkedHashMap` *is* a hash map spliced into a doubly-linked list — the exact structure built
     * by hand above. Constructed with `accessOrder = true` it re-links an entry to the end on every
     * `get`, and overriding [removeEldestEntry] turns it into an LRU cache in one expression.
     *
     * Worth knowing for two reasons: in an interview it shows you recognise the pattern in the
     * standard library, and in production this is the version to reach for. Interviewers usually
     * still want the hand-rolled list — the point of the exercise is the pointer work — so treat
     * this as the epilogue, not the answer.
     *
     * Complexity is the same: O(1) amortised per operation, O(capacity) space.
     */
    class LRUCacheLinkedHashMap(private val capacity: Int) : Cache {

        private val map = object : LinkedHashMap<Int, Int>(capacity, 0.75f, /* accessOrder = */ true) {
            override fun removeEldestEntry(eldest: Map.Entry<Int, Int>): Boolean = size > capacity
        }

        override fun get(key: Int): Int = map[key] ?: -1

        override fun put(key: Int, value: Int) {
            map[key] = value
        }
    }

    /**
     * One shared battery, run against **every** implementation — yours and both references.
     *
     * Each `@Test` describes a behaviour once and [forEachImpl] replays it on a freshly built cache
     * per implementation, so a case can never drift between them. Failures are collected rather than
     * thrown on the first one: the message names *which* implementations broke, which makes the
     * difference between "my logic is wrong" and "the case itself is wrong" obvious at a glance.
     *
     * [LRUCache] is wired in through [asCache] — a thin adapter, so your class is untouched.
     *
     * Note [reInsertExistingKeyKeepsListIntact] and [matchesNaiveOracleOnRandomSequences]: those are
     * the ones that pinned down the original relink-without-unlink bug in [LRUCache]. All three
     * implementations now pass the full battery.
     */
    @Nested
    inner class Solution {

        private val implementations: List<Pair<String, (Int) -> Cache>> = listOf(
            "yours (LRUCache)" to { capacity: Int -> LRUCache(capacity).asCache() },
            "reference (linked list)" to { capacity: Int -> LRUCacheReference(capacity) },
            "reference (LinkedHashMap)" to { capacity: Int -> LRUCacheLinkedHashMap(capacity) },
        )

        /** Adapts [LRUCache] to [Cache] without modifying it — it just happens to have the shape. */
        private fun LRUCache.asCache(): Cache = object : Cache {
            override fun get(key: Int): Int = this@asCache.get(key)
            override fun put(key: Int, value: Int) = this@asCache.put(key, value)
        }

        /** Runs [block] on a fresh cache from every implementation; reports all failures together. */
        private fun forEachImpl(capacity: Int, block: (Cache) -> Unit) {
            val failures = implementations.mapNotNull { (name, factory) ->
                try {
                    block(factory(capacity))
                    null
                } catch (e: Throwable) {
                    "$name failed: ${e.message}"
                }
            }
            if (failures.isNotEmpty()) fail(failures.joinToString("\n"))
        }

        /**
         * Official example:
         * ops:  ["LRUCache","put","put","get","put","get","put","get","get","get"]
         * args: [[2],[1,1],[2,2],[1],[3,3],[2],[4,4],[1],[3],[4]]
         * out:  [null,null,null,1,null,-1,null,-1,3,4]
         */
        @Test
        fun example() = forEachImpl(2) { cache ->
            cache.put(1, 1)
            cache.put(2, 2)
            assertEquals(1, cache.get(1))
            cache.put(3, 3) // evicts key 2 (LRU)
            assertEquals(-1, cache.get(2))
            cache.put(4, 4) // evicts key 1
            assertEquals(-1, cache.get(1))
            assertEquals(3, cache.get(3))
            assertEquals(4, cache.get(4))
        }

        @Test
        fun case2() = forEachImpl(2) { cache ->
            cache.put(2, 1)
            cache.put(1, 1)
            cache.put(2, 3)
            cache.put(4, 1)
            assertEquals(-1, cache.get(1))
            assertEquals(3, cache.get(2))
        }

        @Test
        fun missingKeyReturnsMinusOne() = forEachImpl(1) { cache ->
            assertEquals(-1, cache.get(42))
        }

        @Test
        fun updateExistingKeyDoesNotEvict() = forEachImpl(2) { cache ->
            cache.put(1, 1)
            cache.put(2, 2)
            cache.put(1, 10)
            assertEquals(2, cache.get(2))
            assertEquals(10, cache.get(1))
        }

        @Test
        fun getRefreshesRecency() = forEachImpl(2) { cache ->
            cache.put(1, 1)
            cache.put(2, 2)
            assertEquals(1, cache.get(1)) // 1 becomes MRU, 2 becomes LRU
            cache.put(3, 3)
            assertEquals(1, cache.get(1))
            assertEquals(-1, cache.get(2))
        }

        @Test
        fun capacityOne() = forEachImpl(1) { cache ->
            cache.put(1, 1)
            assertEquals(1, cache.get(1))
            cache.put(2, 2)
            assertEquals(-1, cache.get(1))
            assertEquals(2, cache.get(2))
        }

        @Test
        fun failedGetDoesNotAffectRecency() = forEachImpl(2) { cache ->
            cache.put(1, 1)
            cache.put(2, 2)
            assertEquals(-1, cache.get(3)) // miss
            cache.put(3, 3) // 1 is still the LRU -> evicted
            assertEquals(-1, cache.get(1))
            assertEquals(2, cache.get(2))
            assertEquals(3, cache.get(3))
        }

        /**
         * Re-inserting a key that is already cached must **unlink it before relinking**, otherwise
         * its old neighbours keep pointing at it and the list grows a cycle. The corruption is
         * silent at first — it only shows up a few evictions later, as the wrong key disappearing.
         */
        @Test
        fun reInsertExistingKeyKeepsListIntact() = forEachImpl(3) { cache ->
            cache.put(1, 11)
            cache.put(2, 22)
            cache.put(3, 33)
            cache.put(2, 22) // present already: refresh recency, evict nothing -> order 1, 3, 2
            cache.put(4, 44) // evicts 1 (LRU)
            cache.put(1, 11) // evicts 3 (LRU)
            assertEquals(-1, cache.get(3), "key 3 was the LRU and must have been evicted")
            assertEquals(22, cache.get(2))
            assertEquals(44, cache.get(4))
            assertEquals(11, cache.get(1))
        }

        /** Eviction order must follow *use*, not insertion, over a long run. */
        @Test
        fun evictsInUseOrderOverLongRun() = forEachImpl(3) { cache ->
            (1..3).forEach { cache.put(it, it * 10) }
            assertEquals(10, cache.get(1)) // order: 2, 3, 1
            assertEquals(20, cache.get(2)) // order: 3, 1, 2
            cache.put(4, 40) // 3 is the LRU -> evicted; order: 1, 2, 4
            assertEquals(-1, cache.get(3))
            assertEquals(10, cache.get(1)) // order: 2, 4, 1
            cache.put(5, 50) // 2 is the LRU -> evicted; order: 4, 1, 5
            assertEquals(-1, cache.get(2))
            assertEquals(40, cache.get(4))
            assertEquals(10, cache.get(1))
            assertEquals(50, cache.get(5))
        }

        /**
         * Differential test against a deliberately naive O(n) LRU that is obviously correct.
         * Random op sequences catch the pointer-bookkeeping bugs that hand-picked cases miss, and
         * the failure message replays the exact (short) sequence that broke — a ready-made repro.
         *
         * The same seeds drive every implementation, so the sequences are identical across them.
         */
        @Test
        fun matchesNaiveOracleOnRandomSequences() {
            val random = Random(20260729)
            val rounds = List(300) { 1 + random.nextInt(5) to random.nextLong() }

            val failures = implementations.mapNotNull { (name, factory) ->
                val firstFailure = rounds.firstNotNullOfOrNull { (capacity, seed) ->
                    runCatching { replayAgainstOracle(factory(capacity), capacity, seed) }
                        .exceptionOrNull()?.message
                }
                firstFailure?.let { "$name failed: $it" }
            }
            if (failures.isNotEmpty()) fail(failures.joinToString("\n"))
        }

        /** Replays one pseudo-random op sequence on [cache] and the oracle, asserting every `get`. */
        private fun replayAgainstOracle(cache: Cache, capacity: Int, seed: Long) {
            val oracle = NaiveLru(capacity)
            val ops = Random(seed)
            val log = StringBuilder("LRUCache($capacity)")
            repeat(60) {
                val key = 1 + ops.nextInt(7)
                if (ops.nextBoolean()) {
                    log.append("; get($key)")
                    assertEquals(oracle.get(key), cache.get(key), log.toString())
                } else {
                    val value = ops.nextInt(100)
                    log.append("; put($key,$value)")
                    oracle.put(key, value)
                    cache.put(key, value)
                }
            }
        }
    }

    /** Obviously-correct O(n) LRU used only as a test oracle: index 0 is the LRU end. */
    private class NaiveLru(private val capacity: Int) {
        private val entries = ArrayList<Pair<Int, Int>>()

        fun get(key: Int): Int {
            val i = entries.indexOfFirst { it.first == key }
            if (i < 0) return -1
            val entry = entries.removeAt(i)
            entries.add(entry) // touched -> most recently used
            return entry.second
        }

        fun put(key: Int, value: Int) {
            val i = entries.indexOfFirst { it.first == key }
            if (i >= 0) entries.removeAt(i)
            entries.add(key to value)
            if (entries.size > capacity) entries.removeAt(0)
        }
    }
}
