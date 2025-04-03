package leetcode.array_string

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test


class I0151ReverseWordsInAString {
    data class Case(
        val input: String,
        val output: String,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (String) -> String> {

        override val cases: List<Case> = listOf(
            Case("the sky is blue", "blue is sky the"),
            Case("  hello world  ", "world hello"),
            Case("a good   example", "example good a"),
        )

        override val solutions: List<Pair<String, (String) -> String>> = listOf(
            "solution1" to ::solution1,
        )

        override fun Case.check(solution: (String) -> String): Pair<Boolean, Any> {
            val result = solution(input)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solution1(s: String): String {
            return s.split(" ").mapNotNull {
                if (it.isBlank()) null else it.trim()
            }.reversed().joinToString(" ")
        }

    }
}
