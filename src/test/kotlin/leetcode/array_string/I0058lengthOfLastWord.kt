package leetcode.array_string

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0058 = (String) -> Int

class I0058lengthOfLastWord {

    data class Case(
        val s: String,
        val output: Int,
    )

    val prepareCase = { s: String, r: Int ->
        Case(s, r)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0058> {
        override val cases: List<Case> = listOf(
            prepareCase("Hello World", 5),
            prepareCase("   fly me   to   the moon  ", 4),
            prepareCase("luffy is still joyboy", 6),
        )
        override val solutions: List<Pair<String, I0058>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0058): Pair<Boolean, Any> {
            val result = solution(s)
            return (result == output) to result
        }

        @Test
        fun test() = check()


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
