package leetcode.two_pointers

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0011 = (IntArray) -> Int

class I0011ContainerWithMostWater {

    @Nested
    inner class Solution : ProblemTest<I0011> {
        override val cases = testCases<I0011>(
            "[1,8,6,2,5,4,8,3,7]" expects 49,
            "[1,1]" expects 1,
            "[8,7,2,1]" expects 7,
        )

        @Test
        fun test() = check(::solution1)


        fun solution1(height: IntArray): Int {
            var left = 0
            var right = height.lastIndex
            var max = 0
            while (left < right) {
                println("leftI: $left, rightI: $right")
                val leftH = height[left]
                val rightH = height[right]

                val xSize = right - left
                val ySize = minOf(leftH, rightH)
                val area = xSize * ySize

                println("height[left]: ${height[left]}, height[right]: ${height[right]}, area: $area")
                max = maxOf(area, max)
                if (leftH > rightH) {
                    right--
                } else {
                    left++
                }
            }
            return max
        }


    }
}
