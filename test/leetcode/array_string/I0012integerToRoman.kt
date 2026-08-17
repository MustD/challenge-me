package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.math.pow
import kotlin.test.Test

private typealias I0012 = (Int) -> String

class I0012integerToRoman {

    @Nested
    inner class Solution : ProblemTest<I0012> {
        override val cases = testCases<I0012>(
            3 expects "III",
            58 expects "LVIII",
            1994 expects "MCMXCIV",
            3749 expects "MMMDCCXLIX",
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial)


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
