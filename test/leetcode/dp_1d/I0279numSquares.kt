package leetcode.dp_1d

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 279. Perfect Squares  (https://leetcode.com/problems/perfect-squares/)
 *
 * Given an integer n, return the least number of perfect square numbers that sum to n.
 *
 * A perfect square is an integer that is the square of an integer; in other words, it is the product of some integer
 * with itself. For example, 1, 4, 9, and 16 are perfect squares while 3 and 11 are not.
 *
 * Constraints:
 * - 1 <= n <= 10^4
 */
typealias I0279 = (Int) -> Int

class I0279numSquares {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0279> {

        override val cases = leetcode.testCases<I0279>(
            12 expects 3,
            13 expects 2,
            17 expects 2,
            20 expects 2,
        )

        @Test
        fun test() = check(::numSquares)

        fun numSquares(n: Int): Int {

            val dp = IntArray(n + 1) { Int.MAX_VALUE }
            dp[0] = 0

            (1..n).forEach { dpIdx ->
                var idx = 1
                while (idx * idx <= dpIdx) {
                    dp[dpIdx] = minOf(dp[dpIdx], dp[dpIdx - (idx * idx)] + 1)
                    idx++
                }

            }

            return dp[n]
        }

    }
}
