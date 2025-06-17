package leetcode.array_string

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0014 = (Array<String>) -> String

class I0014longestCommonPrefix {

    data class Case(
        val strs: List<String>,
        val output: String,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0014> {
        override val cases: List<Case> = listOf(
            Case(listOf("flower", "flow", "flight"), "fl"),
            Case(listOf("dog", "racecar", "car"), ""),
        )
        override val solutions: List<Pair<String, I0014>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0014): Pair<Boolean, Any> {
            val result = solution(strs.toTypedArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()


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
