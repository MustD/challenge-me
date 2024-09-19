import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0235validPalindrome {

    data class Case(
        val input: String,
        val output: Boolean,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (String) -> Boolean> {
        override val cases: List<Case> = listOf(
            Case("A man, a plan, a canal: Panama", true),
            Case("race a car", false),
            Case(" ", true),
        )
        override val solutions: List<Pair<String, (String) -> Boolean>> = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
            ::solution3.name to ::solution3,
        )

        override fun Case.check(solution: (String) -> Boolean): Pair<Boolean, Any> {
            val result = solution(input)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(s: String): Boolean {
            val chars = ('a'..'z').union('0'..'9').toSet()
            val clean = s.mapNotNull { char ->
                if (chars.contains(char.lowercaseChar())) char.lowercaseChar() else null
            }
            return clean == clean.reversed()
        }

        fun solution2(s: String): Boolean {
            val chars = ('a'..'z').union('0'..'9').toSet()
            var begin = 0
            var end = s.lastIndex
            while (begin <= end) {
                val beginChar = s.getOrNull(begin)?.lowercaseChar()
                val endChar = s.getOrNull(end)?.lowercaseChar()
                if (chars.contains(beginChar).not()) {
                    begin++; continue
                }
                if (chars.contains(endChar).not()) {
                    end--; continue
                }
                if (beginChar != endChar) return false
                begin++; end--
            }
            return true
        }

        fun solution3(s: String): Boolean {
            var begin = 0
            var end = s.lastIndex
            while (begin <= end) {
                val beginChar = s[begin]
                val endChar = s[end]
                if (beginChar.isLetter().not() && beginChar.isDigit().not()) {
                    begin++; continue
                }
                if (endChar.isLetter().not() && endChar.isDigit().not()) {
                    end--; continue
                }
                if (beginChar.lowercaseChar() != endChar.lowercaseChar()) return false
                begin++; end--
            }
            return true
        }

    }
}