package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0122 = (IntArray) -> Int

class I0122bestTimeToBuyAndSellStockII {

    data class Case(
        val nums: List<Int>,
        val output: Int,
    )

    val prepareCase = { n1: String, r: Int ->
        Case(n1.toIntArray().toList(), r)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0122> {
        override val cases: List<Case> = listOf(
            prepareCase("[7,1,5,3,6,4]", 7),
            prepareCase("[1,2,3,4,5]", 4),
            prepareCase("[7,6,4,3,1]", 0),
        )
        override val solutions: List<Pair<String, I0122>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0122): Pair<Boolean, Any> {
            val array = nums.toIntArray()
            val result = solution(array)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(prices: IntArray): Int {
            var profit = 0
            for (i in 1..prices.lastIndex) {
                if (prices[i] > prices[i - 1]) profit += prices[i] - prices[i - 1]
            }
            return profit
        }

    }
}
