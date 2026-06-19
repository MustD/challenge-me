package leetcode.binary_tree_bfs

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0199 = (TreeNode?) -> List<Int>

class I0199BinaryTreeRightSideView {

    @Nested
    inner class Solution : ProblemTest<I0199> {
        override val cases = testCases<I0199>(
            "[1,2,3,null,5,null,4]" expects "[1,3,4]",
            "[1,2,3,4,null,null,null,5]" expects "[1,3,4,5]",
            "[1,null,3]" expects "[1,3]",
        )

        @Test
        fun test() = check(::solution1)

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
