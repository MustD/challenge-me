package leetcode.stack

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0020 = (String) -> Boolean

class I0020validParentheses {

    @Nested
    inner class Solution : ProblemTest<I0020> {

        override val cases = testCases<I0020>(
            "()" expects true,
            "()[]{}" expects true,
            "(]" expects false,
            "([])" expects true,
            "[" expects false,
        )

        @Test
        fun test() = check(::solution1)

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
