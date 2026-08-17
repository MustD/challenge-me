package leetcode.array_string

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

typealias I0006 = (String, Int) -> String

class I0006zigzagConversion {

    @Nested
    inner class Solution : ProblemTest<I0006> {

        override val cases = testCases<I0006>(
            args("ABCDEFGHIJKL", 3) expects "AEIBDFHJLCGK",
            args("PAYPALISHIRING", 3) expects "PAHNAPLSIIGYIR",
            args("PAYPALISHIRING", 4) expects "PINALSIGYAHRPI",
            args("A", 1) expects "A",
            args("AB", 1) expects "AB",
        )

        @Test
        fun test() = check(::solution1, ::solutionAI)

        fun solution1(s: String, numRows: Int): String {
            if (numRows == 1) return s
            if (s.length <= numRows) return s
            val field = (0..s.length).map { MutableList<Char?>(numRows) { null } }
            var current = 0 to 0
            var vector = 0 to 0
            s.forEach { char ->
                field[current.first][current.second] = char
                when (current.second) {
                    numRows - 1 -> vector = 1 to -1
                    0 -> vector = 0 to 1
                }
                current = (current.first + vector.first) to (current.second + vector.second)
            }
            return (0..<numRows).flatMap { y -> field.mapNotNull { it[y] } }.joinToString("")
        }

        fun solutionAI(s: String, numRows: Int): String {
            if (numRows == 1 || numRows >= s.length) return s
            val rows = Array(numRows) { StringBuilder() }
            var i = 0
            var direction = -1

            for (char in s) {
                rows[i].append(char)
                if (i == 0 || i == numRows - 1) direction *= -1
                i += direction
            }

            return rows.joinToString("") { it.toString() }
        }
    }
}
