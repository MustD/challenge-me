package leetcode.two_pointers

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0125 = (String) -> Boolean

class I0125validPalindrome {

    @Nested
    inner class Solution : ProblemTest<I0125> {
        override val cases = testCases<I0125>(
            "A man, a plan, a canal: Panama" expects true,
            "race a car" expects false,
            " " expects true,
        )

        @Test
        fun test() = check(::solution1, ::solution2, ::solution3)

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
