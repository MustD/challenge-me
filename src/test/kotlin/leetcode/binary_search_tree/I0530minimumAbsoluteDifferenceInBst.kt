package leetcode.binary_search_tree

import leetcode.AproblemTest
import leetcode.utils.TreeNode
import leetcode.utils.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.math.abs
import kotlin.test.Test

class I0530minimumAbsoluteDifferenceInBst {

    data class Case(
        val input: TreeNode?,
        val output: Int,
    )

    fun prepareCase(s: String, out: Int) = Case(s.toTreeNode(), out)


    @Nested
    inner class Solution : AproblemTest<Case, (TreeNode?) -> Int> {
        override val cases: List<Case> = listOf(
            prepareCase("[4,2,6,1,3]", 1),
            prepareCase("[1,0,48,null,null,12,49]", 1),
            prepareCase("[1,null,5,null,null,3]", 2),
            prepareCase("[236,104,701,null,227,null,911]", 9)
        )
        override val solutions: List<Pair<String, (TreeNode?) -> Int>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionCommunity.name to ::solutionCommunity,
        )

        override fun Case.check(solution: (TreeNode?) -> Int): Pair<Boolean, Any> {
            val result = solution(input)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(root: TreeNode?): Int {
            fun dfs(node: TreeNode?, path: List<Int>): Int {
                if (node == null) return Int.MAX_VALUE
                return minOf(
                    a = dfs(node.left, path.plus(node.`val`)),
                    b = dfs(node.right, path.plus(node.`val`)),
                    c = if (path.isNotEmpty()) path.minOf { abs(node.`val` - it) } else Int.MAX_VALUE,
                )
            }
            return dfs(root, emptyList())
        }

        fun solutionCommunity(root: TreeNode?): Int {
            var prev: TreeNode? = null
            var diff = Int.MAX_VALUE

            fun dfs(node: TreeNode?) {
                if (node == null) return
                dfs(node.left)
                if (prev != null) {
                    diff = minOf(abs(prev!!.`val` - node.`val`), diff)
                }
                prev = node
                dfs(node.right)
            }

            dfs(root)

            return diff
        }
    }
}
