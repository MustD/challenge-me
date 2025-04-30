package leetcode.sliding_window

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0209 = (Int, IntArray) -> Int

class I0209minimumSizeSubarraySum {
    data class Case(
        val target: Int,
        val nums: List<Int>,
        val output: Int,
    )

    val prepareCase = { t: Int, s: String, o: Int ->
        Case(t, s.toIntArray().toList(), o)
    }

    @Nested
    inner class Solution : AproblemTest<Case, I0209> {

        override val cases: List<Case> = listOf(
            prepareCase(7, "[2,3,1,2,4,3]", 2),
            prepareCase(4, "[1,4,4]", 1),
            prepareCase(11, "[1,1,1,1,1,1,1,1]", 0),
        )
        override val solutions: List<Pair<String, I0209>> = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
        )

        override fun Case.check(solution: I0209): Pair<Boolean, Any> {
            val result = solution(target, nums.toIntArray())
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

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
