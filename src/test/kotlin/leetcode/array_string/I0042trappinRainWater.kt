package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0042 = (IntArray) -> Int

class I0042trappinRainWater {

    @Nested
    inner class Solution : ProblemTest<I0042> {
        override val cases = testCases<I0042>(
            "[0,1,0,2,1,0,1,3,2,1,2,1]" expects 6,
            "[4,2,0,3,2,5]" expects 9,
        )

        @Test
        fun test() = check(::solutionEditorial)


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
