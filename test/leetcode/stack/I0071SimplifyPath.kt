package leetcode.stack

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import java.util.Stack
import kotlin.collections.ArrayDeque
import kotlin.test.Test

typealias I0071 = (String) -> String

class I0071SimplifyPath {

    @Nested
    inner class Solution : ProblemTest<I0071> {

        override val cases = testCases<I0071>(
            "/home/" expects "/home",
            "/home//foo/" expects "/home/foo",
            "/home/user/Documents/../Pictures" expects "/home/user/Pictures",
            "/../" expects "/",
            "/.../a/../b/c/../d/./" expects "/.../b/d",
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial)

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
