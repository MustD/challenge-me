package leetcode.array_string

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.math.pow
import kotlin.test.Test

private typealias I0012 = (Int) -> String

class I0012integerToRoman {

    data class Case(
        val num: Int,
        val output: String,
    )

    val prepareCase = { s: Int, r: String ->
        Case(s, r)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0012> {
        override val cases: List<Case> = listOf(
            prepareCase(3, "III"),
            prepareCase(58, "LVIII"),
            prepareCase(1994, "MCMXCIV"),
            prepareCase(3749, "MMMDCCXLIX"),
        )
        override val solutions: List<Pair<String, I0012>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0012): Pair<Boolean, Any> {
            val result = solution(num)
            return (result == output) to result
        }

        @Test
        fun test() = check()


        fun solution1(num: Int): String {
            val dict = mapOf(
                1 to "I", 2 to "II", 3 to "III", 4 to "IV", 5 to "V",
                6 to "VI", 7 to "VII", 8 to "VIII", 9 to "IX",
                10 to "X", 20 to "XX", 30 to "XXX", 40 to "XL", 50 to "L",
                60 to "LX", 70 to "LXX", 80 to "LXXX", 90 to "XC",
                100 to "C", 200 to "CC", 300 to "CCC", 400 to "CD", 500 to "D",
                600 to "DC", 700 to "DCC", 800 to "DCCC", 900 to "CM",
                1000 to "M", 2000 to "MM", 3000 to "MMM",
            )
            val numStr = num.toString().reversed()
            val result = mutableListOf<String>()
            for (i in numStr.indices) {
                val value = numStr[i].digitToInt() * 10.0.pow(i)
                result.add(dict[value.toInt()] ?: "")
            }
            return result.reversed().joinToString("")
        }

        fun solutionEditorial(num: Int): String {
            val dict = mapOf(
                1 to "I", 2 to "II", 3 to "III", 4 to "IV", 5 to "V",
                6 to "VI", 7 to "VII", 8 to "VIII", 9 to "IX",
                10 to "X", 20 to "XX", 30 to "XXX", 40 to "XL", 50 to "L",
                60 to "LX", 70 to "LXX", 80 to "LXXX", 90 to "XC",
                100 to "C", 200 to "CC", 300 to "CCC", 400 to "CD", 500 to "D",
                600 to "DC", 700 to "DCC", 800 to "DCCC", 900 to "CM",
                1000 to "M", 2000 to "MM", 3000 to "MMM",
            )

            val thousands = num / 1000
            val hundreds = (num % 1000) / 100
            val tens = (num % 100) / 10
            val ones = (num % 10)
            return dict.getOrDefault(thousands * 1000, "") +
                    dict.getOrDefault(hundreds * 100, "") +
                    dict.getOrDefault(tens * 10, "") +
                    dict.getOrDefault(ones, "")

        }


    }
}
