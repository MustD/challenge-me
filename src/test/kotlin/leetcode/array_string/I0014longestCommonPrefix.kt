package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0014 = (Array<String>) -> String

class I0014longestCommonPrefix {

    @Nested
    inner class Solution : ProblemTest<I0014> {
        override val cases = testCases<I0014>(
            """["flower","flow","flight"]""" expects "fl",
            """["dog","racecar","car"]""" expects "",
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial)


        /**
         * Horizontal scan
         */
        fun solution1(strs: Array<String>): String {
            var prefix = strs.first()
            for (i in 1..strs.lastIndex) {
                val str = strs[i]
                for (c in prefix.indices) {
                    if (prefix[c] != str.getOrNull(c)) {
                        prefix = prefix.substring(0, c)
                        break
                    }
                }
            }

            return prefix
        }

        /**
         * Vertical scan
         */
        fun solutionEditorial(strs: Array<String>): String {
            val prefix = mutableListOf<Char>()
            for (i in strs.first().indices) {
                val c = strs.first()[i]
                for (s in strs) {
                    if (c != s.getOrNull(i)) return prefix.joinToString("")
                }
                prefix.add(c)
            }
            return prefix.joinToString("")
        }

    }
}
