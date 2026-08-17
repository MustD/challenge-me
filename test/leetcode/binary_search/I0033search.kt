package leetcode.binary_search

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 33. Search in Rotated Sorted Array  (https://leetcode.com/problems/search-in-rotated-sorted-array/)
 *
 * An integer array `nums` is sorted in ascending order with distinct values, then possibly rotated at
 * an unknown pivot index k (1 <= k < nums.length): [nums[k], ..., nums[n-1], nums[0], ..., nums[k-1]].
 * Given the (possibly rotated) array and an integer `target`, return the index of `target` if present,
 * otherwise -1.
 *
 * Must run in O(log n) time.
 *
 * Constraints:
 * - 1 <= nums.length <= 5000
 * - -10^4 <= nums[i] <= 10^4
 * - All values of nums are unique
 * - -10^4 <= target <= 10^4
 */
typealias I0033 = (IntArray, Int) -> Int

class I0033search {

    @Nested
    inner class Solution : ProblemTest<I0033> {

        override val cases = testCases<I0033>(
            args("[4,5,6,7,0,1,2]", 0) expects 4,
            args("[4,5,6,7,0,1,2]", 3) expects -1,
            args("[1]", 0) expects -1,
            args("[1]", 1) expects 0,
            args("[5,1,3]", 5) expects 0,
            args("[1,2,3,4,5]", 4) expects 3,   // not actually rotated
            args("[5,1,2,3,4]", 1) expects 1,
        )


        @Test
        fun test() = check(::search, ::referenceSolution)

        /**
         * YOUR SOLUTION — analysis (verified: all 7 cases pass).
         *
         * Pattern: modified binary search, "one half is always sorted". A rotated sorted array
         * of distinct values is two ascending runs glued together; splitting at `mid`, at least
         * one of [lo..mid] / [mid..hi] is fully sorted, so you can range-test the target against
         * that sorted half and discard half the array each step.
         *
         * What you wrote, walked through:
         *   - `if (target == nums[mid]) return mid` up front — the found-check comes first, so the
         *     subsequent range tests only ever run for target != nums[mid]. That is what lets your
         *     ranges be closed on the Kotlin side yet stay logically half-open (see below).
         *   - `lSorted = nums[lo] <= nums[mid]` — the crucial `<=` (not `<`). When lo == mid (a
         *     one-element window, or the final narrowing) the left half is trivially sorted; a
         *     strict `<` would misclassify it and send you down the wrong branch. Correct.
         *   - Left sorted: `target in nums[lo]..nums[mid] - 1` is nums[lo] <= target <= nums[mid]-1,
         *     i.e. nums[lo] <= target < nums[mid]. Since target == nums[mid] is already handled, this
         *     is exactly "target lies in the sorted left half" -> shrink hi. Otherwise the target must
         *     be in the (possibly pivot-containing) right half -> lo = mid + 1.
         *   - Right sorted: `target in nums[mid] + 1..nums[hi]` is nums[mid] < target <= nums[hi],
         *     the symmetric test -> lo = mid + 1, else hi = mid - 1.
         *   Using Kotlin's `in IntRange` here is a clean, readable way to express the ordered-range
         *   membership; when the endpoints cross (e.g. nums[mid]-1 < nums[lo]) the range is simply
         *   empty and the test is false, which is the behavior you want.
         *
         * Time:  O(log n). Single `while (lo <= hi)` loop, no nesting; each iteration moves lo up
         *        or hi down past mid, halving the window. ~log2(5000) ≈ 13 iterations worst case.
         * Space: O(1). Only the scalars lo/hi/mid/lSorted. Iterative — no recursion stack (a recursive
         *        phrasing would add O(log n) frames; yours avoids that).
         *
         * Correctness / edge cases you handle:
         *   - Single element `[1]`: lo == hi == mid, resolved by the found-check or falls through to -1.
         *   - Not-actually-rotated input `[1,2,3,4,5]`: nums[lo] <= nums[mid] always true, degenerates
         *     to ordinary binary search. Correct.
         *   - Miss returns -1 (presence search, not insertion-point) — right, a lower-bound index would
         *     be meaningless here.
         *   - `mid = lo + (hi - lo) / 2` avoids the classic (lo + hi) overflow. Harmless at n <= 5000,
         *     but the right habit.
         *   - RELIES ON DISTINCT VALUES (a stated constraint). With duplicates, nums[lo] == nums[mid]
         *     makes "which half is sorted" ambiguous and this breaks — that is the separate problem
         *     LC 81 (Search in Rotated Sorted Array II), where the fix is `lo++` on the tie and worst
         *     case degrades to O(n).
         *
         * Alternatives & trade-offs:
         *   - Two-pass: binary-search the pivot (index of min), then binary-search the correct segment.
         *     Same O(log n)/O(1), but ~2x the comparisons and more code — your single-pass decision
         *     search is strictly tidier.
         *   - Index-remap: binary search over [0, n) mapping a logical sorted index i to the physical
         *     index (i + pivot) % n. One clean comparison per step, but you still need the pivot first,
         *     or clever arithmetic — no asymptotic win.
         *   - Linear scan: O(n), trivial, but violates the required O(log n). Only defensible if n is
         *     tiny or the array isn't actually structured.
         *   Your approach is asymptotically optimal: any comparison-based search must read Ω(log n)
         *   elements to distinguish n positions, so no better bound exists.
         *
         * Parallelism: not worth it, and worth understanding why. Binary search is inherently
         *   sequential — each step's bounds depend on the previous comparison (a data dependency chain),
         *   so it can't be split across threads. And with only ~13 steps total, thread/coordination
         *   overhead would dwarf the work by orders of magnitude. (Parallel search only pays off when
         *   the data is unsorted and you must scan all n elements — then a parallel reduce over shards
         *   helps; here the whole point is that you DON'T scan.)
         *
         * Real-world: "search a sorted structure with a wraparound offset" shows up in circular/ring
         *   buffers and log-structured stores, where the rotation point (head) is tracked explicitly as
         *   an offset — so you'd remap the index (the alternative above) rather than binary-search for
         *   the pivot. In everyday code you'd reach for a library primitive (Arrays.binarySearch /
         *   Collections.binarySearch) on genuinely sorted data, or a B-tree / sorted index in a DB;
         *   the hand-rolled rotated search is mostly an interview construct exercising the invariant.
         */
        fun search(nums: IntArray, target: Int): Int {
            var lo = 0
            var hi = nums.lastIndex
            while (lo <= hi) {
                val mid = lo + (hi - lo) / 2
                if (target == nums[mid]) return mid

                val lSorted = nums[lo] <= nums[mid]
                if (lSorted) {
                    if (target in nums[lo]..nums[mid] - 1) hi = mid - 1
                    else lo = mid + 1
                } else {
                    if (target in nums[mid] + 1..nums[hi]) lo = mid + 1
                    else hi = mid - 1
                }

            }
            return -1
        }

        /**
         * Pattern: modified binary search ("one half is always sorted").
         *
         * Key insight — a rotated sorted array of distinct values is two sorted runs glued
         * together. When you split at `mid`, AT LEAST ONE of the halves [lo..mid] or [mid..hi]
         * is fully sorted (the pivot can only sit in one of them). So each step:
         *   1. If nums[mid] == target, done.
         *   2. Figure out which half is sorted by comparing nums[lo] and nums[mid].
         *        nums[lo] <= nums[mid]  =>  the LEFT half is sorted.
         *        otherwise              =>  the RIGHT half is sorted.
         *   3. Check whether target falls inside that sorted half's value range (a plain
         *      ordered-range test). If yes, recurse into it; if no, discard it and search the
         *      other half.
         * Because you drop half the array every iteration, it stays O(log n).
         *
         * Complexity: O(log n) time, O(1) space — classic binary search bookkeeping.
         *
         * Pitfalls:
         *   - Use `nums[lo] <= nums[mid]` (<=, not <): when lo == mid the left half is trivially
         *     sorted, and a strict < would misclassify it.
         *   - The range tests must be half-open and anchored on the SORTED side's endpoints:
         *     left sorted -> nums[lo] <= target < nums[mid]; right sorted -> nums[mid] < target <= nums[hi].
         *   - Return -1 (not `lo`) when the loop exits — this is a presence search, not a
         *     lower-bound/insertion search, so a miss has no meaningful index.
         *
         */
        fun referenceSolution(nums: IntArray, target: Int): Int {
            var lo = 0
            var hi = nums.lastIndex
            while (lo <= hi) {
                val mid = lo + (hi - lo) / 2
                when {
                    nums[mid] == target -> return mid
                    nums[lo] <= nums[mid] -> // left half [lo..mid] is sorted
                        if (target >= nums[lo] && target < nums[mid]) hi = mid - 1 else lo = mid + 1

                    else -> // right half [mid..hi] is sorted
                        if (target > nums[mid] && target <= nums[hi]) lo = mid + 1 else hi = mid - 1
                }
            }
            return -1
        }

    }
}
