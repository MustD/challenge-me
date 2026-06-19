package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0013 = (String) -> Int

class I0013romanToInteger {

    @Nested
    inner class Solution : ProblemTest<I0013> {
        override val cases = testCases<I0013>(
            "III" expects 3,
            "LVIII" expects 58,
            "MCMXCIV" expects 1994,
        )

        @Test
        fun test() = check(::solution1)


        fun solution1(s: String): Int {

            val nums = mapOf(
                'I' to 1, 'V' to 5, 'X' to 10, 'L' to 50,
                'C' to 100, 'D' to 500, 'M' to 1000,
            )

            var result = 0
            for (i in (0..s.lastIndex - 1)) {
                val num = nums.getOrElse(s[i]) { throw IllegalArgumentException() }
                val next = nums.getOrElse(s[i + 1]) { throw IllegalArgumentException() }
                if (num < next) {
                    result -= num
                } else {
                    result += num
                }
            }
            result += nums.getOrElse(s.last()) { throw IllegalArgumentException() }
            return result
        }


    }
}
