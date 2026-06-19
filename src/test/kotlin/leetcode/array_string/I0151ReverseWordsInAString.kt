package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0151 = (String) -> String

class I0151ReverseWordsInAString {

    @Nested
    inner class Solution : ProblemTest<I0151> {

        override val cases = testCases<I0151>(
            "the sky is blue" expects "blue is sky the",
            "  hello world  " expects "world hello",
            "a good   example" expects "example good a",
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(s: String): String {
            return s.split(" ").mapNotNull {
                if (it.isBlank()) null else it.trim()
            }.reversed().joinToString(" ")
        }

    }
}
