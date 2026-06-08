package leetcode.graph_general

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0207 = (Int, Array<IntArray>) -> Boolean

class I0207courseSchedule {

    @Nested
    inner class Solution : ProblemTest<I0207> {

        override val cases = testCases<I0207>(
            args(2, "[[0,1]]") expects true,
            args(2, "[[1,0],[0,1]]") expects false,
            args(5, "[[1,4],[2,4],[3,1],[3,2]]") expects true,
        )

        @Test
        fun test() = check(::canFinish, ::canFinishKahn)

        fun canFinish(numCourses: Int, prerequisites: Array<IntArray>): Boolean {
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

            return (0 until numCourses).all { dfs(it) }
        }


        fun canFinishKahn(numCourses: Int, prerequisites: Array<IntArray>): Boolean {
            val inDegree = IntArray(numCourses)
            val graph = Array(numCourses) { mutableListOf<Int>() }

            for ((course, prereq) in prerequisites) {
                graph[prereq].add(course)  // prereq unlocks course
                inDegree[course]++
            }

            val queue = ArrayDeque<Int>()
            for (i in 0 until numCourses) {
                if (inDegree[i] == 0) queue.add(i)
            }

            var processed = 0
            while (queue.isNotEmpty()) {
                val node = queue.removeFirst()
                processed++
                for (neighbor in graph[node]) {
                    if (--inDegree[neighbor] == 0) queue.add(neighbor)
                }
            }

            return processed == numCourses
        }

    }
}
