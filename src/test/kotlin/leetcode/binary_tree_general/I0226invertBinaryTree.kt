package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0226 = (TreeNode?) -> TreeNode?

class I0226invertBinaryTree {

    @Nested
    inner class Solution : ProblemTest<I0226> {
        override val cases = testCases<I0226>(
            "[4,2,7,1,3,6,9]" expects "[4,7,2,9,6,3,1]",
            "[2,1,3]" expects "[2,3,1]",
            "[]" expects "[]",
        )

        @Test
        fun test() = check(::solution1, ::solution2)

        fun solution1(root: TreeNode?): TreeNode? {
            if (root == null) return null
            val queue = ArrayDeque<TreeNode>()
            queue.add(root)
            while (queue.isNotEmpty()) {
                val node = queue.removeFirst()
                node.left?.let(queue::add)
                node.right?.let(queue::add)
                val temp = node.left
                node.left = node.right
                node.right = temp
            }

            return root
        }

        fun solution2(root: TreeNode?): TreeNode? {
            fun rec(root: TreeNode?): TreeNode? {
                if (root == null) return null
                val left = rec(root.left)
                val right = rec(root.right)
                root.left = right
                root.right = left
                return root
            }
            return rec(root)
        }

    }
}
