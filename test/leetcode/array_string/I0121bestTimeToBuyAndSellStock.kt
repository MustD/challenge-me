package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0121 = (IntArray) -> Int

class I0121bestTimeToBuyAndSellStock {

    @Nested
    inner class Solution : ProblemTest<I0121> {

        override val cases = testCases<I0121>(
            "[7,1,5,3,6,4]" expects 5,
            "[7,6,4,3,1]" expects 0,
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial, ::solutionCommunity)

        /**
         * O(n²) time complexity
         * space complexity is O(1)
         * leetcode time limit
         */
        fun solution1(prices: IntArray): Int {
            var max = 0
            for (buy in 0..prices.lastIndex) {
                for (sell in buy + 1..prices.lastIndex) {
                    max = maxOf(max, prices[sell] - prices[buy])
                }
            }
            return max
        }

        /**
         * O(n) time complexity
         * space complexity is O(1)
         */
        fun solutionEditorial(prices: IntArray): Int {
            var minPrice = Int.MAX_VALUE
            var maxProfit = 0
            for (i in 0..prices.lastIndex) {
                if (prices[i] < minPrice) {
                    minPrice = prices[i]
                } else if (prices[i] - minPrice > maxProfit) {
                    maxProfit = prices[i] - minPrice
                }
            }
            return maxProfit
        }

        fun solutionCommunity(prices: IntArray): Int {
            prices.foldIndexed(prices[0]) { i, min, n ->
                prices[i] = n - min
                if (n < min) n else min
            }
            return prices.max()
        }


    }
}
