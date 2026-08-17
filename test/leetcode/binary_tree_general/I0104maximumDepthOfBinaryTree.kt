package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0104 = (TreeNode?) -> Int

class I0104maximumDepthOfBinaryTree {

    @Nested
    inner class Solution : ProblemTest<I0104> {
        override val cases = testCases<I0104>(
            "[3,9,20,null,null,15,7]" expects 3,
            "[1,null,2]" expects 2,
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(root: TreeNode?): Int {
            fun dfs(node: TreeNode?, lvl: Int): Int {
                if (node == null) return lvl
                return maxOf(
                    a = dfs(node.left, lvl + 1),
                    b = dfs(node.right, lvl + 1)
                )
            }
            return dfs(root, 0)
        }
    }
}
