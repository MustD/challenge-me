package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0129 = (TreeNode?) -> Int

class I0129sumRootToLeafNumbers {

    @Nested
    inner class Solution : ProblemTest<I0129> {

        override val cases = testCases<I0129>(
            "[1,2,3]" expects 25,
        )

        @Test
        fun test() = check(::sumNumbers)

        fun sumNumbers(root: TreeNode?): Int {
            if (root == null) return 0
            var sum = 0
            fun dfs(node: TreeNode = root, current: Int = 0) {
                val isLeaf = node.left == null && node.right == null
                if (isLeaf) {
                    sum += (current * 10 + node.`val`)
                    return
                }
                node.left?.let { left -> dfs(left, current * 10 + node.`val`) }
                node.right?.let { right -> dfs(right, current * 10 + node.`val`) }
            }
            return dfs().let { sum }
        }

    }
}
