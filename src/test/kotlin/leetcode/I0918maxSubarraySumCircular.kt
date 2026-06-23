package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 918. Maximum Sum Circular Subarray  (https://leetcode.com/problems/maximum-sum-circular-subarray/)
 *
 * Given a circular integer array `nums` of length `n`, return the maximum possible sum of a
 * non-empty subarray of `nums`. In a circular array the end connects to the beginning: the next
 * element of `nums[i]` is `nums[(i + 1) % n]` and the previous element is `nums[(i - 1 + n) % n]`.
 * A subarray may include each element of the fixed buffer at most once.
 *
 * Constraints:
 * - n == nums.length
 * - 1 <= n <= 3 * 10^4
 * - -3 * 10^4 <= nums[i] <= 3 * 10^4
 * - Edge case: the maximum-sum subarray can wrap around the end; watch the all-negative case
 *   (the wrap candidate would pick the empty subarray, which is not allowed).
 */
typealias I0918 = (IntArray) -> Int

class I0918maxSubarraySumCircular {

    @Nested
    inner class Solution : ProblemTest<I0918> {

        override val cases = testCases<I0918>(
            "[1,-2,3,-2]" expects 3,    // [3]
            "[5,-3,5]" expects 10,      // [5,5] wrapping around
            "[-3,-2,-3]" expects -2,    // all negative -> single largest element
            "[3,-1,2,-1]" expects 4,    // [2,-1,3] wrapping
            "[5]" expects 5,            // single element
            "[-2]" expects -2,          // single negative element
        )

        @Test
        fun test() = check(::maxSubarraySumCircular, ::referenceSolution)

        /**
         * PATTERN: Kadane's algorithm, applied twice (the "max OR min subarray" trick).
         *
         * Intuition. The answer is one of exactly two shapes:
         *   (1) A NON-wrapping subarray — a normal contiguous range. The best such sum is plain
         *       Kadane's maximum-subarray.
         *   (2) A WRAPPING subarray — it uses a suffix + a prefix and skips a contiguous middle
         *       chunk. Key insight: maximizing the wrapping part is the same as MINIMIZING the
         *       contiguous middle chunk you throw away. So
         *           bestWrap = totalSum - (minimum-subarray sum).
         *       Run Kadane's a second time to find the minimum subarray (track running min).
         *
         * Answer = max(maxKadane, total - minKadane).
         *
         * Approach (single pass, four running variables):
         *   - curMax / maxSum: standard Kadane for the maximum subarray.
         *   - curMin / minSum: mirror Kadane for the minimum subarray.
         *   - total: running sum of all elements.
         *
         * PITFALL — the all-negative case. If every element is negative, the minimum subarray is
         * the whole array, so total - minSum == 0, which corresponds to choosing the EMPTY
         * subarray — not allowed (subarray must be non-empty). Detect this with `maxSum < 0`
         * (max element is negative) and just return maxSum in that case. That is why this code
         * guards `if (maxSum < 0) return maxSum`.
         *
         * Complexity: O(n) time (one pass), O(1) extra space.
         *
         * Note on the existing attempt above: it tries to grow/shrink a window across the wrap
         * boundary with index pointers, but `endP` is advanced in both branches while `startP`
         * jumps around, so it neither does a clean Kadane nor correctly enumerates wrapping
         * windows — the two-Kadane formulation sidesteps all of that bookkeeping.
         */
        fun referenceSolution(nums: IntArray): Int {
            var curMax = 0
            var maxSum = nums[0]
            var curMin = 0
            var minSum = nums[0]
            var total = 0

            for (n in nums) {
                curMax = maxOf(curMax + n, n)
                maxSum = maxOf(maxSum, curMax)

                curMin = minOf(curMin + n, n)
                minSum = minOf(minSum, curMin)

                total += n
            }

            // All-negative array: empty wrap subarray would win, so fall back to the best single max.
            return if (maxSum < 0) maxSum else maxOf(maxSum, total - minSum)
        }

        fun maxSubarraySumCircular(nums: IntArray): Int {
            var curMax = 0
            var maxSum = nums[0]
            var curMin = 0
            var minSum = nums[0]
            var total = 0

            for (n in nums) {
                curMax = maxOf(curMax + n, n)
                maxSum = maxOf(maxSum, curMax)

                curMin = minOf(curMin + n, n)
                minSum = minOf(minSum, curMin)

                total += n
            }

            return if (maxSum < 0) maxSum else maxOf(maxSum, total - minSum)
        }

    }
}
