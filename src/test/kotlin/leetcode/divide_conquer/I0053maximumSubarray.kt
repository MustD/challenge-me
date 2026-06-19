package leetcode.divide_conquer

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0053 = (IntArray) -> Int

class I0053maximumSubarray {

    @Nested
    inner class Solution : ProblemTest<I0053> {
        override val cases = testCases<I0053>(
            "[-2,1,-3,4,-1,2,1,-5,4]" expects 6,
            "[1]" expects 1,
            "[5,4,-1,7,8]" expects 23,
        )

        @Test
        fun test() = check(::solution1)

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
