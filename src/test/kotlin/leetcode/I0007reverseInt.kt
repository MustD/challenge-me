package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0007reverseInt {

    data class Case(
        val input: Int,
        val output: Int,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (Int) -> Int> {
        override val cases: List<Case> = listOf(
            Case(123, 321),
            Case(-123, -321),
            Case(120, 21),
        )
        override val solutions: List<Pair<String, (Int) -> Int>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionAi.name to ::solutionAi,
        )

        override fun Case.check(solution: (Int) -> Int): Pair<Boolean, Any> {
            val result = solution(input)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(x: Int): Int {
            return runCatching {
                when {
                    x > 0 -> x.toString().reversed().toInt()
                    else -> (x * -1).toString().reversed().toInt().let { it * -1 }
                }
            }.getOrDefault(0)
        }

        /**
         * Reverses the digits of an integer.
         *
         * @param x the integer whose digits need to be reversed
         * @return the reversed integer
         */
        fun solutionAi(x: Int): Int {
            var reversedNumber = 0
            var remainingNumber = x

            val isOverflow: (Int, Int) -> Boolean = { revNum, lastDigit ->
                (revNum > Int.MAX_VALUE / 10) or
                        (revNum == Int.MAX_VALUE / 10 && lastDigit > 7) or
                        (revNum < Int.MIN_VALUE / 10) or
                        (revNum == Int.MIN_VALUE / 10 && lastDigit < -8)
            }

            while (remainingNumber != 0) {
                val lastDigit = remainingNumber % 10
                if (isOverflow(reversedNumber, lastDigit)) return 0
                reversedNumber = reversedNumber * 10 + lastDigit
                remainingNumber /= 10
            }

            return reversedNumber
        }


    }
}
