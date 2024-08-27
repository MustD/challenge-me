import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * https://leetcode.com/problems/palindrome-number/
 */
class I0009palindromeNumber {
    data class Case(
        val input: Int,
        val output: Boolean,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (Int) -> Boolean> {
        override val cases: List<Case> = listOf(
            Case(121, true),
            Case(-121, false),
            Case(1122, false),
            Case(10, false),
        )
        override val solutions: List<Pair<String, (Int) -> Boolean>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionAi.name to ::solutionAi,
        )

        override fun Case.check(solution: (Int) -> Boolean): Pair<Boolean, Any> {
            val result = solution(input)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solution1(x: Int): Boolean {
            if (x < 0) return false
            if (x < 10) return true
            val string = x.toString()
            (0..string.length / 2).forEach {
                val iStart = it
                val iEnd = string.length - it - 1
                if (iStart == iEnd) return true
                if (string[iStart] != string[iEnd]) return false
            }
            return true
        }

        private fun solutionAi(x: Int): Boolean {
            if (x < 0) return false

            var original = x
            var reversed = 0

            while (original != 0) {
                val digit = original % 10
                reversed *= 10
                reversed += digit
                original /= 10
            }

            return x == reversed
        }


    }
}