package leetcode.binary_tree_general

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import leetcode.utils.TreeNode
import leetcode.utils.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0105 = (IntArray, IntArray) -> TreeNode?

class I0105constructBinaryTreeFromPreorderAndInorderTraversal {

    data class Case(
        val preorder: List<Int>,
        val inorder: List<Int>,
        val output: TreeNode?,
    )

    fun prepareCase(p: String, q: String, r: String) = Case(
        p.toIntArray().toList(),
        q.toIntArray().toList(),
        r.toTreeNode()
    )


    @Nested
    inner class Solution : AproblemTest<Case, I0105> {
        override val cases: List<Case> = listOf(
            prepareCase("[3,9,20,15,7]", "[9,3,15,20,7]", "[3,9,20,null,null,15,7]"),
        )
        override val solutions: List<Pair<String, I0105>> = listOf(
            ::solutionCommunity.name to ::solutionCommunity,
            ::solutionCommunity2.name to ::solutionCommunity2,
        )

        override fun Case.check(solution: I0105): Pair<Boolean, Any> {
            val result = solution(preorder.toIntArray(), inorder.toIntArray())
            return (result.toString() == output.toString()) to result.toString()
        }

        @Test
        fun test() = check()

        /**
         * Time Complexity: O(n²) in worst case due to the linear search in the inorder array
         * Space Complexity: O(h) where h is the height of the tree, due to recursion stack
         */
        fun solutionCommunity(preorder: IntArray, inorder: IntArray): TreeNode? {
            fun buildTree(
                preorder: IntArray, inorder: IntArray,
                preLeft: Int = 0, preRight: Int = preorder.lastIndex,
                inLeft: Int = 0, inRight: Int = inorder.lastIndex,
            ): TreeNode? {
                if (inRight < inLeft || preRight < preLeft) return null

                val node = TreeNode(preorder[preLeft])

                var nodeIndexInInOrder = inLeft
                while (node.`val` != inorder[nodeIndexInInOrder]) {
                    nodeIndexInInOrder++
                }

                val sizeOfLeftInOrder = nodeIndexInInOrder - inLeft
                node.left = buildTree(
                    preorder, inorder,
                    preLeft + 1, preLeft + sizeOfLeftInOrder,
                    inLeft, nodeIndexInInOrder - 1
                )
                node.right = buildTree(
                    preorder, inorder,
                    preLeft + sizeOfLeftInOrder + 1, preRight,
                    nodeIndexInInOrder + 1, inRight
                )

                return node
            }
            return buildTree(preorder, inorder)
        }

        /**
         * Time Complexity: O(n) where n is the number of nodes in the tree. Each node is processed exactly once.
         * Space Complexity: O(n) hashmap + recursion stack
         */
        fun solutionCommunity2(preorder: IntArray, inorder: IntArray): TreeNode? {
            val inorderMap = HashMap<Int, Int>()
            inorder.forEachIndexed { index, value -> inorderMap[value] = index }
            var preorderIndex = 0

            fun buildTree(preorder: IntArray, left: Int, right: Int): TreeNode? {
                if (left > right) return null

                val rootVal = preorder[preorderIndex++]
                val root = TreeNode(rootVal)

                val inorderIndex = inorderMap[rootVal]!!

                root.left = buildTree(preorder, left, inorderIndex - 1)
                root.right = buildTree(preorder, inorderIndex + 1, right)

                return root
            }
            return buildTree(preorder, 0, inorder.size - 1)
        }

    }
}
