package leetcode.divide_conquer

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0053 = (IntArray) -> Int

class I0053maximumSubarray {

    data class Case(
        val input: List<Int>,
        val output: Int,
    )

    fun prepareCase(s: String, out: Int) = Case(
        s.toIntArray().toList(),
        out
    )


    @Nested
    inner class Solution : AproblemTest<Case, I0053> {
        override val cases: List<Case> = listOf(
            prepareCase("[-2,1,-3,4,-1,2,1,-5,4]", 6),
            prepareCase("[1]", 1),
            prepareCase("[5,4,-1,7,8]", 23),
        )
        override val solutions: List<Pair<String, I0053>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0053): Pair<Boolean, Any> {
            val result = solution(input.toIntArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(nums: IntArray): Int {
            var max = nums[0]
            var curr = 0
            for (n in nums) {
                curr = maxOf(curr + n, n)
                max = maxOf(max, curr)
            }
            return max
        }


    }
}
