package leetcode.binary_tree_general

import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested

typealias I0236 = (TreeNode?, TreeNode?, TreeNode?) -> TreeNode?

class I0236lowestCommonAncestorOfABinaryTree {

    @Nested
    inner class Solution {


        fun lowestCommonAncestor(root: TreeNode?, p: TreeNode?, q: TreeNode?): TreeNode? {
            if (root == null) return root

            var result: TreeNode? = null

            fun dfs(node: TreeNode = root): Pair<Boolean, Boolean> {
                if (result != null) return (false to false)
                val isP = node == p
                val isQ = node == q
                val (lP, lQ) = node.left?.let { dfs(it) } ?: (false to false)
                val (rP, rQ) = node.right?.let { dfs(it) } ?: (false to false)
                if (result != null) return (false to false)
                val pFound = isP || lP || rP
                val qFound = isQ || lQ || rQ
                if (pFound && qFound) result = node
                return (pFound to qFound)
            }
            dfs()

            return result
        }

        fun classicSolution(root: TreeNode?, p: TreeNode?, q: TreeNode?): TreeNode? {
            // Base case: null OR we found p or q → bubble it up
            if (root == null || root == p || root == q) return root

            val left = lowestCommonAncestor(root.left, p, q)
            val right = lowestCommonAncestor(root.right, p, q)

            return when {
                left != null && right != null -> root  // p and q split here → LCA!
                left != null -> left                  // both found on the left
                else -> right                 // both found on the right (or null)
            }
        }

    }
}
