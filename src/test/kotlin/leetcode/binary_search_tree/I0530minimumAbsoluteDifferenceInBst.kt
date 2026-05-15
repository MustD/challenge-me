package leetcode.binary_search_tree

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.math.abs
import kotlin.test.Test

typealias I0530 = (TreeNode?) -> Int

class I0530minimumAbsoluteDifferenceInBst {

    @Nested
    inner class Solution : ProblemTest<I0530> {

        override val cases = testCases<I0530>(
            "[4,2,6,1,3]" expects 1,
            "[1,0,48,null,null,12,49]" expects 1,
            "[236,104,701,null,227,null,911]" expects 9
        )

        @Test
        fun test() = check(
            ::solutionNotOptimal,
            ::getMinimumDifference,
            ::getMinimumDifferenceCommunity
        )

        fun solutionNotOptimal(root: TreeNode?): Int {
            if (root == null) return Int.MAX_VALUE

            fun dfs(node: TreeNode = root, path: List<Int> = emptyList()): Int {
                return minOf(
                    a = node.left?.let { dfs(it, path.plus(node.`val`)) } ?: Int.MAX_VALUE,
                    b = node.right?.let { dfs(it, path.plus(node.`val`)) } ?: Int.MAX_VALUE,
                    c = if (path.isNotEmpty()) path.minOf { abs(node.`val` - it) } else Int.MAX_VALUE,
                )
            }
            return dfs()
        }

        fun getMinimumDifference(root: TreeNode?): Int {
            var min = Int.MAX_VALUE
            if (root == null) return min

            var prev: TreeNode? = null
            fun dfs(node: TreeNode = root) {
                node.left?.let { dfs(it) }

                prev?.let { min = minOf(abs(it.`val` - node.`val`), min) }
                prev = node

                node.right?.let { dfs(it) }
            }

            dfs()

            return min
        }

        fun getMinimumDifferenceCommunity(root: TreeNode?): Int {
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
