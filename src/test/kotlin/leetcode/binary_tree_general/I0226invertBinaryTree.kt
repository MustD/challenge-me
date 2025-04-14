package leetcode.binary_tree_general

import leetcode.AproblemTest
import leetcode.utils.TreeNode
import leetcode.utils.deepCopy
import leetcode.utils.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0226 = (TreeNode?) -> TreeNode?

class I0226invertBinaryTree {

    data class Case(
        val input: TreeNode?,
        val output: TreeNode?,
    )

    fun prepareCase(i: String, o: String) = Case(
        i.toTreeNode(),
        o.toTreeNode(),
    )


    @Nested
    inner class Solution : AproblemTest<Case, I0226> {
        override val cases: List<Case> = listOf(
            prepareCase("[4,2,7,1,3,6,9]", "[4,7,2,9,6,3,1]"),
            prepareCase("[2,1,3]", "[2,3,1]"),
            prepareCase("[]", "[]"),
        )
        override val solutions: List<Pair<String, I0226>> = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
        )

        override fun Case.check(solution: I0226): Pair<Boolean, Any> {
            val result = solution(input?.deepCopy())
            return (result.toString() == output.toString()) to result.toString()
        }

        @Test
        fun test() = check()

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
