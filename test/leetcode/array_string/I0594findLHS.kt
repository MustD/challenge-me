package leetcode.array_string

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 594. Longest Harmonious Subsequence  (https://leetcode.com/problems/longest-harmonious-subsequence/)
 *
 * We define a harmonious array as an array where the difference between its maximum value and its minimum value is
 * exactly 1.
 *
 * Given an integer array nums, return the length of its longest harmonious subsequence among all its possible
 * subsequences.
 *
 * Constraints:
 * - 1 <= nums.length <= 2 * 10^4
 * - -10^9 <= nums[i] <= 10^9
 */
typealias I0594 = (IntArray) -> Int

class I0594findLHS {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0594> {

        override val cases = leetcode.testCases<I0594>(
            "[1,3,2,2,5,2,3,7]" expects 5,
            "[1,2,3,4]" expects 2,
            "[1,1,1,1]" expects 0,
        )

        @Test
        fun test() = check(::findLHS, ::referenceSolution)

        fun findLHS(nums: IntArray): Int {
            //subseq -> we may skip values
            val digitCount = nums.asIterable().groupingBy { it }.eachCount()
            var max = 0
            digitCount.keys.forEach { k ->
                val kCount = checkNotNull(digitCount[k])
                digitCount[k + 1]?.let { max = maxOf(max, it + kCount) }
            }
            return max
        }

        /**
         * ## Restatement
         * Pick any subset of `nums` (order does not matter for a *subsequence* here, only which elements you keep)
         * such that its max minus its min is exactly 1. Return the largest possible size of such a subset, or 0 if
         * none exists.
         *
         * ## Pattern
         * **Frequency map (hash counting).** The word "subsequence" is a red herring: because we only care about
         * max/min, element positions are irrelevant, so the problem collapses to counting values.
         *
         * ### Intuition
         * A harmonious subsequence can contain only two distinct values, `x` and `x + 1` (max - min == 1 leaves no
         * room for anything else). Once you fix `x`, the best choice is to take *every* copy of `x` and *every* copy
         * of `x + 1`. So the answer is `max over x of count[x] + count[x + 1]`, considering only `x` where both
         * exist (otherwise the difference would be 0, not 1).
         *
         * ### Core concepts
         * - **Frequency map**: `value -> occurrences`. Turns "choose elements" into "look up two numbers".
         * - **Subsequence vs. subarray**: a subsequence may skip elements, so when only the *multiset* matters,
         *   order is irrelevant and sorting/counting are both valid.
         * - **Neighbour lookup**: checking `x + 1` (only upward) visits every adjacent pair exactly once, avoiding
         *   double counting with `x - 1`.
         * - **Both-present guard**: the pair only counts if both values occur; `[1,1,1,1]` has count 4 for `1` but
         *   answer 0.
         *
         * ## Approach
         * 1. Build `count` by one pass over `nums`.
         * 2. For every key `x` in `count`, if `x + 1` is also a key, candidate = `count[x] + count[x + 1]`.
         * 3. Return the best candidate (0 by default).
         *
         * ## Complexity
         * - Time: O(n), one pass to count, one pass over distinct keys with O(1) lookups.
         * - Space: O(d) where d <= n is the number of distinct values.
         * (Alternative: sort and use a two-pointer window where `nums[r] - nums[l] <= 1`, O(n log n) time, O(1) extra
         * space; the window only counts if its difference is exactly 1.)
         *
         * ## Common pitfalls
         * - Forgetting that the difference must be *exactly* 1: an all-equal array gives 0, not its length.
         * - Using `x - 1` and `x + 1` both and double counting, or adding `count[x]` alone when `x + 1` is absent.
         * - Integer overflow: `x + 1` with `x == Int.MAX_VALUE` wraps to `Int.MIN_VALUE`; constraints cap values at
         *   10^9 so it is safe here, but it is worth noticing.
         * - Treating "subsequence" as contiguous and reaching for a sliding window over the original order.
         */
        fun referenceSolution(nums: IntArray): Int {
            val count = HashMap<Int, Int>()
            for (n in nums) count[n] = (count[n] ?: 0) + 1

            var best = 0
            for ((x, c) in count) {
                val next = count[x + 1] ?: continue
                best = maxOf(best, c + next)
            }
            return best
        }

    }
}
