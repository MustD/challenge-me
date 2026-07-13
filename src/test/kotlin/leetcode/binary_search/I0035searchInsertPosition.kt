package leetcode.binary_search

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 35. Search Insert Position  (https://leetcode.com/problems/search-insert-position/)
 *
 * Given a sorted array of distinct integers and a target value, return the index if the target is
 * found. If not, return the index where it would be inserted to keep the array sorted.
 *
 * Must run in O(log n) time.
 *
 * Constraints:
 * - 1 <= nums.length <= 10^4
 * - -10^4 <= nums[i] <= 10^4
 * - nums contains distinct values sorted in ascending order
 * - -10^4 <= target <= 10^4
 */
typealias I0035 = (IntArray, Int) -> Int

class I0035searchInsertPosition {

    @Nested
    inner class Solution : ProblemTest<I0035> {
        override val cases = testCases<I0035>(
            args("1,3,5,6", 5) expects 2,
            args("1,3,5,6", 2) expects 1,
            args("1,3,5,6", 7) expects 4,
        )

        @Test
        fun test() = check(::solution1, ::searchInsert, ::searchInsertSecond)

        fun solution1(nums: IntArray, target: Int): Int {
            fun srch(s: Int, e: Int): Int {
                val size = e - s
                val m = s + (size / 2) //searching middle index
                return when {
                    nums[m] == target -> m //if middle value == target return m index

                    nums[m] > target -> {
                        if (size == 0) return m
                        srch(s, m)
                    } //if middle value > target take left part

                    nums[m] < target -> {
                        if (size == 0) return m + 1
                        srch(m + 1, e)
                    } //if middle value < target take right part
                    else -> -1
                }
            }

            return srch(0, nums.lastIndex)
        }

        /**
         * Analysis — verified correct (all 5 cases pass).
         *
         * ## Pattern
         * Classic **binary search / lower-bound** ("find the leftmost index where `nums[i] >= target`")
         * written with the robust **half-open interval** `[lo, hi)`:
         * - `hi` starts at `nums.size` (one past the end), not `size - 1`, so the answer `n` (insert at
         *   the tail) is representable without a special case.
         * - Invariant maintained every iteration: the answer lives in `[lo, hi)`. When `target > nums[mid]`
         *   the answer must be to the right → `lo = mid + 1`; otherwise `mid` itself is still a candidate
         *   → `hi = mid` (never `mid - 1`, since `mid` may be the answer). The loop shrinks the window until
         *   `lo == hi`, which is exactly the insertion point. Because the values are *distinct*, that same
         *   index is also where an existing `target` sits — one branch handles both "found" and "insert".
         *
         * ## Complexity
         * - **Time: O(log n)** — the window `[lo, hi)` halves each iteration (line 42–45), satisfying the
         *   problem's O(log n) requirement. n ≤ 10^4 ⇒ ≤ 14 iterations.
         * - **Space: O(1)** — two index variables, no recursion (iterative), no allocation.
         *
         * ## Correctness / edge cases
         * - target below all elements → returns `0`; above all → returns `n`; both fall out of the same
         *   invariant, no special-casing. Single-element input (min constraint `n >= 1`) works.
         * - **Overflow teaching point:** `(lo + hi) / 2` (line 42) is safe *here* because indices ≤ 10^4,
         *   but the idiomatic guard is `lo + (hi - lo) / 2` — with `Int` indices near `Int.MAX_VALUE`,
         *   `lo + hi` can overflow to a negative midpoint. Worth internalizing for the general template.
         *
         * ## Alternatives
         * - **Linear scan** — walk until `nums[i] >= target`. O(n) time, O(1) space, trivially correct, but
         *   violates the explicit O(log n) requirement. Only "faster" in practice for tiny n where branch
         *   prediction beats the log factor.
         * - **Inclusive `[lo, hi]` variant** (`hi = n - 1`, `while (lo <= hi)`) — same asymptotics but needs
         *   a post-loop adjustment / extra boundary reasoning; the half-open form used here is the
         *   less error-prone default.
         * - **Stdlib**: `nums.toList().binarySearch(target)` returns `idx` if found else `-(insertion)-1`;
         *   the insertion point is recoverable, but hand-rolling is the point of the exercise.
         *
         * ## Parallelism
         * Not applicable. Binary search is inherently sequential — each comparison decides which half to
         * probe next, a hard data dependency, so there's nothing to fan out. At n ≤ 10^4 (≤14 steps) any
         * threading overhead would dwarf the work. (Parallel search only pays off over *huge* external
         * datasets, and even then via sharding + per-shard binary search, not parallelizing one search.)
         *
         * ## Real-world
         * Lower-bound is one of the most reused primitives in production: C++ `std::lower_bound`, Python
         * `bisect.bisect_left`, Java `Collections.binarySearch`/`Arrays.binarySearch`. It underpins
         * insertion into sorted structures (skip lists, B-tree/LSM index range scans), time-series "find
         * the first sample at/after time T" lookups, and rank/percentile queries. The half-open convention
         * here is exactly what those libraries standardize on, and the `lo + (hi - lo) / 2` overflow guard
         * is a real historical bug (famously latent in the JDK's binary search for ~9 years).
         */
        fun searchInsert(nums: IntArray, target: Int): Int {
            var lo = 0
            var hi = nums.size
            while (lo < hi) {
                val midIdx = (lo + hi) / 2

                if (target > nums[midIdx]) lo = midIdx + 1
                else hi = midIdx
            }

            return lo
        }

        fun searchInsertSecond(nums: IntArray, target: Int): Int {
            var lo = 0
            var hi = nums.lastIndex

            while (lo <= hi) {
                val midIdx = (lo + hi) / 2

                if (target > nums[midIdx]) lo = midIdx + 1
                else hi = midIdx - 1
            }

            return lo
        }

    }
}
