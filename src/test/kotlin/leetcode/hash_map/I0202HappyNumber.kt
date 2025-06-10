package leetcode.hash_map

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0202 = (Int) -> Boolean

class I0202HappyNumber {
    data class Case(
        val n: Int,
        val output: Boolean,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0202> {

        override val cases: List<Case> = listOf(
            Case(19, true),
            Case(2, false),
        )
        override val solutions: List<Pair<String, I0202>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0202): Pair<Boolean, Any> {
            val result = solution(n)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        private fun solution1(n: Int): Boolean {
            val memoSquare = mutableMapOf<Int, Int>()

            fun splitNumber(n: Int): List<Int> {
                if (n == 0) return listOf(0)
                val digits = mutableListOf<Int>()
                var temp = kotlin.math.abs(n) // Handle the negative numbers not necessary but whatever
                while (temp > 0) {
                    digits.add(temp % 10)
                    temp /= 10
                }
                return digits.reversed()
            }

            fun sumOfSquares(n: Int): Int {
                val result = splitNumber(n).sumOf {
                    memoSquare.getOrPut(it) { it * it }
                }
                return result
            }

            var sum = n
            while (sum > 9) {
                sum = sumOfSquares(sum)
            }
            return sum == 1 || sum == 7
        }


    }
}
