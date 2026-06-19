package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0100 = (TreeNode?, TreeNode?) -> Boolean

class I0100SameTree {

    @Nested
    inner class Solution : ProblemTest<I0100> {
        override val cases = testCases<I0100>(
            args("[1,2,3]", "[1,2,3]") expects true,
            args("[1,2]", "[1,null,2]") expects false,
            args("[1,2,1]", "[1,1,2]") expects false,
        )

        @Test
        fun test() = check(::solution1)

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
