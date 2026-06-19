package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0058 = (String) -> Int

class I0058lengthOfLastWord {

    @Nested
    inner class Solution : ProblemTest<I0058> {
        override val cases = testCases<I0058>(
            "Hello World" expects 5,
            "   fly me   to   the moon  " expects 4,
            "luffy is still joyboy" expects 6,
        )

        @Test
        fun test() = check(::solution1)


        fun solution1(s: String): Int {
            var size = 0
            val letters = (('a'..'z') + ('A'..'Z')).toSet()
            for (i in s.lastIndex downTo 0) {
                if (letters.contains(s[i])) {
                    size++
                } else {
                    if (size > 0) return size
                }
            }
            return size
        }

        fun solutionEditorial(s: String): Int {
            var size = 0
            for (i in s.lastIndex downTo 0) {
                if (s[i] != ' ') {
                    size++
                } else {
                    if (size > 0) return size
                }
            }
            return size
        }


    }
}
