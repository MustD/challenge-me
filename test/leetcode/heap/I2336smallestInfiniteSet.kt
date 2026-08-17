package leetcode.heap

import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * 2336. Smallest Number in Infinite Set  (https://leetcode.com/problems/smallest-number-in-infinite-set/)
 *
 * You have a set which initially contains **all** positive integers `[1, 2, 3, 4, 5, ...]`.
 *
 * Implement the `SmallestInfiniteSet` class:
 * - `SmallestInfiniteSet()` — initialize the object to contain all positive integers.
 * - `popSmallest(): Int` — remove and return the smallest integer currently in the set.
 * - `addBack(num: Int)` — add the positive integer `num` back into the set, **if it is not already
 *   present**. (So `addBack` of a number that was never popped, or popped and already added back,
 *   is a no-op.)
 *
 * Constraints:
 * - 1 <= num <= 1000
 * - At most 1000 calls in total will be made to `popSmallest` and `addBack`
 *
 * Note: this is a *design* problem, so the `testCases` / `args` / `expects` DSL does not apply —
 * a call sequence is exercised with plain JUnit assertions below. Add more tests freely.
 *
 * Things worth thinking about before you start (no answers here — that's the exercise):
 * - The set is conceptually infinite, so you can never materialise it. What is the *smallest* piece
 *   of state that still distinguishes the set from "all integers from some point on"?
 * - `addBack` can only ever re-introduce a number strictly below that frontier. Why?
 * - Which structure gives you "give me the minimum" cheaply, and what does it cost you to also
 *   reject duplicates?
 */
class I2336smallestInfiniteSet {

    /**
     * Minimal common surface so the shared test battery in [Solution] can run against every
     * implementation. [SmallestInfiniteSet] does not declare it — it is adapted in the test class
     * instead, so your code stays exactly in the shape LeetCode asks for.
     */
    private interface InfiniteSet {
        fun popSmallest(): Int
        fun addBack(num: Int)
    }

    /**
     * Your implementation. Fill in the two methods (and whatever state they need).
     */
    class SmallestInfiniteSet {
        private var next = 1
        private val addedBackIndex = mutableSetOf<Int>()
        private val addedBackQueue = PriorityQueue<Int>()

        fun popSmallest(): Int {
            addedBackQueue.poll()?.let { smallestAddedBack ->
                addedBackIndex.remove(smallestAddedBack)
                return smallestAddedBack
            }
            return next++
        }

        fun addBack(num: Int) {
            if (next <= num) return
            if (addedBackIndex.add(num).not()) return
            addedBackQueue.add(num)
        }
    }

    /**
     * ## Reference solution — "frontier counter + min-heap of holes"
     *
     * ### Restated
     * The set starts as every positive integer. `popSmallest` hands back the current minimum and
     * removes it; `addBack(num)` puts `num` back only if it is currently missing. Answer a stream of
     * up to 1000 such calls.
     *
     * ### The key insight
     * You cannot store infinitely many numbers, but you never have to. At any moment the set has a
     * very rigid shape:
     *
     * ```
     *   1 .. next-1      -> a *finite*, arbitrary subset (the numbers we popped, minus the ones added back)
     *   next, next+1, .. -> ALL present, untouched, forever
     * ```
     *
     * `next` is the frontier: the smallest number that has never been popped. Everything at or above
     * it is guaranteed present, so that whole infinite tail is described by the single `Int` `next`.
     * Only the finite region *below* the frontier needs real bookkeeping — and by the constraints it
     * holds at most 1000 values.
     *
     * That also answers the second hint in the header: `addBack(num)` can only ever matter when
     * `num < next`. If `num >= next` the number was never popped, so it is already present and the
     * call is a no-op — no membership check required, the comparison alone decides it.
     *
     * ### Pattern
     * **Lazy/implicit representation + heap for the minimum.** The general trick: when a structure is
     * conceptually unbounded but only finitely *perturbed*, store the perturbations explicitly and the
     * regular part as a formula (here, a counter). The same idea shows up in sparse arrays, "infinite"
     * grids in game problems, and `Iterator`-style generators.
     *
     * On top of that, `popSmallest` is a classic **min-heap** job: it is exactly "extract-min". The
     * heap alone is not enough though — a binary heap cannot answer "do you already contain `num`?"
     * without an O(n) scan, and `addBack` must reject duplicates. So the heap is paired with a
     * `HashSet` holding the same elements: the set decides membership in O(1), the heap decides order.
     * Keeping two structures in lockstep over one logical collection is a pattern worth remembering
     * (it is the same reflex behind LRU cache = hash map + linked list).
     *
     * ### Approach
     * State: `next = 1`, an empty min-heap `addedBack`, and an empty `HashSet` `addedBackSet`
     * mirroring the heap.
     *
     * - `popSmallest()`:
     *   - If `addedBack` is non-empty its minimum is below `next` by construction, so it is the global
     *     minimum: pop it from the heap, drop it from the set, return it.
     *   - Otherwise the set is exactly `next, next+1, ...`, so return `next` and increment it.
     * - `addBack(num)`:
     *   - If `num >= next` it is already present -> no-op.
     *   - Else if `addedBackSet` already holds it -> no-op (this is what makes a double `addBack` safe).
     *   - Else push onto the heap and into the set.
     *
     * ### Complexity
     * With `k` = number of holes currently below the frontier (`k <= 1000` here):
     * - `popSmallest` — O(log k) for the heap poll; O(1) when the heap is empty.
     * - `addBack` — O(log k) for the push, O(1) to reject.
     * - Space — O(k), only the popped-and-returned numbers; the infinite tail costs one `Int`.
     *
     * ### Common pitfalls
     * - **Materialising the set** (e.g. a 1001-long boolean array) works *only* because `num <= 1000`;
     *   it is not a real answer to "infinite set" and breaks the moment the bound is lifted. The
     *   frontier keeps the solution genuinely unbounded.
     * - **Heap without a set**: `addBack(2); addBack(2)` then pushes `2` twice and `popSmallest`
     *   returns `2` twice. The dedupe is not optional — see `addBackTwiceIsIgnoredTheSecondTime`.
     * - **Forgetting the `num >= next` guard**: without it, `addBack(5)` on a fresh set inserts `5`
     *   into the heap and `5` gets popped before `1` — and again later from the tail. See
     *   `addBackOfNeverPoppedNumberIsNoOp`.
     * - **Off-by-one at the frontier**: after popping `3`, `next` is `4` and `3` is a legal add-back
     *   (`3 < 4`). Using `<=` instead of `<` would let the just-popped-and-still-absent number be
     *   rejected. See `addBackMostRecentlyPoppedNumber`.
     * - **Forgetting to remove from the mirror set on pop** — the value then looks permanently present
     *   and can never be added back again (`interleavedPopAndAddBackOfSameValue` catches it).
     */
    class ReferenceSolution {

        /** Smallest number never yet popped: every integer >= [next] is present in the set. */
        private var next = 1

        /** Numbers below [next] that were popped and later added back — ordered, for extract-min. */
        private val addedBack = PriorityQueue<Int>()

        /** Same contents as [addedBack], for O(1) duplicate rejection in [addBack]. */
        private val addedBackSet = HashSet<Int>()

        fun popSmallest(): Int {
            addedBack.poll()?.let { smallest ->
                addedBackSet.remove(smallest)
                return smallest
            }
            return next++
        }

        fun addBack(num: Int) {
            if (num >= next) return                  // never popped -> already present
            if (!addedBackSet.add(num)) return       // already back in the set -> no-op
            addedBack.add(num)
        }
    }

    /**
     * Same idea, with the heap-plus-set pair collapsed into one `TreeSet`.
     *
     * A sorted set gives *both* `first()` (the minimum) and duplicate rejection in one structure, so
     * the two-structures-in-lockstep bookkeeping disappears. The trade-off: `TreeSet` is a red-black
     * tree, so insertion is O(log k) with worse constants than a binary heap's, and every element
     * costs a node. Worth knowing as the shorter answer to write under interview pressure.
     */
    class ReferenceSolutionSortedSet {

        private var next = 1
        private val addedBack = TreeSet<Int>()

        fun popSmallest(): Int =
            addedBack.pollFirst() ?: next++

        fun addBack(num: Int) {
            if (num < next) addedBack.add(num)       // TreeSet.add is itself the dedupe
        }
    }

    /**
     * One shared battery, replayed against every implementation.
     *
     * Each `@Test` describes a behaviour once and [forEachImpl] runs it on a freshly built set per
     * implementation, so a case can never drift between them. Failures are collected rather than
     * thrown on the first one, so the message names *which* implementations broke.
     *
     * Right now [implementations] holds only yours; if you write a second approach (or
     * `/leetcode-help` adds a reference), add it to the list and the whole battery covers it too.
     */
    @Nested
    inner class Solution {

        private val implementations: List<Pair<String, () -> InfiniteSet>> = listOf(
            "yours (SmallestInfiniteSet)" to { SmallestInfiniteSet().asInfiniteSet() },
            "reference (heap + set)" to { ReferenceSolution().asInfiniteSet() },
            "reference (TreeSet)" to { ReferenceSolutionSortedSet().asInfiniteSet() },
        )

        /** Adapts [SmallestInfiniteSet] to [InfiniteSet] without modifying it. */
        @Suppress("unused")
        private fun SmallestInfiniteSet.asInfiniteSet(): InfiniteSet = object : InfiniteSet {
            override fun popSmallest(): Int = this@asInfiniteSet.popSmallest()
            override fun addBack(num: Int) = this@asInfiniteSet.addBack(num)
        }

        /** Adapts [ReferenceSolution] to [InfiniteSet]. */
        private fun ReferenceSolution.asInfiniteSet(): InfiniteSet = object : InfiniteSet {
            override fun popSmallest(): Int = this@asInfiniteSet.popSmallest()
            override fun addBack(num: Int) = this@asInfiniteSet.addBack(num)
        }

        /** Adapts [ReferenceSolutionSortedSet] to [InfiniteSet]. */
        private fun ReferenceSolutionSortedSet.asInfiniteSet(): InfiniteSet = object : InfiniteSet {
            override fun popSmallest(): Int = this@asInfiniteSet.popSmallest()
            override fun addBack(num: Int) = this@asInfiniteSet.addBack(num)
        }

        /** Runs [block] on a fresh set from every implementation; reports all failures together. */
        private fun forEachImpl(block: (InfiniteSet) -> Unit) {
            val failures = implementations.mapNotNull { (name, factory) ->
                try {
                    block(factory())
                    null
                } catch (e: Throwable) {
                    "$name failed: ${e.message}"
                }
            }
            if (failures.isNotEmpty()) fail(failures.joinToString("\n"))
        }

        /**
         * Official example:
         * ops:  ["SmallestInfiniteSet","addBack","popSmallest","popSmallest","popSmallest",
         *        "addBack","popSmallest","popSmallest","popSmallest"]
         * args: [[],[2],[],[],[],[1],[],[],[]]
         * out:  [null,null,1,2,3,null,1,4,5]
         */
        @Test
        fun example() = forEachImpl { set ->
            set.addBack(2) // 2 is already in the set -> no-op
            assertEquals(1, set.popSmallest())
            assertEquals(2, set.popSmallest())
            assertEquals(3, set.popSmallest())
            set.addBack(1) // 1 was popped -> comes back, and is now the smallest again
            assertEquals(1, set.popSmallest())
            assertEquals(4, set.popSmallest())
            assertEquals(5, set.popSmallest())
        }

        /** With no `addBack` at all, the set is just 1, 2, 3, ... in order. */
        @Test
        fun popsAscendingFromFreshSet() = forEachImpl { set ->
            (1..20).forEach { expected ->
                assertEquals(expected, set.popSmallest(), "pop #$expected")
            }
        }

        /** Adding back a number that was never popped changes nothing. */
        @Test
        fun addBackOfNeverPoppedNumberIsNoOp() = forEachImpl { set ->
            set.addBack(5)
            set.addBack(1000)
            assertEquals(1, set.popSmallest())
            assertEquals(2, set.popSmallest())
            assertEquals(3, set.popSmallest())
            assertEquals(4, set.popSmallest())
            assertEquals(5, set.popSmallest()) // exactly once, not twice
            assertEquals(6, set.popSmallest())
        }

        /** Adding the same number back twice must not make it poppable twice. */
        @Test
        fun addBackTwiceIsIgnoredTheSecondTime() = forEachImpl { set ->
            assertEquals(1, set.popSmallest())
            assertEquals(2, set.popSmallest())
            assertEquals(3, set.popSmallest())
            set.addBack(2)
            set.addBack(2) // already present again -> no-op
            assertEquals(2, set.popSmallest())
            assertEquals(4, set.popSmallest(), "2 must not be returned a second time")
        }

        /** Numbers added back out of order still come out smallest-first. */
        @Test
        fun addBackOutOfOrderStillPopsInAscendingOrder() = forEachImpl { set ->
            repeat(5) { set.popSmallest() } // popped 1..5
            set.addBack(3)
            set.addBack(1)
            set.addBack(4)
            assertEquals(1, set.popSmallest())
            assertEquals(3, set.popSmallest())
            assertEquals(4, set.popSmallest())
            assertEquals(6, set.popSmallest()) // back to the untouched tail
        }

        /** Pop / add-back of the same value, tightly interleaved. */
        @Test
        fun interleavedPopAndAddBackOfSameValue() = forEachImpl { set ->
            assertEquals(1, set.popSmallest())
            set.addBack(1)
            assertEquals(1, set.popSmallest())
            set.addBack(1)
            assertEquals(1, set.popSmallest())
            assertEquals(2, set.popSmallest())
        }

        /**
         * Adding back the number that was *most recently* popped — the boundary case where the
         * "added back" values and the untouched tail meet.
         */
        @Test
        fun addBackMostRecentlyPoppedNumber() = forEachImpl { set ->
            assertEquals(1, set.popSmallest())
            assertEquals(2, set.popSmallest())
            assertEquals(3, set.popSmallest())
            set.addBack(3)
            assertEquals(3, set.popSmallest())
            assertEquals(4, set.popSmallest())
        }

        /** Empty the low range completely, add all of it back, and drain it again. */
        @Test
        fun drainAddBackEverythingAndDrainAgain() = forEachImpl { set ->
            repeat(10) { set.popSmallest() } // popped 1..10
            (10 downTo 1).forEach { set.addBack(it) }
            (1..10).forEach { expected ->
                assertEquals(expected, set.popSmallest(), "second drain, pop #$expected")
            }
            assertEquals(11, set.popSmallest())
        }
    }
}
