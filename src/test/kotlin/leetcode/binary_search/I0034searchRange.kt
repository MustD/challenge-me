package leetcode.binary_search

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 34. Find First and Last Position of Element in Sorted Array
 * (https://leetcode.com/problems/find-first-and-last-position-of-element-in-sorted-array/)
 *
 * Given an array of integers `nums` sorted in non-decreasing order, find the starting and ending
 * position of a given `target` value. If `target` is not found in the array, return `[-1, -1]`.
 * You must write an algorithm with O(log n) runtime complexity.
 *
 * Constraints:
 * - 0 <= nums.length <= 10^5   (the array can be empty)
 * - -10^9 <= nums[i] <= 10^9
 * - nums is a non-decreasing array (duplicates allowed — that's the whole point)
 * - -10^9 <= target <= 10^9
 */
typealias I0034 = (IntArray, Int) -> IntArray

class I0034searchRange {

    @Nested
    inner class Solution : ProblemTest<I0034> {

        override val cases = testCases<I0034>(
            args("[5,7,7,8,8,10]", 8) expects "[3,4]",
            args("[5,7,7,8,8,10]", 6) expects "[-1,-1]",
            args("[1,2,6,6,6,6,6,6,6,7,8]", 6) expects "[2, 8]",
            args("[]", 0) expects "[-1,-1]",
            args("[2,2]", 3) expects "[-1,-1]",
            args("[1]", 1) expects "[0, 0]"
        )

        @Test
        fun test() = check(::searchRange, ::referenceSolution)

        /**
         * ANALYSIS (of your `searchRange` — the primary solution in `check`).
         *
         * PATTERN: two `lowerBound` sweeps on a sorted array (a.k.a. `bisect_left`). Left edge =
         * first index with `nums[i] >= target`; right edge = (first index with `nums[i] > target`) - 1.
         * You express the second as `lowerBound(target + 1)`, which is the idiomatic trick.
         *
         * WHY IT'S CORRECT: both loops use the half-open window `[lo, hi)` with the floor midpoint
         * `mid = lo + (hi - lo) / 2`. When the predicate holds (`goLeft`) you set `hi = mid` (keep
         * mid as a candidate); otherwise `lo = mid + 1` (discard mid). Since `mid < hi` always, `hi`
         * strictly drops or `lo` strictly rises, so the window shrinks every iteration -> guaranteed
         * termination, and `lo` lands on the first qualifying index (or `nums.size`). This is the
         * loop shape that avoids the classic `lo = mid` infinite-loop trap.
         *   - First loop predicate `target <= nums[mid]`  == `nums[mid] >= target`   -> left edge.
         *   - Second loop predicate `(target + 1) <= nums[mid]` == `nums[mid] > target` -> right edge + 1.
         *
         * TWO THINGS YOU DID WELL:
         *   1. `nums.getOrNull(lo) != target` folds the "off the end" and "wrong value" checks into
         *      one line: when `lo == nums.size` the getter returns null (!= target), covering the
         *      empty-array and target-absent cases without an explicit bounds guard.
         *   2. You DON'T reset `lo = 0` before the second search — you reuse `lo == left`. That's a
         *      real (small) optimization: the last occurrence can never precede the first, so the
         *      right-edge search only needs the window `[left, n)`. Correct because `nums[left] ==
         *      target < target + 1`, so index `left` never qualifies and `lo - 1 >= left`.
         *
         * TIME: O(log n) — two binary searches; the second runs over the narrower `[left, n)`, so
         *   in practice slightly fewer iterations, same asymptotic class.
         * SPACE: O(1) auxiliary (a handful of Int locals); the 2-element result is output, not work.
         *
         * EDGE CASES HANDLED: empty array (`lo` stays 0, `getOrNull(0)` == null), singleton,
         *   all-equal blocks, and target outside the value range. `target + 1` cannot overflow since
         *   `target <= 1e9`, well inside Int.
         *
         * ALTERNATIVES:
         *   - Single lower-bound + linear walk right from `first` to count duplicates: O(log n + k)
         *     where k = size of the equal block. Simpler, but degrades to O(n) when the array is one
         *     giant run of `target` — your all-binary approach stays O(log n) regardless.
         *   - `nums.indexOf` / `lastIndexOf` (linear scan): trivial to write but O(n), violating the
         *     problem's required O(log n).
         *   Your solution is asymptotically optimal: you must read at least log n elements to locate a
         *   boundary in a sorted array by comparisons, so O(log n) is the floor.
         *
         * PARALLELISM: not worth it. n <= 1e5 and binary search is inherently sequential — each step's
         *   probe depends on the previous comparison, a hard data dependency. Thread/SIMD overhead would
         *   dwarf ~17 comparisons. (A parallel angle only appears if you drop the sorted precondition and
         *   must scan unsorted data — then a parallel reduce for min/max index of a value applies.)
         *
         * REAL WORLD: this is `bisect_left`/`std::lower_bound` — the backbone of range queries over
         *   sorted columns (DB index scans returning `[start, end)` of a key), time-series windowing
         *   ("all events in [t0, t1)"), and LSM/SSTable block lookups. At scale you'd reach for the
         *   library primitive rather than hand-rolling; the interesting production wear is elsewhere —
         *   cache/branch-prediction (a branchless or Eytzinger layout can beat textbook binary search
         *   on large arrays) and the fact that data often lives on disk/across shards, turning "one
         *   O(log n) search" into a very different I/O-bound problem.
         */
        fun searchRange(nums: IntArray, target: Int): IntArray {
            var lo = 0
            var hi = nums.size
            while (lo < hi) {
                val mid = lo + (hi - lo) / 2

                val goLeft = target <= nums[mid]
                if (goLeft) hi = mid
                else lo = mid + 1
            }
            if (nums.getOrNull(lo) != target) return intArrayOf(-1, -1) //null when lo reach hi (out of bounds)
            val left = lo

            hi = nums.size
            while (lo < hi) {
                val mid = lo + (hi - lo) / 2

                val goLeft = (target + 1) <= nums[mid]
                if (goLeft) hi = mid
                else lo = mid + 1
            }

            return intArrayOf(left, lo - 1)
        }

        /**
         * PATTERN: two binary searches for the boundaries of a value in a sorted array —
         * a "lower bound" (first index whose value is >= target) done twice.
         *
         * INTUITION: In a non-decreasing array all copies of `target` sit in one contiguous
         * block. The block's LEFT edge is `lowerBound(target)` — the first index with
         * `nums[i] >= target`. The block's RIGHT edge is one step before where the NEXT larger
         * value begins, i.e. `lowerBound(target + 1) - 1`. So a single reusable `lowerBound`
         * helper answers both questions; no second, differently-shaped search is needed.
         *
         * APPROACH:
         *   1. first = lowerBound(nums, target).
         *   2. If first is off the end (== nums.size) or nums[first] != target, target is
         *      absent -> [-1, -1]. (This one check also covers the empty array.)
         *   3. Otherwise last = lowerBound(nums, target + 1) - 1. Return [first, last].
         *
         * LOWER-BOUND INVARIANT: search the half-open window [lo, hi). `nums[mid] < target`
         * means mid can't be the answer, so lo = mid + 1; otherwise mid is a candidate, so
         * hi = mid. The window always shrinks (lo advances or hi drops to mid < hi), so it
         * terminates and lo lands on the first qualifying index (or nums.size).
         *
         * COMPLEXITY: O(log n) time (two binary searches), O(1) extra space.
         *
         * PITFALLS:
         *   - The infinite loop your `second` search hits: with `lo = mid` you must round the
         *     midpoint UP (`mid = lo + (hi - lo + 1) / 2`) and shrink hi via `hi = mid - 1`,
         *     or the window stops shrinking when hi - lo == 1. The lower-bound shape below
         *     (`lo = mid + 1` / `hi = mid`, floor midpoint) sidesteps that entirely. Your
         *     FIRST search is already a correct lower bound — reuse that shape for both edges.
         *   - Don't index before the presence check: after the left search, `first` may equal
         *     nums.size, so guard with `first == nums.size || nums[first] != target`.
         *   - The `target + 1` trick is safe here: target <= 1e9, well within Int range.
         *   - The size == 0 / size == 1 special cases aren't needed; the general logic handles
         *     empty and singleton arrays.
         */
        fun referenceSolution(nums: IntArray, target: Int): IntArray {
            val first = lowerBound(nums, target)
            if (first == nums.size || nums[first] != target) return intArrayOf(-1, -1)
            return intArrayOf(first, lowerBound(nums, target + 1) - 1)
        }

        /** First index `i` in `[0, nums.size]` with `nums[i] >= target`. */
        private fun lowerBound(nums: IntArray, target: Int): Int {
            var lo = 0
            var hi = nums.size
            while (lo < hi) {
                val mid = lo + (hi - lo) / 2

                if (nums[mid] < target) lo = mid + 1 else hi = mid
            }
            return lo
        }

    }
}
