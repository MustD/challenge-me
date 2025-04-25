package leetcode.dp_multi

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0120 = (List<List<Int>>) -> Int

class I0120triangle {
    data class Case(
        val input: List<List<Int>>,
        val output: Int,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0120> {

        override val cases: List<Case> = listOf(
            Case(
                listOf(
                    listOf(2),
                    listOf(3, 4),
                    listOf(6, 5, 7),
                    listOf(4, 1, 8, 3),
                ),
                11
            ),
            Case(
                listOf(
                    listOf(-10),
                ),
                -10
            ),
            Case(
                listOf(
                    listOf(-1),
                    listOf(2, 3),
                    listOf(1, -1, -3)
                ),
                -1
            )
        )

        override val solutions = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0120): Pair<Boolean, Any> {
            val result = solution(input)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

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
