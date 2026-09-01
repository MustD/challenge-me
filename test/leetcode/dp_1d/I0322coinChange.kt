package leetcode.dp_1d

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 322. Coin Change  (https://leetcode.com/problems/coin-change/)
 *
 * You are given an integer array `coins` representing coins of different denominations, and an integer
 * `amount` representing a total amount of money. Return the fewest number of coins needed to make up
 * that amount. If the amount cannot be made up by any combination of the coins, return -1.
 * You may assume you have an infinite number of each kind of coin.
 *
 * Constraints:
 * - 1 <= coins.length <= 12
 * - 1 <= coins[i] <= 2^31 - 1  (a single coin can exceed `amount`, and coin sums can overflow Int)
 * - 0 <= amount <= 10^4        (amount == 0 is valid input; the answer there is 0)
 */
typealias I0322 = (IntArray, Int) -> Int

class I0322coinChange {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0322> {

        override val cases = leetcode.testCases<I0322>(
            leetcode.args("[1,2,5]", 11) expects 3,        // 11 = 5 + 5 + 1
            leetcode.args("[2]", 3) expects -1,            // odd amount, only even coin
            leetcode.args("[1]", 0) expects 0,             // zero amount needs zero coins
            leetcode.args("[2147483647]", 2) expects -1,   // coin far larger than amount
            leetcode.args("[1,2,5]", 100) expects 20,
            leetcode.args("[186,419,83,408]", 6249) expects 20,  // greedy-by-largest fails here
        )

        // NOTE: `coinChange` (the in-progress attempt) is intentionally left OUT of `check(...)`
        // for now — it is correct in spirit but exponential, and case `[186,419,83,408] / 6249`
        // would effectively hang the suite. Add `::coinChange` back once it is optimized.
        @Test
        fun test() = check(::coinChange, ::referenceSolution, ::referenceSolution2)

        fun coinChange(coins: IntArray, amount: Int): Int {

            val inf = amount + 1
            val dp = MutableList(amount + 1) { inf }
            dp[0] = 0

            (1..amount).forEach { am ->
                coins.forEach { coin ->
                    if (
                        coin in 1..am //current coin may fit in current amount
                        && dp[am - coin] + 1 < dp[am]
                    ) dp[am] = dp[am - coin] + 1
                }
            }

            return if (dp[amount] > amount) -1 else dp[amount]

        }


        /**
         * ## Restatement
         *
         * Unlimited supply of each denomination (an *unbounded* knapsack). Find the **minimum number of
         * coins** summing exactly to `amount`, or `-1` when no combination hits it exactly.
         *
         * ## Why greedy fails, and why your search is too slow
         *
         * Greedy "take the biggest coin that fits" is wrong. On `[186,419,83,408], 6249` greedy grabs
         * 419s first and strands a remainder it cannot finish cheaply; the true optimum is 20 coins and
         * does not lead with the largest denomination. That case sits in `cases` precisely to kill greedy.
         *
         * Your attempt is **iterative deepening + backtracking**: try "can I hit `amount` with exactly
         * k coins?" for k = minLimit, minLimit+1, ... and return the first k that works. That is
         * *correct* (the first k that succeeds is minimal) but the search tree is `n^k` — with n = 4
         * denominations and k = 20 that is ~10^12 nodes. Two things are missing that make it explode:
         *  - **no pruning**: `backtrack` keeps adding coins even after `result > amount`;
         *  - **no memoization**: the same (remaining amount, coins left) pair is re-explored endlessly,
         *    and it also re-explores permutations of the same multiset (1+2 and 2+1 are separate paths).
         *
         * Once you add memo on "remaining amount", the coin *count* stops needing to be an outer loop at
         * all — that is exactly the DP below.
         *
         * ## Pattern: unbounded knapsack / 1-D bottom-up DP
         *
         * Define `dp[a]` = fewest coins to make exactly `a`. The recurrence is the whole insight:
         *
         *     dp[0] = 0
         *     dp[a] = 1 + min over coins c <= a of dp[a - c]        (INF if none reachable)
         *
         * "To make `a`, some coin `c` must be the last one placed; the rest is the optimal way to make
         * `a - c`." Because `dp[a - c]` is already final when we compute `dp[a]` (we sweep `a` upward),
         * one pass suffices — no recursion, no permutation blow-up.
         *
         * Note the coin loop is *inside* the amount loop and `a` runs **forward** — that forward sweep is
         * what makes each coin reusable infinitely (contrast 0/1 knapsack, which sweeps backward).
         *
         * ## Complexity
         *
         * Time `O(amount * n)` — one inner scan of the `n` denominations per amount; here at most
         * 10^4 * 12 = 120k steps. Space `O(amount)` for the table.
         *
         * ## Pitfalls
         *
         * - **Sentinel overflow**: initializing the table with `Int.MAX_VALUE` and then writing
         *   `dp[a - c] + 1` overflows to a negative number and silently wins the `min`. Use
         *   `amount + 1` as "infinity" (no valid answer can exceed `amount`, since the smallest coin is
         *   >= 1), or guard with `if (dp[a - c] != INF)`.
         * - **Coin larger than amount**: `coins[i]` can be up to 2^31-1, so `a - c` must be guarded by
         *   `c <= a` — otherwise negative index. (Your `filter { it <= amount }` handles this the same way.)
         * - **`amount == 0` returns 0**, not -1 — it falls out of `dp[0] = 0` for free.
         * - Return `-1` when `dp[amount]` is still the sentinel.
         */
        fun referenceSolution(coins: IntArray, amount: Int): Int {
            val inf = amount + 1                    // unreachable sentinel: answer can never exceed `amount`
            val dp = IntArray(amount + 1) { inf }
            dp[0] = 0

            for (a in 1..amount) {
                for (coin in coins) {
                    if (coin in 1..a && dp[a - coin] + 1 < dp[a]) {
                        dp[a] = dp[a - coin] + 1
                    }
                }
            }

            return if (dp[amount] > amount) -1 else dp[amount]
        }

        /**
         * Same recurrence expressed **top-down** (memoized recursion) — closer in shape to the
         * backtracking attempt above, and useful to see that the only real change is the `memo` array
         * plus dropping the explicit "coin count" dimension.
         *
         * `memo[a] = 0` means "not computed yet"; `-1` means "proved impossible". Complexity is identical:
         * each of the `amount + 1` states is solved once, each costing an `O(n)` scan.
         */
        fun referenceSolution2(coins: IntArray, amount: Int): Int {
            val memo = IntArray(amount + 1) { 0 }         // 0 = unknown, -1 = impossible, k > 0 = answer

            fun solve(rest: Int): Int {
                if (rest == 0) return 0
                if (memo[rest] != 0) return memo[rest]

                var best = -1
                for (coin in coins) {
                    if (coin !in 1..rest) continue

                    val sub = solve(rest - coin)
                    val found = sub >= 0

                    val notYetFound = best == -1
                    val newBest = sub + 1 < best
                    val newBestFound = notYetFound || newBest

                    if (found && newBestFound) best = sub + 1
                }

                memo[rest] = best
                return best
            }

            return solve(amount)
        }

    }
}
