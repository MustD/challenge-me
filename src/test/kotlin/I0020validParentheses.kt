import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0020 = (String) -> Boolean

class I0020validParentheses {

    data class Case(
        val input: String,
        val output: Boolean,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0020> {

        override val cases: List<Case> = listOf(
            Case("()", true),
            Case("()[]{}", true),
            Case("(]", false),
            Case("([])", true),
            Case("[", false),

            )
        override val solutions: List<Pair<String, I0020>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0020): Pair<Boolean, Any> {
            val result = solution(input)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solution1(s: String): Boolean {
            val stack = ArrayDeque<Char>()
            val matchingBrackets = mapOf(
                ')' to '(',
                ']' to '[',
                '}' to '{'
            )

            for (c in s) {
                if (c in matchingBrackets.keys) {
                    stack.lastOrNull() ?: return false
                    if (stack.removeLast() != matchingBrackets[c]) return false
                } else {
                    stack.addLast(c)
                }
            }

            return stack.isEmpty()
        }


    }
}
