package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0124 = (TreeNode?) -> Int

class I0124binaryTreeMaximumPathSum {

    @Nested
    inner class Solution : ProblemTest<I0124> {

        override val cases = testCases<I0124>(
            "[1,2,3]" expects 6,
            "[0,1,1]" expects 2,
            "[7,9,-8,-6,-4,null,null,7,-2,null,null,null,null,-6]" expects 17,
        )

        @Test
        fun test() = check(::maxPathSum, ::maxPathSum_broken)

        fun maxPathSum(root: TreeNode?): Int {
            if (root == null) return Int.MIN_VALUE

            var max = Int.MIN_VALUE

            // Returns the best single-arm gain downward from this node.
            // A negative gain is discarded (we take 0 instead — skip that subtree).
            fun dfs(node: TreeNode?): Int {
                if (node == null) return 0

                val leftGain = maxOf(0, dfs(node.left))
                val rightGain = maxOf(0, dfs(node.right))

                // Best path that "bends" at this node (uses both arms)
                val pathThroughNode = node.`val` + leftGain + rightGain
                max = maxOf(max, pathThroughNode)

                // Only one arm can be passed upward
                return node.`val` + maxOf(leftGain, rightGain)
            }

            dfs(root)
            return max
        }

        //not working on leetcode side and O(n^2)
        fun maxPathSum_broken(root: TreeNode?): Int {
            if (root == null) return Int.MIN_VALUE

            data class GraphNode(
                val value: Int,
                private val number: Long,
            ) {
                val rels: MutableList<GraphNode> = mutableListOf()
            }

            val allNodes = mutableListOf<GraphNode>()
            fun dfs(node: TreeNode = root, parent: GraphNode? = null, num: Long = 1) {
                val current = GraphNode(node.`val`, num).also { allNodes.add(it) }
                parent?.let {
                    it.rels.add(current)
                    current.rels.add(it)
                }
                node.left?.let { dfs(it, current, num * 2) }
                node.right?.let { dfs(it, current, num * 2 + 1) }
            }
            dfs()

            var max = Int.MIN_VALUE
            allNodes.forEach { start ->
                val visited = mutableSetOf<GraphNode>()
                fun dfs(node: GraphNode, sum: Int = 0) {
                    if (visited.contains(node)) return
                    visited.add(node)
                    val pathSum = sum + node.value
                    if (max < pathSum) max = pathSum
                    node.rels.forEach { dfs(it, pathSum) }
                }
                dfs(start)
            }

            return max
        }

    }
}
