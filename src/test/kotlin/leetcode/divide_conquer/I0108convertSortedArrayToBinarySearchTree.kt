package leetcode.divide_conquer

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import leetcode.utils.TreeNode
import leetcode.utils.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0108convertSortedArrayToBinarySearchTree {

    data class Case(
        val input: List<Int>,
        val output: TreeNode?,
    )

    fun prepareCase(s: String, out: String) = Case(
        s.toIntArray().toList(),
        out.toTreeNode()
    )


    @Nested
    inner class Solution : AproblemTest<Case, (IntArray) -> TreeNode?> {
        override val cases: List<Case> = listOf(
            prepareCase("[-10,-3,0,5,9]", "[0,-3,9,-10,null,5]"),
            prepareCase("[1,3]", "[3,1]")
        )
        override val solutions: List<Pair<String, (IntArray) -> TreeNode?>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: (IntArray) -> TreeNode?): Pair<Boolean, Any> {
            val result = solution(input.toIntArray()) ?: ""
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(nums: IntArray): TreeNode? {
            fun dfs(nums: IntArray): TreeNode? {
                val half = nums.size / 2
                return TreeNode(nums[half]).apply {
                    left = if (half > 0) dfs(nums.copyOfRange(0, half)) else null
                    right = if (half < nums.size - 1) dfs(nums.copyOfRange(half + 1, nums.size)) else null
                }
            }
            return dfs(nums)
        }

    }
}
