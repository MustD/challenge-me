package leetcode.math

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0066 = (IntArray) -> IntArray

class I0066plusOne {

    @Nested
    inner class Solution : ProblemTest<I0066> {

        override val cases = testCases<I0066>(
            "[1,2,3]" expects "[1,2,4]",
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(digits: IntArray): IntArray {
            var output = digits.clone()
            fun addOne(idx: Int) {
                if (idx < 0) {
                    output = intArrayOf(1).plus(output)
                    return
                }
                val num = output[idx] + 1
                output[idx] = num % 10
                if (num >= 10) addOne(idx - 1)
            }

            addOne(output.lastIndex)
            return output
        }


    }
}
