package leetcode.binary_tree_bfs

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toListOfIntLists
import leetcode.utils.TreeNode
import leetcode.utils.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0102 = (TreeNode?) -> List<List<Int>>

class I0102BinaryTreeLevelOrderTraversal {

    data class Case(
        val input: TreeNode?,
        val output: List<List<Int>>,
    )

    fun prepareCase(s: String, out: String) = Case(s.toTreeNode(), out.toListOfIntLists())


    @Nested
    inner class Solution : AproblemTest<Case, I0102> {
        override val cases: List<Case> = listOf(
            prepareCase("[3,9,20,null,null,15,7]", "[[3],[9,20],[15,7]]"),
            prepareCase("[1]", "[[1]]"),
            prepareCase("[]", "[]"),
        )
        override val solutions: List<Pair<String, I0102>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0102): Pair<Boolean, Any> {
            val result = solution(input)
            return (result.toList() == output.toList()) to result.toList()
        }

        @Test
        fun test() = check()

        fun solution1(root: TreeNode?): List<List<Int>> {
            val queue = ArrayDeque<TreeNode>()
            val result = mutableListOf<List<Int>>()
            queue.add(root ?: return result)

            while (queue.isNotEmpty()) {
                val levelSize = queue.size
                val levelValues = mutableListOf<Int>()

                for (i in 0 until levelSize) {
                    val node = queue.removeFirst()
                    levelValues.add(node.`val`)
                    node.left?.let(queue::add)
                    node.right?.let(queue::add)
                }

                result.add(levelValues)
            }
            return result
        }

    }
}
