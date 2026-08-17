package leetcode.heap

import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 295. Find Median from Data Stream  (https://leetcode.com/problems/find-median-from-data-stream/)
 *
 * The median is the middle value in an ordered integer list. If the size of the list is even,
 * there is no single middle value, and the median is the mean of the two middle values.
 *   - For arrays [2,3,4], the median is 3.
 *   - For arrays [2,3],   the median is (2 + 3) / 2 = 2.5.
 *
 * Numbers arrive one at a time from a stream, and the median may be asked for at any point.
 * Implement the `MedianFinder` class:
 *
 * - `MedianFinder()`          initializes the MedianFinder object.
 * - `addNum(num: Int)`        adds the integer `num` from the data stream to the data structure.
 * - `findMedian(): Double`    returns the median of all elements added so far. Answers within
 *                             10^-5 of the actual answer are accepted.
 *
 * Worked example (each call operates on the same instance):
 *   MedianFinder()
 *   addNum(1)      // stream = [1]
 *   addNum(2)      // stream = [1, 2]
 *   findMedian()   -> 1.5   // (1 + 2) / 2
 *   addNum(3)      // stream = [1, 2, 3]
 *   findMedian()   -> 2.0
 *
 * Constraints:
 * - -10^5 <= num <= 10^5
 * - There will be at least one element in the data structure before `findMedian` is called.
 * - At most 5 * 10^4 calls will be made to `addNum` and `findMedian`.
 *
 * Follow-up (from the official statement):
 * 1. If all integers from the stream are in the range [0, 100], how would you optimize your
 *    solution?
 * 2. If 99% of all integers from the stream are in the range [0, 100], how would you optimize
 *    your solution?
 *
 * NOTE ON THE HARNESS: this is a *design* problem — a stateful class with several methods, not a
 * single pure function. The `ProblemTest` / `testCases` DSL models one function signature per
 * problem and can't drive a multi-method class, so (like the other design problems in this repo:
 * `I0146lruCache`, `I0155MinStack`, `I0208implementTrieAttempt2`,
 * `I0211designAddAndSearchWordsDataStructure`) it is written as a plain class driven by ordinary
 * `@Test` methods that replay a call sequence. The tests below are your correctness oracle — they
 * fail until `TODO("implement")` is replaced.
 *
 * The call sequences live once in `MedianFinderContract` and are replayed against two nested test
 * classes — `Solution` (your `MedianFinder`) and `Reference` (`MedianFinderReference`) — which is
 * this file's stand-in for the harness's usual `check(::yourSolution, ::referenceSolution)`. Both
 * classes stay plain, standalone, LeetCode-pasteable classes; a tiny adapter in each nested test
 * class bridges them to the shared contract.
 *
 * A note on scale: with up to 5 * 10^4 calls, re-sorting the whole stream on every `findMedian`
 * is the obvious first idea — think about what that costs per call, and what you'd need to keep
 * around between calls to do better.
 */
class I0295findMedianFromDataStream {

    class MedianFinder {
        val lo = PriorityQueue<Int>(compareBy { it }) // 10 20 30 <- peek
        val hi = PriorityQueue<Int>(compareByDescending { it }) // peek -> 10 20 30

        fun addNum(num: Int) {
            lo.add(num)
            hi.add(lo.poll())
            if (hi.size > lo.size) lo.add(hi.poll())
        }

        fun findMedian(): Double {
            return if (lo.size == hi.size) {
                (lo.peek() + hi.peek()) / 2.0
            } else {
                lo.peek().toDouble()
            }

        }

    }

    /**
     * REFERENCE SOLUTION — two heaps ("dual heap" / "median maintenance").
     *
     * Restatement
     * -----------
     * Numbers stream in one by one; at any moment you may be asked for the median of everything
     * seen so far. Nothing is ever removed. So the real question is: *what do I keep between calls
     * so that the median is cheap to read?*
     *
     * Why the obvious ideas are not enough
     * ------------------------------------
     * - Keep an unsorted list, sort on every query: O(n log n) per `findMedian`. With 5*10^4 mixed
     *   calls that is ~10^9 element moves — far too slow.
     * - Keep a sorted `ArrayList`, binary-search the insertion point: `findMedian` becomes O(1),
     *   but `addNum` is O(n) because inserting into the middle shifts the tail. That is O(n^2)
     *   overall (~1.25*10^9 shifts worst case). It happens to pass on LeetCode thanks to
     *   `System.arraycopy` being memory-bandwidth fast, but it is not the intended answer.
     *
     * The key insight
     * ---------------
     * You never need the whole sorted order — only the one or two elements sitting *at the
     * boundary* between the smaller half and the larger half. A heap is exactly the structure that
     * gives you O(1) access to one extreme and O(log n) insertion. So use **two** of them, back to
     * back at the boundary:
     *
     *     lo = MAX-heap holding the smaller half   -> its root is the LARGEST of the low values
     *     hi = MIN-heap holding the larger half    -> its root is the SMALLEST of the high values
     *
     *     ... low values ...  [lo.peek()] | [hi.peek()]  ... high values ...
     *                                     ^ the median lives right here
     *
     * Two invariants must hold after every `addNum`:
     *   (1) ORDER:   every element of `lo` <= every element of `hi`.
     *   (2) BALANCE: lo.size == hi.size, or lo.size == hi.size + 1 (odd total: `lo` keeps the extra).
     *
     * Then:
     *   - odd total  -> median = lo.peek()
     *   - even total -> median = (lo.peek() + hi.peek()) / 2.0
     *
     * The insert trick
     * ----------------
     * Do NOT branch on "is num smaller than the median?" — that is where most buggy attempts live.
     * Instead use the unconditional funnel below, which restores both invariants by construction:
     *
     *     lo.add(num)                                 // everything enters through the low half
     *     hi.add(lo.poll())                           // ship lo's largest across -> invariant (1)
     *     if (hi.size > lo.size) lo.add(hi.poll())    // rebalance                 -> invariant (2)
     *
     * Step 2 is what makes the comparison-free version correct: after adding `num` to `lo`, the
     * maximum of `lo` is by definition >= every other low value, so moving *that* element to `hi`
     * can never violate the ordering. Step 2 always makes `hi` one bigger than it should be when
     * sizes were equal, hence the single-line rebalance in step 3.
     *
     * Complexity
     * ----------
     * `addNum`     O(log n) — at most three heap operations, each O(log n).
     * `findMedian` O(1)     — one or two `peek`s, no restructuring.
     * Space        O(n)     — every number seen is stored exactly once, split across the two heaps.
     *
     * Common pitfalls
     * ---------------
     * - Forgetting the max-heap. `PriorityQueue<Int>()` is a MIN-heap in Java/Kotlin; `lo` must be
     *   built with `compareByDescending { it }` (or `reverseOrder()`), otherwise `lo.peek()` returns
     *   the smallest low value and the median is nonsense.
     * - Integer division: `(lo.peek() + hi.peek()) / 2` is Int math and truncates — [1,2] would give
     *   1 instead of 1.5. Divide by `2.0`.
     * - Balancing the wrong way, or only rebalancing when the sizes differ by more than one. The
     *   invariant must be re-established after *every* insert, not lazily.
     * - Picking the median from the wrong heap. With the convention above the extra element always
     *   lives in `lo`, so the odd case is `lo.peek()`. If you let `hi` hold the extra instead, the
     *   odd case becomes `hi.peek()` — either is fine, but be consistent.
     * - Calling `findMedian` on an empty stream would NPE on `peek()`. The constraints promise at
     *   least one element, so no guard is added here; in production code you would return
     *   `Double.NaN` or throw explicitly.
     * - Overflow is a non-issue at these constraints (|num| <= 10^5, so the sum fits an Int), but the
     *   habit worth keeping is to widen before summing when the range is unknown.
     *
     * Follow-ups from the official statement
     * --------------------------------------
     * 1. All integers in [0, 100]: drop the heaps and keep a COUNTING array `counts[101]` plus a
     *    running total. `addNum` is O(1); `findMedian` scans at most 101 buckets accumulating counts
     *    until it passes the (n/2)-th element — O(101) = O(1) with a tiny constant, and O(1) space.
     *    This is counting sort applied to a stream.
     * 2. 99% in [0, 100]: keep the bucket array for the in-range values, and two small overflow
     *    containers (a max-heap of values below 0 and a min-heap of values above 100, or just sorted
     *    lists) for the 1% outliers. `findMedian` walks the low outliers, then the buckets, then the
     *    high outliers, using the same rank-counting walk. Amortised cost stays near O(1) because
     *    the outlier structures hold ~1% of n.
     *
     * Related patterns worth transferring
     * -----------------------------------
     * The "two heaps facing each other at a boundary" idea generalises to any *running k-th order
     * statistic*: sliding-window median (LC 480, add a lazy-deletion map), IPO / scheduling problems
     * that pair a heap of "not yet available" with a heap of "available now" (LC 502, LC 2462), and
     * the general Kth-largest-in-a-stream (LC 703, which needs only a single size-k min-heap).
     */
    class MedianFinderReference {

        /** Max-heap over the smaller half: `peek()` is the LARGEST of the low values. */
        private val lo = PriorityQueue<Int>(compareByDescending { it })

        /** Min-heap over the larger half: `peek()` is the SMALLEST of the high values. */
        private val hi = PriorityQueue<Int>()

        fun addNum(num: Int) {
            lo.add(num)                                 // everything enters through the low half
            hi.add(lo.poll())                           // ship lo's max across -> lo <= hi holds
            if (hi.size > lo.size) lo.add(hi.poll())    // keep sizes equal, or lo one bigger
        }

        fun findMedian(): Double =
            if (lo.size > hi.size) lo.peek().toDouble()          // odd total: the extra lives in lo
            else (lo.peek() + hi.peek()) / 2.0                   // even total: average the boundary

    }

    /**
     * The shared correctness oracle. Both the stub above and the reference are driven through the
     * same call sequences via [newFinder], so implementing `MedianFinder` is enough to turn the
     * `Solution` tests green — nothing here needs to change.
     */
    abstract class MedianFinderContract {

        protected val tolerance = 1e-9

        /** Factory for the implementation under test. */
        abstract fun newFinder(): Api

        /**
         * Thin adapter so both implementations can share one test suite while each class stays a
         * standalone, LeetCode-pasteable `MedianFinder` with no interface bolted on.
         */
        interface Api {
            fun addNum(num: Int)
            fun findMedian(): Double
        }

        @Test
        fun officialExample() {
            val mf = newFinder()
            mf.addNum(1)
            mf.addNum(2)
            assertEquals(1.5, mf.findMedian(), tolerance)
            mf.addNum(3)
            assertEquals(2.0, mf.findMedian(), tolerance)
        }

        @Test
        fun singleElement() {
            val mf = newFinder()
            mf.addNum(7)
            assertEquals(7.0, mf.findMedian(), tolerance)
        }

        @Test
        fun unsortedArrivalOrder() {
            val mf = newFinder()
            mf.addNum(5)
            assertEquals(5.0, mf.findMedian(), tolerance)  // [5]
            mf.addNum(15)
            assertEquals(10.0, mf.findMedian(), tolerance) // [5,15]
            mf.addNum(1)
            assertEquals(5.0, mf.findMedian(), tolerance)  // [1,5,15]
            mf.addNum(3)
            assertEquals(4.0, mf.findMedian(), tolerance)  // [1,3,5,15]
        }

        @Test
        fun descendingArrivalOrder() {
            val mf = newFinder()
            mf.addNum(3)
            mf.addNum(2)
            mf.addNum(1)
            assertEquals(2.0, mf.findMedian(), tolerance)  // [1,2,3]
            mf.addNum(0)
            assertEquals(1.5, mf.findMedian(), tolerance)  // [0,1,2,3]
        }

        @Test
        fun duplicates() {
            val mf = newFinder()
            mf.addNum(2)
            mf.addNum(2)
            mf.addNum(2)
            assertEquals(2.0, mf.findMedian(), tolerance)
        }

        @Test
        fun negativeNumbers() {
            val mf = newFinder()
            mf.addNum(-1)
            assertEquals(-1.0, mf.findMedian(), tolerance)     // [-1]
            mf.addNum(-2)
            assertEquals(-1.5, mf.findMedian(), tolerance)     // [-2,-1]
            mf.addNum(-3)
            assertEquals(-2.0, mf.findMedian(), tolerance)     // [-3,-2,-1]
        }

        @Test
        fun mixedSignsSpanningZero() {
            val mf = newFinder()
            listOf(-100000, 100000, 0, -1, 1).forEach(mf::addNum)
            assertEquals(0.0, mf.findMedian(), tolerance)      // [-100000,-1,0,1,100000]
        }

        @Test
        fun independentInstancesDoNotShareState() {
            val a = newFinder()
            val b = newFinder()
            a.addNum(1)
            b.addNum(100)
            assertEquals(1.0, a.findMedian(), tolerance)
            assertEquals(100.0, b.findMedian(), tolerance)
        }

        @Test
        fun longAlternatingStreamMatchesBruteForce() {
            val mf = newFinder()
            val seen = mutableListOf<Int>()
            val rnd = Random(295)
            repeat(300) {
                val num = rnd.nextInt(2001) - 1000
                mf.addNum(num)
                seen.add(num)
                seen.sort()
                val n = seen.size
                val expected =
                    if (n % 2 == 1) seen[n / 2].toDouble()
                    else (seen[n / 2 - 1] + seen[n / 2]) / 2.0
                assertEquals(expected, mf.findMedian(), tolerance, "after $n inserts")
            }
        }
    }

    /** Your implementation. */
    @Nested
    inner class Solution : MedianFinderContract() {
        override fun newFinder(): Api = MedianFinder().let { mf ->
            object : Api {
                override fun addNum(num: Int) = mf.addNum(num)
                override fun findMedian(): Double = mf.findMedian()
            }
        }
    }

    /** The reference implementation, validated against the exact same cases. */
    @Nested
    inner class Reference : MedianFinderContract() {
        override fun newFinder(): Api = MedianFinderReference().let { mf ->
            object : Api {
                override fun addNum(num: Int) = mf.addNum(num)
                override fun findMedian(): Double = mf.findMedian()
            }
        }
    }
}
