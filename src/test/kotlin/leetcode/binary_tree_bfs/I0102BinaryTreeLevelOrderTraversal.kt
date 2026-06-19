package leetcode.binary_tree_bfs

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0102 = (TreeNode?) -> List<List<Int>>

class I0102BinaryTreeLevelOrderTraversal {

    @Nested
    inner class Solution : ProblemTest<I0102> {
        override val cases = testCases<I0102>(
            "[3,9,20,null,null,15,7]" expects "[[3],[9,20],[15,7]]",
            "[1]" expects "[[1]]",
            "[]" expects "[]",
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(root: TreeNode?): List<List<Int>> {
            val queue = ArrayDeque<TreeNode>()
            val result = mutableListOf<List<Int>>()
            queue.add(root ?: return result)

            while (queue.isNotEmpty()) {
                val levelSize = queue.size
                val levelValues = mutableListOf<Int>()

                for (i in 0 until levelSize) {
                    val node = queue.removeFirst()
                    levelValues.add(node.`val`)
                    node.left?.let(queue::add)
                    node.right?.let(queue::add)
                }

                result.add(levelValues)
            }
            return result
        }

    }
}
