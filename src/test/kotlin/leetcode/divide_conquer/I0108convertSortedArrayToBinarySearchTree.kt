package leetcode.divide_conquer

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0108 = (IntArray) -> TreeNode?

class I0108convertSortedArrayToBinarySearchTree {

    @Nested
    inner class Solution : ProblemTest<I0108> {
        override val cases = testCases<I0108>(
            "[-10,-3,0,5,9]" expects "[0,-3,9,-10,null,5]",
            "[1,3]" expects "[3,1]",
        )

        @Test
        fun test() = check(::solution1)

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
