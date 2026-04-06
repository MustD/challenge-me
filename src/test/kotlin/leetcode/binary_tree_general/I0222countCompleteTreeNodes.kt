package leetcode.binary_tree_general

import leetcode.AproblemTest
import leetcode.utils.TreeNode
import leetcode.utils.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0222countCompleteTreeNodes {

    data class Case(
        val input: TreeNode?,
        val output: Int,
    )

    fun prepareCase(s: String, out: Int) = Case(s.toTreeNode(), out)


    @Nested
    inner class Solution : AproblemTest<Case, (TreeNode?) -> Int> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,2,3,4,5,6]", 6),
            prepareCase("[]", 0),
            prepareCase("[1]", 1),
            prepareCase("[1,2,3]", 3),
            prepareCase("[1,2,3,4]", 4)
        )
        override val solutions: List<Pair<String, (TreeNode?) -> Int>> = listOf(
            ::countNodes.name to ::countNodes,
        )

        override fun Case.check(solution: (TreeNode?) -> Int): Pair<Boolean, Any> {
            val result = solution(input)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun countNodes(root: TreeNode?): Int {
            return if (root == null) 0 else 1 + countNodes(root.left) + countNodes(root.right)
        }

    }
}
