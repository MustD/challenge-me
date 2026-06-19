package leetcode.binary_tree_bfs

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0637 = (TreeNode?) -> DoubleArray

class I0637averageOfLevelsInBinaryTree {

    @Nested
    inner class Solution : ProblemTest<I0637> {
        override val cases = testCases<I0637>(
            "[3,9,20,null,null,15,7]" expects "[3.0,14.5,11.00000]",
            "[3,9,20,15,7]" expects "[3.0,14.5,11.0]",
            "[3,1,5,0,2,4,6]" expects "3.0,3.0,3.0]",
        )

        @Test
        fun test() = check(::solution1, ::solution2)

        fun solution1(root: TreeNode?): DoubleArray {
            if (root == null) return doubleArrayOf()

            val queue: ArrayDeque<Pair<Int, TreeNode>> = ArrayDeque()
            queue.add(0 to root)
            val map = mutableMapOf<Int, MutableList<Double>>()

            while (queue.isNotEmpty()) {
                val (lvl, node) = queue.removeFirst()
                val current = map[lvl]
                if (current == null) {
                    map[lvl] = mutableListOf(node.`val`.toDouble())
                } else {
                    current.add(node.`val`.toDouble())
                }

                node.left?.let { queue.add(lvl + 1 to it) }
                node.right?.let { queue.add(lvl + 1 to it) }
            }

            return map.map { it.value.average() }.toDoubleArray()
        }

        fun solution2(root: TreeNode?): DoubleArray {
            val map = mutableMapOf<Int, MutableList<Double>>()
            fun dfs(node: TreeNode?, lvl: Int): Int {
                if (node == null) return lvl
                map.getOrPut(lvl) { mutableListOf() }.add(node.`val`.toDouble())

                return maxOf(
                    a = dfs(node.left, lvl + 1),
                    b = dfs(node.right, lvl + 1)
                )
            }

            dfs(root, 0)
            return map.map { it.value.average() }.toDoubleArray()
        }
    }
}
