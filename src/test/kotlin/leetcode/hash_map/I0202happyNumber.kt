package leetcode.hash_map

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0202 = (Int) -> Boolean


class I0202happyNumber {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0202> {

        override val cases = _root_ide_package_.leetcode.testCases<I0202>(
            19 expects true,
            2 expects false,
        )

        @Test
        fun test() = check(::solution1, ::isHappy)

        fun solution1(n: Int): Boolean {

            val index = mutableSetOf<Int>()
            var result = n

            while (result != 1) {
                val nums = result.toString().map { it.toString().toInt() }
                result = nums.sumOf { it * it }

                if (index.add(result).not()) return false
            }
            return true
        }

        fun isHappy(n: Int): Boolean {
            val memoSquare = mutableMapOf<Int, Int>()

            fun splitNumber(n: Int): List<Int> {
                if (n == 0) return listOf(0)
                val digits = mutableListOf<Int>()
                var temp = kotlin.math.abs(n) // Handle negative numbers
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
