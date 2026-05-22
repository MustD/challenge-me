package leetcode.graph_general

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0399 = (List<List<String>>, DoubleArray, List<List<String>>) -> DoubleArray

class I0399evaluateDivision {

    @Nested
    inner class Solution : ProblemTest<I0399> {

        override val cases = testCases<I0399>(
            args(
                """[["a","b"],["b","c"]]""",
                """[2.0,3.0]""",
                """[["a","c"],["b","a"],["a","e"],["a","a"],["x","x"]]"""
            ) expects "[6.0,0.5,-1.0,1.0,-1.0]",
        )

        @Test
        fun test() = check(::calcEquation)

        fun calcEquation(equations: List<List<String>>, values: DoubleArray, queries: List<List<String>>): DoubleArray {

            val graph = HashMap<String, HashMap<String, Double>>()

            equations.forEachIndexed { i, (a, b) ->
                // a/b = x, b/a = 1/x
                graph.getOrPut(a) { HashMap() }[b] = values[i]
                graph.getOrPut(b) { HashMap() }[a] = 1.0 / values[i]
            }

            fun bfs(src: String, dst: String): Double {
                if (graph.containsKey(src).not() || graph.containsKey(dst).not()) return -1.0
                if (src == dst) return 1.0

                val visited = HashSet<String>()
                val queue = ArrayDeque<Pair<String, Double>>()
                queue.add(src to 1.0)
                visited.add(src)

                // a/c = (a/b) × (b/c) = 2.0 × 3.0 = 6.0
                while (queue.isNotEmpty()) {
                    val (node, product) = queue.removeFirst()
                    graph[node]?.forEach { (neighbor, weight) ->
                        if (neighbor == dst) return product * weight
                        if (visited.contains(neighbor).not()) {
                            visited.add(neighbor)
                            queue.add(neighbor to product * weight)
                        }
                    }
                }
                return -1.0
            }


            // Step 3: Answer each query
            return DoubleArray(queries.size) { i ->
                queries[i].let { (a, b) -> bfs(a, b) }
            }
        }

    }
}
