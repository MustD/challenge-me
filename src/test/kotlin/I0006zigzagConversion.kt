import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class I0006zigzagConversion {
    data class Case(
        val string: String,
        val numRows: Int,
        val output: String,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (String, Int) -> String> {

        override val cases: List<Case> = listOf(
            Case("ABCDEFGHIJKL", 3, "AEIBDFHJLCGK"),
            Case("PAYPALISHIRING", 3, "PAHNAPLSIIGYIR"),
            Case("PAYPALISHIRING", 4, "PINALSIGYAHRPI"),
            Case("A", 1, "A"),
            Case("AB", 1, "AB")
        )

        override val solutions: List<Pair<String, (String, Int) -> String>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionAI.name to ::solutionAI,
        )

        override fun Case.check(solution: (String, Int) -> String): Pair<Boolean, Any> {
            val result = solution(string, numRows)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(s: String, numRows: Int): String {
            if(numRows == 1) return s
            if(s.length <= numRows) return s
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
            return (0..< numRows).flatMap { y -> field.mapNotNull { it[y] } }.joinToString("")
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