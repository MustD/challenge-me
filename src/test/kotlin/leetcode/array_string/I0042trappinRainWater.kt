package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0042 = (IntArray) -> Int

class I0042trappinRainWater {

    data class Case(
        val nums: List<Int>,
        val output: Int,
    )

    val prepareCase = { n1: String, r: Int ->
        Case(n1.toIntArray().toList(), r)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0042> {
        override val cases: List<Case> = listOf(
            prepareCase("[0,1,0,2,1,0,1,3,2,1,2,1]", 6),
            prepareCase("[4,2,0,3,2,5]", 9)
        )
        override val solutions: List<Pair<String, I0042>> = listOf(
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0042): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()


        fun solutionEditorial(height: IntArray): Int {
            if (height.isEmpty()) return 0
            var ans = 0
            val leftMax = MutableList(height.size) { 0 }
            val rightMax = MutableList(height.size) { 0 }

            leftMax[0] = height[0]
            for (i in 1..height.lastIndex) {
                leftMax[i] = maxOf(leftMax[i - 1], height[i])
            }

            rightMax[height.lastIndex] = height[height.lastIndex]
            for (i in height.lastIndex - 1 downTo 0) {
                rightMax[i] = maxOf(rightMax[i + 1], height[i])
            }

            for (i in 1..height.lastIndex) {
                ans += minOf(leftMax[i], rightMax[i]) - height[i]
            }

            return ans
        }


    }
}
