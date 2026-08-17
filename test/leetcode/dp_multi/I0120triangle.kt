package leetcode.dp_multi

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0120 = (List<List<Int>>) -> Int

class I0120triangle {

    @Nested
    inner class Solution : ProblemTest<I0120> {

        override val cases = testCases<I0120>(
            "[[2],[3,4],[6,5,7],[4,1,8,3]]" expects 11,
            "[[-10]]" expects -10,
            "[[-1],[2,3],[1,-1,-3]]" expects -1,
        )

        @Test
        fun test() = check(::solution1)

        private fun solutionOnlyPositive(triangle: List<List<Int>>): Int {
            val dp = MutableList(triangle.size) { Int.MAX_VALUE }
            dp[0] = triangle[0][0]
            for (i in 1..triangle.lastIndex) {
                val prevRowMin = dp[i - 1]
                for (j in 0..triangle[i].lastIndex) {
                    val left = if (j - 1 >= 0) prevRowMin + triangle[i][j] else Int.MAX_VALUE
                    val right = if (j <= triangle[i].lastIndex) prevRowMin + triangle[i][j] else Int.MAX_VALUE
                    dp[i] = minOf(left, right, dp[i])
                }
            }
            return dp.last()
        }

        private fun solution1(triangle: List<List<Int>>): Int {
            val dp = MutableList(triangle.size) { i -> MutableList(triangle[i].size) { j -> triangle[i][j] } }
            for (i in 1..triangle.lastIndex) {
                for (j in 0..triangle[i].lastIndex) {
                    val left = if (j - 1 >= 0) dp[i - 1][j - 1] + triangle[i][j] else Int.MAX_VALUE
                    val right = if (j <= triangle[i - 1].lastIndex) dp[i - 1][j] + triangle[i][j] else Int.MAX_VALUE
                    dp[i][j] = minOf(left, right)
                }
            }
            return dp.last().min()
        }
    }
}
