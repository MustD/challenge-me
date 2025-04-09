package leetcode.binary_tree_bfs

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import leetcode.utils.TreeNode
import leetcode.utils.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0199 = (TreeNode?) -> List<Int>

class I0199BinaryTreeRightSideView {

    data class Case(
        val input: TreeNode?,
        val output: List<Int>,
    )

    fun prepareCase(s: String, out: String) = Case(s.toTreeNode(), out.toIntArray().toList())


    @Nested
    inner class Solution : AproblemTest<Case, I0199> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,2,3,null,5,null,4]", "[1,3,4]"),
            prepareCase("[1,2,3,4,null,null,null,5]", "[1,3,4,5]"),
            prepareCase("[1,null,3]", "[1,3]"),
        )
        override val solutions: List<Pair<String, I0199>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0199): Pair<Boolean, Any> {
            val result = solution(input)
            return (result.toList() == output.toList()) to result.toList()
        }

        @Test
        fun test() = check()

        /**
         * time complexity is O(n)
         * space complexity is O(n)
         */
        fun solution1(root: TreeNode?): List<Int> {
            val queue = ArrayDeque<Pair<TreeNode, Int>>()
            val result = mutableListOf<Int>()

            if (root == null) return emptyList()
            queue.add(root to 0)
            while (queue.isNotEmpty()) {
                val (node, lvl) = queue.removeFirst()
                val left = node.left
                val right = node.right
                if (left != null) queue.add(left to lvl + 1)
                if (right != null) queue.add(right to lvl + 1)
                if (result.lastIndex < lvl) result.add(node.`val`) else result[lvl] = node.`val`
            }
            return result
        }

    }
}
