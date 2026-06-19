package leetcode.math

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0009 = (Int) -> Boolean

/**
 * https://leetcode.com/problems/palindrome-number/
 */
class I0009palindromeNumber {

    @Nested
    inner class Solution : ProblemTest<I0009> {
        override val cases = testCases<I0009>(
            121 expects true,
            -121 expects false,
            1122 expects false,
            10 expects false,
        )

        @Test
        fun test() = check(::solution1, ::solutionAi)

        private fun solution1(x: Int): Boolean {
            if (x < 0) return false
            if (x < 10) return true
            val string = x.toString()
            (0..string.length / 2).forEach {
                val iStart = it
                val iEnd = string.length - it - 1
                if (iStart == iEnd) return true
                if (string[iStart] != string[iEnd]) return false
            }
            return true
        }

        private fun solutionAi(x: Int): Boolean {
            if (x < 0) return false

            var original = x
            var reversed = 0

            while (original != 0) {
                val digit = original % 10
                reversed *= 10
                reversed += digit
                original /= 10
            }

            return x == reversed
        }


    }
}
