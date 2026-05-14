package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0094 = (TreeNode?) -> List<Int>

class I0094binaryTreeInorderTraversal {

    @Nested
    inner class Solution : ProblemTest<I0094> {

        override val cases = testCases<I0094>(
            "[1,null,2,3]" expects listOf(1, 3, 2),
        )

        @Test
        fun test() = check(::inorderTraversalReq, ::inorderTraversal)

        fun inorderTraversalReq(root: TreeNode?): List<Int> {
            if (root == null) return emptyList()

            fun dfs(node: TreeNode = root): List<Int> {
                val result = mutableListOf<Int>()
                node.left?.let { dfs(it) }?.let { result.addAll(it) }
                node.`val`.let { result.add(it) }
                node.right?.let { dfs(it) }?.let { result.addAll(it) }
                return result
            }

            return dfs()
        }

        fun inorderTraversal(root: TreeNode?): List<Int> {
            if (root == null) return emptyList()

            val deque = ArrayDeque<TreeNode>()
            val result = mutableListOf<Int>()
            var current = root

            while (current != null || deque.isNotEmpty()) {
                //Go leftmost
                while (current != null) {
                    deque.addLast(current)
                    current = current.left
                }

                current = deque.removeLast()
                result.add(current.`val`)
                current = current.right

            }
            return result
        }

    }
}
