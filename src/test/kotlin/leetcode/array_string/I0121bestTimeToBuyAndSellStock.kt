package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0121 = (IntArray) -> Int

class I0121bestTimeToBuyAndSellStock {

    data class Case(
        val nums: List<Int>,
        val output: Int,
    )

    val prepareCase = { nums: String, o: Int ->
        Case(nums.toIntArray().toList(), o)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0121> {

        override val cases: List<Case> = listOf(
            prepareCase("[7,1,5,3,6,4]", 5),
            prepareCase("[7,6,4,3,1]", 0),
        )

        override val solutions: List<Pair<String, I0121>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
            ::solutionCommunity.name to ::solutionCommunity,
        )

        override fun Case.check(solution: I0121): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()

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
