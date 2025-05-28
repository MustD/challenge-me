package leetcode.two_pointers

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0011 = (IntArray) -> Int

class I0011ContainerWithMostWater {

    data class Case(
        val nums: List<Int>,
        val output: Int,
    )

    val prepareCase = { n1: String, r: Int ->
        Case(n1.toIntArray().toList(), r)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0011> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,8,6,2,5,4,8,3,7]", 49),
            prepareCase("[1,1]", 1),
            prepareCase("[8,7,2,1]", 7),
        )
        override val solutions: List<Pair<String, I0011>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0011): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()


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
