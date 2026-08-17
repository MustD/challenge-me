package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0106 = (IntArray, IntArray) -> TreeNode?

class I0106constructBinaryTreeFromInorderAndPostorderTraversal {

    @Nested
    inner class Solution : ProblemTest<I0106> {

        override val cases = testCases<I0106>(
            args("[9,3,15,20,7]", "[9,15,7,20,3]") expects "[3,9,20,null,null,15,7]",
        )

        @Test
        fun test() = check(::buildTree)

        fun buildTree(inorder: IntArray, postorder: IntArray): TreeNode? {
            val inOrderIndex = HashMap<Int, Int>().apply {
                inorder.forEachIndexed { i, v -> this[v] = i }
            }

            var postIdx = postorder.lastIndex

            //taking range of tree in inorder array
            fun build(inLeft: Int, inRight: Int): TreeNode? {
                //no subtree - exit with empty tree
                if (inLeft > inRight) return null

                val rootVal = postorder[postIdx--]
                val root = TreeNode(rootVal)
                val mid =
                    inOrderIndex[rootVal] ?: throw IllegalStateException("Root value $rootVal not found in inorder")

                root.right = build(mid + 1, inRight)
                root.left = build(inLeft, mid - 1)
                return root
            }

            return build(0, inorder.lastIndex)
        }

    }
}
