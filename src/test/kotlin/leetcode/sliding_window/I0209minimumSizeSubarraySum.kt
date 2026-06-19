package leetcode.sliding_window

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0209 = (Int, IntArray) -> Int

class I0209minimumSizeSubarraySum {

    @Nested
    inner class Solution : ProblemTest<I0209> {

        override val cases = testCases<I0209>(
            args(7, "[2,3,1,2,4,3]") expects 2,
            args(4, "[1,4,4]") expects 1,
            args(11, "[1,1,1,1,1,1,1,1]") expects 0,
        )

        @Test
        fun test() = check(::solution1, ::solution2)

        private fun solution1(target: Int, nums: IntArray): Int {
            var left = 0
            var sum = 0
            var minLen = Int.MAX_VALUE
            for (right in nums.indices) {
                sum += nums[right]
                while (sum >= target) {
                    minLen = minOf(minLen, right - left + 1)
                    sum -= nums[left]
                    left++
                }
            }
            return if (minLen == Int.MAX_VALUE) 0 else minLen
        }

        private fun solution2(target: Int, nums: IntArray): Int {
            tailrec fun recWind(left: Int = 0, right: Int = 0, minSize: Int = Int.MAX_VALUE, sum: Int = 0): Int {
                val valid = sum < target
                if (right == nums.lastIndex && valid) return minSize
                return recWind(
                    left = if (valid) left else left + 1,
                    right = if (valid) right + 1 else right,
                    minSize = if (valid) minSize else minOf(right - left + 1, minSize),
                    sum = if (valid) sum + nums[right + 1] else sum - nums[left],
                )
            }

            return recWind(sum = nums[0]).let { if (it == Int.MAX_VALUE) 0 else it }
        }

    }
}
