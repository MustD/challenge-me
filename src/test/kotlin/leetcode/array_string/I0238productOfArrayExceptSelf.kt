package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0238 = (IntArray) -> IntArray

class I0238productOfArrayExceptSelf {

    data class Case(
        val nums: List<Int>,
        val output: List<Int>,
    )

    val prepareCase = { n1: String, r: String ->
        Case(n1.toIntArray().toList(), r.toIntArray().toList())
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0238> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,2,3,4]", "[24,12,8,6]"),
            prepareCase("[-1,1,0,-3,3]", "[0,0,9,0,0]"),
        )
        override val solutions: List<Pair<String, I0238>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0238): Pair<Boolean, Any> {
            val array = nums.toIntArray()
            val result = solution(array)
            return (result.toList() == output) to result.toList()
        }

        @Test
        fun test() = check()

        /**
         * bruteforce TLE
         */
        fun solution1(nums: IntArray): IntArray {
            val result = IntArray(nums.size) { 1 }
            for (i in nums.indices) {
                for (j in nums.indices) {
                    if (i == j) continue
                    result[i] *= nums[j]
                }
            }
            return result
        }

        fun solutionEditorial(nums: IntArray): IntArray {
            val size = nums.size
            val left = IntArray(size)
            val right = IntArray(size)
            left[0] = 1
            right[size - 1] = 1

            for (i in 1..left.lastIndex) {
                left[i] = left[i - 1] * nums[i - 1]
            }
            for (i in right.lastIndex - 1 downTo 0) {
                right[i] = right[i + 1] * nums[i + 1]
            }

            val answer = IntArray(size)
            for (i in nums.indices) {
                answer[i] = left[i] * right[i]
            }
            return answer
        }


    }
}
