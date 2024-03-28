import org.junit.jupiter.api.Nested
import kotlin.test.Test


/**
 * https://leetcode.com/problems/longest-substring-without-repeating-characters/description/
 */
class I003longestSubstring {
    data class Case(
        val input: String,
        val output: Int,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (String) -> Int> {

        override val cases: List<Case> = listOf(
            Case("abcabcbb", 3)
        )
        override val solutions: List<Pair<String, (String) -> Int>> = listOf(
            ::solution1.name to ::solution1
        )

        override fun Case.check(solution: (String) -> Int): Boolean {
            return solution(input) == output
        }

        @Test
        fun test() = check()

        //todo
        private fun solution1(s: String): Int {
            return 1
        }


    }
}