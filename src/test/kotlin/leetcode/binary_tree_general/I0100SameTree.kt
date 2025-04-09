package leetcode.binary_tree_general

import leetcode.AproblemTest
import leetcode.utils.TreeNode
import leetcode.utils.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0100 = (TreeNode?, TreeNode?) -> Boolean

class I0100SameTree {

    data class Case(
        val input: TreeNode?,
        val same: TreeNode?,
        val output: Boolean,
    )

    fun prepareCase(p: String, q: String, r: Boolean) = Case(
        p.toTreeNode(),
        q.toTreeNode(),
        r
    )


    @Nested
    inner class Solution : AproblemTest<Case, I0100> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,2,3]", "[1,2,3]", true),
            prepareCase("[1,2]", "[1,null,2]", false),
            prepareCase("[1,2,1]", "[1,1,2]", false),
        )
        override val solutions: List<Pair<String, I0100>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0100): Pair<Boolean, Any> {
            val result = solution(input, same)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(p: TreeNode?, q: TreeNode?): Boolean {
            if (p == null && q == null) return true
            val queue = ArrayDeque<Pair<TreeNode?, TreeNode?>>()
            queue.add(p to q)
            while (queue.isNotEmpty()) {
                val (first, second) = queue.removeFirst()
                if (first == null && second == null) continue

                if (first == null || second == null) return false
                if (first.`val` != second.`val`) return false

                queue.add(first.left to second.left)
                queue.add(first.right to second.right)
            }
            return true
        }


        fun solution2(p: TreeNode?, q: TreeNode?): Boolean {
            fun dfs(p: TreeNode?, q: TreeNode?): Boolean = when {
                p == null && q == null -> true
                p?.`val` != q?.`val` -> false
                else -> dfs(p?.left, q?.left) && dfs(p?.right, q?.right)
            }
            return dfs(p, q)
        }

    }
}
