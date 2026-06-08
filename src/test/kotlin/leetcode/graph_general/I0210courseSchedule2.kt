package leetcode.graph_general

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0210 = (Int, Array<IntArray>) -> IntArray

class I0210courseSchedule2 {

    @Nested
    inner class Solution : ProblemTest<I0210> {

        override val cases = testCases<I0210>(
            args(2, "[[1,0]]") expects "[0,1]",
            args(4, "[[1,0],[2,0],[3,1],[3,2]]") expects "[0,2,1,3]",
            args(1, "[]") expects "[0]",
        )

        @Test
        fun test() = check(::findOrder)

        fun findOrder(numCourses: Int, prerequisites: Array<IntArray>): IntArray {
            val graph = mutableMapOf<Int, MutableSet<Int>>()
            prerequisites.forEach { d ->
                val courseId = d[0]
                val courseDep = d[1]
                graph.getOrPut(courseId) { mutableSetOf() }.add(courseDep)
            }

            val done = mutableSetOf<Int>()
            val inStack = mutableSetOf<Int>()

            fun dfs(node: Int): Boolean {
                if (node in inStack) return false
                if (node in done) return true
                inStack.add(node)
                graph[node]?.forEach { dep -> if (!dfs(dep)) return false }
                inStack.remove(node)
                done.add(node)
                return true
            }

            repeat(numCourses) { node -> if (!dfs(node)) return intArrayOf() }

            return done.toIntArray()
        }

    }
}
