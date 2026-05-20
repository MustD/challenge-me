package leetcode.graph_general

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.Node
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0133 = (Node?) -> Node?

class I0133cloneGraph {

    @Nested
    inner class Solution : ProblemTest<I0133> {

        override val cases = testCases<I0133>(
            "[[2,4],[1,3],[2,4],[1,3]]" expects "[[2,4],[1,3],[2,4],[1,3]]",
            "[[]]" expects "[[]]",
        )

        @Test
        fun test() = check(::cloneGraph)

        fun cloneGraph(node: Node?): Node? {
            if (node == null) return null
            if (node.neighbors.isEmpty()) return Node(node.`val`)

            val visited = mutableMapOf<Node, Node>()
            fun copy(what: Node = node): Node {
                visited[what]?.let { return it }
                val new = Node(what.`val`)
                visited[what] = new
                val newNeighbors = what.neighbors.filterNotNull().map { copy(it) }
                new.neighbors.addAll(newNeighbors)
                return new
            }
            return copy()

        }

    }
}
