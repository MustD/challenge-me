package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0238 = (IntArray) -> IntArray

class I0238productOfArrayExceptSelf {

    @Nested
    inner class Solution : ProblemTest<I0238> {
        override val cases = testCases<I0238>(
            "[1,2,3,4]" expects "[24,12,8,6]",
            "[-1,1,0,-3,3]" expects "[0,0,9,0,0]",
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial)

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
