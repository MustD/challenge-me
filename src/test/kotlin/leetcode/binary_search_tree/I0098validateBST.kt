package leetcode.binary_search_tree

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0098 = (TreeNode?) -> Boolean

class I0098validateBST {

    @Nested
    inner class Solution : ProblemTest<I0098> {

        override val cases = testCases<I0098>(
            "[2,1,3]" expects true,
            "[5,1,4,null,null,3,6]" expects false,
            "[5,4,6,null,null,3,7]" expects false,
        )

        @Test
        fun test() = check(::isValidBSTReq, ::isValidBSTQueue)

        fun isValidBSTReq(root: TreeNode?): Boolean {
            fun validate(node: TreeNode?, min: Long, max: Long): Boolean {
                if (node == null) return true
                if (node.`val` !in (min + 1)..<max) return false

                return validate(node.left, min, node.`val`.toLong()) &&
                        validate(node.right, node.`val`.toLong(), max)
            }
            return validate(root, Long.MIN_VALUE, Long.MAX_VALUE)
        }

        fun isValidBSTQueue(root: TreeNode?): Boolean {

            val stack = ArrayDeque<TreeNode>()
            var prev = Long.MIN_VALUE
            var current = root

            while (current != null || stack.isNotEmpty()) {
                while (current != null) {
                    stack.addLast(current)
                    current = current.left
                }
                current = stack.removeLast()
                if (current.`val` <= prev) return false
                prev = current.`val`.toLong()
                current = current.right
            }
            return true
        }

    }
}
