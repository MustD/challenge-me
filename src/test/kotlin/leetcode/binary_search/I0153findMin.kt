package leetcode.binary_search

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 153. Find Minimum in Rotated Sorted Array  (https://leetcode.com/problems/find-minimum-in-rotated-sorted-array/)
 *
 * An array of length n sorted in ascending order (with unique elements) has been rotated between 1 and n times.
 * Given such a rotated array `nums`, return its minimum element.
 * You must write an algorithm that runs in O(log n) time.
 *
 * Constraints:
 * - n == nums.length
 * - 1 <= n <= 5000
 * - -5000 <= nums[i] <= 5000
 * - All the integers of nums are unique.
 * - nums is sorted and rotated between 1 and n times (a full rotation leaves it fully sorted again).
 */
typealias I0153 = (IntArray) -> Int

class I0153findMin {

    @Nested
    inner class Solution : ProblemTest<I0153> {

        override val cases = testCases<I0153>(
            "[3,4,5,1,2]" expects 1,
            "[1,2,3,4,5]" expects 1,
            "[5,1,2,3,4]" expects 1,
            "[3,1,2]" expects 1,
            "[2,3,4,5,1]" expects 1,
            "[4,5,6,7,0,1,2]" expects 0,
            "[11,13,15,17]" expects 11,   // rotated n times == not rotated: min is first element
            "[1]" expects 1,              // single element
            "[2,1]" expects 1,            // smallest rotation
            "[1,2]" expects 1,            // no effective rotation, two elements
        )

        @Test
        fun test() = check(::findMin, ::referenceSolution)

        fun findMin(nums: IntArray): Int {
            var lo = 0
            var hi = nums.lastIndex
            while (lo < hi) {
                val mid = lo + (hi - lo) / 2

                // 1 2 3 4 5
                // 5 1 2 3 4
                // 2 3 4 5 1
                //3,1,2

                val goRight = nums[mid] > nums[hi]

                if (goRight) lo = mid + 1
                else hi = mid

            }

            return nums[lo]
        }

        /**
         * PATTERN: Binary search on a monotonic *predicate*, not on a target value.
         * There is no key to search for here, so the trick is to find a yes/no property that
         * flips exactly once across the array and binary-search that boundary. Compare the
         * midpoint against the RIGHT end (`nums[hi]`), the one fixed landmark whose relationship
         * to the minimum is unambiguous:
         *   - If nums[mid] > nums[hi], mid sits on the "high plateau" (left, larger segment),
         *     so the minimum must be strictly to the right  -> lo = mid + 1.
         *   - Otherwise nums[mid] <= nums[hi], mid is on the segment that contains the minimum
         *     (mid could BE the minimum), so discard the right but KEEP mid -> hi = mid.
         * The loop shrinks [lo, hi] until lo == hi, which lands exactly on the minimum.
         *
         * WHY compare with nums[hi] and not nums[lo]: a not-rotated array (e.g. [11,13,15,17])
         * has nums[lo] < nums[mid] < nums[hi] throughout; comparing to the right end still
         * correctly walks left to index 0, whereas the left-end comparison needs a special case.
         *
         * COMPLEXITY: O(log n) time (halve the window each step), O(1) space.
         *
         */
        fun referenceSolution(nums: IntArray): Int {
            var lo = 0
            var hi = nums.lastIndex
            while (lo < hi) {
                val mid = lo + (hi - lo) / 2
                if (nums[mid] > nums[hi]) lo = mid + 1   // min is strictly to the right
                else hi = mid                            // min is at mid or to its left — keep mid
            }
            return nums[lo]
        }

    }
}
