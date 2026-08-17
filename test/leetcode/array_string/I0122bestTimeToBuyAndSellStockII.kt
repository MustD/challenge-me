package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0122 = (IntArray) -> Int

/**
 * LeetCode 122 — Best Time to Buy and Sell Stock II
 *
 * You are given an integer array prices where prices(i) is the price of a given stock on the ith day.
 *
 * On each day, you may decide to buy and/or sell the stock. You can only hold at most one share of the
 * stock at any time. However, you can sell and buy the stock multiple times on the same day, ensuring
 * you never hold more than one share of the stock.
 *
 * Find and return the maximum profit you can achieve.
 *
 * Example 1:
 *   Input: prices = [7,1,5,3,6,4]
 *   Output: 7
 *   Explanation: Buy on day 2 (price = 1) and sell on day 3 (price = 5), profit = 5-1 = 4.
 *   Then buy on day 4 (price = 3) and sell on day 5 (price = 6), profit = 6-3 = 3.
 *   Total profit is 4 + 3 = 7.
 *
 * Example 2:
 *   Input: prices = [1,2,3,4,5]
 *   Output: 4
 *   Explanation: Buy on day 1 (price = 1) and sell on day 5 (price = 5), profit = 5-1 = 4.
 *
 * Example 3:
 *   Input: prices = [7,6,4,3,1]
 *   Output: 0
 *   Explanation: There is no way to make a positive profit, so we never buy.
 *
 * ─────────────────────────────────────────────────────────────────────────────────────────────
 * EDUCATIONAL NOTES
 * ─────────────────────────────────────────────────────────────────────────────────────────────
 *
 * The unlimited-transactions rule is what makes this problem far easier than its "Stock I" sibling
 * (where you may buy/sell only once).
 *
 * KEY INSIGHT — "capture every uphill step"
 * Any profitable trade from a valley v to a peak p can be decomposed into the sum of the daily gains
 * along the way:
 *
 *     p - v = (d1 - v) + (d2 - d1) + ... + (p - dn)
 *
 * Each term on the right is just the difference between two consecutive days. So instead of hunting
 * for valley/peak pairs, we can greedily collect EVERY positive day-over-day difference. Negative
 * differences (downhill) are simply skipped — we just don't hold the stock on those days.
 *
 * Visualizing [7,1,5,3,6,4]:
 *
 *     diffs:  1->5 = +4   (keep)
 *             5->3 = -2   (skip)
 *             3->6 = +3   (keep)
 *             6->4 = -2   (skip)
 *     total = 4 + 3 = 7   ✓
 *
 * This greedy choice is optimal: there's never a reason to sit out a positive step (it only adds
 * profit) and never a reason to ride a negative step (it only subtracts). Because the share is
 * costless to re-buy and we can transact same-day, the valley/peak walk and the "sum of positive
 * diffs" produce identical totals.
 *
 * COMPLEXITY
 *   Time:  O(n) — one pass over prices.
 *   Space: O(1) — a single accumulator.
 *
 * COMMON PITFALLS
 *   • Forgetting to return the accumulator (the original bug here returned a literal 0).
 *   • Off-by-one when comparing day i with day i+1; iterate i in 1 until size and compare
 *     against the previous day (i minus 1) to stay in bounds.
 *   • Over-engineering with explicit "holding a position" state — unnecessary for this variant.
 */
class I0122bestTimeToBuyAndSellStockII {
    @Nested
    inner class Solution : ProblemTest<I0122> {

        override val cases = testCases<I0122>(
            "[7,1,5,3,6,4]" expects 7,
            "[1,2,3,4,5]" expects 4,
            "[7,6,4,3,1]" expects 0,
            "[1]" expects 0,
            "[]" expects 0,
            "[2,2,2,2]" expects 0,
            "[1,5,2,8]" expects 10,
        )

        @Test
        fun test() = check(::solution1, ::solution2)

        fun solution1(prices: IntArray): Int {
            var profit = 0
            for (i in 1..prices.lastIndex) {
                if (prices[i] > prices[i - 1]) profit += prices[i] - prices[i - 1]
            }
            return profit
        }

        /**
         * Greedy one-pass: accumulate every positive consecutive difference.
         * 2026-06-10
         */
        fun solution2(prices: IntArray): Int {
            var profit = 0
            for (i in prices.indices) {
                val next = prices.getOrNull(i + 1) ?: return profit

                if (prices[i] < prices[i + 1]) profit += next - prices[i]
            }

            return profit
        }


    }
}
