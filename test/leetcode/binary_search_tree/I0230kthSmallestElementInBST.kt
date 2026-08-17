package leetcode.binary_search_tree

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0230 = (TreeNode?, Int) -> Int

class I0230kthSmallestElementInBST {

    @Nested
    inner class Solution : ProblemTest<I0230> {

        override val cases = testCases<I0230>(
            args("[3,1,4,null,2]", 1) expects 1,
            args("[5,3,6,2,4,null,null,1]", 3) expects 3,

            )

        @Test
        fun test() = check(::kthSmallest)

        fun kthSmallest(root: TreeNode?, k: Int): Int {
            if (root == null) return Int.MAX_VALUE
            var result = root.`val`
            var counter = 0

            fun inOrder(node: TreeNode = root) {
                if (counter == k) return
                node.left?.let { inOrder(it) }
                if (counter == k) return
                node.`val`.let { counter++; result = it }
                node.right?.let { inOrder(it) }
            }
            inOrder()

            return result
        }

    }
}
