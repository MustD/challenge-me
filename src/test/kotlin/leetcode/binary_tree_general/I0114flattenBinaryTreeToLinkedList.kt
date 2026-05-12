package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0114 = (TreeNode?) -> TreeNode?

class I0114flattenBinaryTreeToLinkedList {

    @Nested
    inner class Solution : ProblemTest<I0114> {

        override val cases = testCases<I0114>(
            "[1,2,5,3,4,null,6]" expects "[1,null,2,null,3,null,4,null,5,null,6]",
        )

        @Test
        fun test() = check(::flatten, ::flatten2)

        fun flatten(root: TreeNode?): TreeNode? {
            if (root == null) return null
            //preorder traversal -> root, left, right
            val list = mutableListOf<TreeNode?>()
            fun dfs(node: TreeNode?) {
                if (node == null) return
                list.add(node)
                dfs(node.left)
                dfs(node.right)
            }
            dfs(root)
            list.forEachIndexed { i, node ->
                node?.left = null
                if (i == list.lastIndex) return@forEachIndexed
                node?.right = list[i + 1]
            }

            return root
        }

        fun flatten2(root: TreeNode?): TreeNode? {
            var current = root
            while (current != null) {
                if (current.left != null) {
                    //Find rightmost node in left subtree
                    //this is the node that comes just before current.right
                    // in pre-order traversal, so we wire them together
                    var predecessor = current.left
                    while (predecessor?.right != null) {
                        predecessor = predecessor.right
                    }

                    //step1: attach current's right subtree after the left subtree
                    predecessor?.right = current.right

                    //step2: make the left subtree the mew right child
                    current.right = current.left

                    //step3: Clear the left pointer
                    current.left = null
                }
                //advance to the next node
                current = current.right

            }
            return root
        }

    }
}
