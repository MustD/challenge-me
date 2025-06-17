package leetcode.stack

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import java.util.Stack
import kotlin.collections.ArrayDeque
import kotlin.test.Test

typealias I0071 = (String) -> String

class I0071SimplifyPath {

    data class Case(
        val input: String,
        val output: String,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0071> {

        override val cases: List<Case> = listOf(
            Case("/home/", "/home"),
            Case("/home//foo/", "/home/foo"),
            Case("/home/user/Documents/../Pictures", "/home/user/Pictures"),
            Case("/../", "/"),
            Case("/.../a/../b/c/../d/./", "/.../b/d"),

            )
        override val solutions: List<Pair<String, I0071>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0071): Pair<Boolean, Any> {
            val result = solution(input)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solution1(path: String): String {
            val stack = ArrayDeque<String>()
            path.split("/").forEach {
                when (it) {
                    "." -> Unit
                    "" -> Unit
                    ".." -> stack.removeLastOrNull()
                    else -> stack.add(it)
                }
            }
            return "/" + stack.joinToString("/").removeSuffix("/")
        }

        private fun solutionEditorial(path: String): String {
            val stack = Stack<String>()
            path.split("/").forEach {
                when (it) {
                    "." -> Unit
                    "" -> Unit
                    ".." -> stack.removeLastOrNull()
                    else -> stack.add(it)
                }
            }
            return "/" + stack.joinToString("/").removeSuffix("/")
        }


    }
}
