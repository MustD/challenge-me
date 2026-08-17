package leetcode.array_string

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 169. Majority Element  (https://leetcode.com/problems/majority-element/)
 *
 * Given an array `nums` of size `n`, return the majority element — the element
 * that appears more than ⌊n / 2⌋ times. You may assume the majority element
 * always exists in the array.
 *
 * Constraints:
 * - n == nums.length
 * - 1 <= n <= 5 * 10^4
 * - -10^9 <= nums[i] <= 10^9
 *
 * Follow-up: solve it in linear time and O(1) space.
 */
typealias I0169 = (IntArray) -> Int

class I0169majorityElement {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0169> {

        override val cases = leetcode.testCases<I0169>(
            "[3,2,3]" expects 3,
            "[2,2,1,1,1,2,2]" expects 2,
            "[1]" expects 1,           // single element
        )

        @Test
        fun test() = check(::majorityElementSort, ::majorityElementMap, ::referenceSolution)

        /**
         * Sorted, O(n log(n)), O(1) space
         */
        fun majorityElementSort(nums: IntArray): Int {
            nums.sort()
            return nums[nums.lastIndex / 2]
        }

        /**
         * Indexed in map O(n), O(n) space
         */
        fun majorityElementMap(nums: IntArray): Int {
            val counter = mutableMapOf<Int, Int>()
            nums.forEach {
                counter.merge(it, 1, Int::plus) //jvm only
            }
            return counter.maxBy { it.value }.key
        }

        /**
         * Pattern: Boyer–Moore Majority Vote.
         *
         * Intuition. The majority element occurs more than ⌊n/2⌋ times, so it
         * strictly outnumbers *everything else combined*. Imagine every element
         * "cancelling" one element of a different value: after all possible
         * cancellations the only value that can survive is the majority one,
         * precisely because it has more copies than all others put together.
         *
         * Approach (one linear pass, O(1) space):
         *   - Keep a `candidate` and a running `count`.
         *   - When `count == 0`, adopt the current number as the new candidate.
         *   - If the current number equals the candidate, increment `count`;
         *     otherwise decrement it (a cancellation).
         *   Because a majority element exists, whatever candidate is left standing
         *   at the end is the answer — no verification second pass is needed here
         *   (it would be required if existence weren't guaranteed).
         *
         * Complexity: O(n) time, O(1) space — one scan, two scalar variables.
         *
         * Pitfalls:
         *   - Don't confuse `count` with the true frequency of the candidate; it's
         *     a net "lead" counter that can bounce around, not a tally.
         *   - `count == 0` must trigger re-selection *before* the equality check
         *     so the current element becomes (and is counted as) the candidate.
         *   - Only safe to skip the confirmation pass because the problem
         *     guarantees a majority element exists.
         *
         * Other approaches worth knowing: sort and return nums[n/2] (O(n log n));
         * a HashMap frequency count (O(n) time / O(n) space).
         */
        fun referenceSolution(nums: IntArray): Int {
            var candidate = 0
            var count = 0
            for (n in nums) {
                if (count == 0) candidate = n
                count += if (n == candidate) 1 else -1
            }
            return candidate
        }

    }
}
