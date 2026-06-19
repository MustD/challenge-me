package leetcode.binary_search

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0035 = (IntArray, Int) -> Int

class I0035searchInsertPosition {

    @Nested
    inner class Solution : ProblemTest<I0035> {
        override val cases = testCases<I0035>(
            args("1,3,5,6", 5) expects 2,
            args("1,3,5,6", 2) expects 1,
            args("1,3,5,6", 7) expects 4,
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(nums: IntArray, target: Int): Int {
            fun srch(s: Int, e: Int): Int {
                val size = e - s
                val m = s + (size / 2) //searching middle index
                return when {
                    nums[m] == target -> m //if middle value == target return m index

                    nums[m] > target -> {
                        if (size == 0) return m
                        srch(s, m)
                    } //if middle value > target take left part

                    nums[m] < target -> {
                        if (size == 0) return m + 1
                        srch(m + 1, e)
                    } //if middle value < target take right part
                    else -> -1
                }
            }

            return srch(0, nums.lastIndex)
        }


    }
}
