package leetcode.binary_tree_general

import leetcode.utils.TreeNode

class I0173binarySearchTreeIterator {

    class BSTIterator(root: TreeNode?) {
        private val stack = ArrayDeque<TreeNode>()

        init {
            var current = root
            //Go leftmost
            while (current != null) {
                stack.addLast(current)
                current = current.left
            }
        }

        fun next(): Int {
            val current = stack.removeLast()
            val value = current.`val`

            var right = current.right
            while (right != null) {
                stack.addLast(right)
                right = right.left
            }

            return value
        }

        fun hasNext(): Boolean = stack.isNotEmpty()

    }

}
