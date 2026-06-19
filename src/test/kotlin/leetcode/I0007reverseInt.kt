package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0007 = (Int) -> Int

class I0007reverseInt {

    @Nested
    inner class Solution : ProblemTest<I0007> {
        override val cases = testCases<I0007>(
            123 expects 321,
            -123 expects -321,
            120 expects 21,
        )

        @Test
        fun test() = check(::solution1, ::solutionAi)

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
