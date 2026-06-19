package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0026 = (IntArray) -> Int

class I0026removeDuplicatesFromSortedArray {

    @Nested
    inner class Solution : ProblemTest<I0026> {

        override val cases = testCases<I0026>(
            "[1,1,2]" expects 2,
            "[0,0,1,1,1,2,2,3,3,4]" expects 5,
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(nums: IntArray): Int {
            var insertIdx = 1
            for (i in 1..nums.lastIndex) {
                if (nums[i] != nums[i - 1]) {
                    nums[insertIdx] = nums[i]
                    insertIdx++
                }
            }
            return insertIdx
        }


    }
}
