package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0283 = (IntArray) -> IntArray

class I0283moveZeroes {

    @Nested
    inner class Solution : ProblemTest<I0283> {

        override val cases = testCases<I0283>(
            "[0,1,0,3,12]" expects "[1,3,12,0,0]",
            "[0]" expects "[0]",
        )

        @Test
        fun test() = check(::moveZeroes)

        fun moveZeroes(nums: IntArray): IntArray {
            var lastNotZero = -1

            for (i in nums.indices) {
                if (nums[i] != 0) {
                    lastNotZero++
                    val tmp = nums[i]
                    nums[i] = nums[lastNotZero]
                    nums[lastNotZero] = tmp
                }
            }
            return nums
        }

    }
}
