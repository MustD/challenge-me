package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0013 = (String) -> Int

class I0013romanToInteger {

    data class Case(
        val s: String,
        val output: Int,
    )

    val prepareCase = { s: String, r: Int ->
        Case(s, r)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0013> {
        override val cases: List<Case> = listOf(
            prepareCase("III", 3),
            prepareCase("LVIII", 58),
            prepareCase("MCMXCIV", 1994),
        )
        override val solutions: List<Pair<String, I0013>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0013): Pair<Boolean, Any> {
            val result = solution(s)
            return (result == output) to result
        }

        @Test
        fun test() = check()


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
