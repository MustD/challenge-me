package leetcode.dp_1d

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 746. Min Cost Climbing Stairs  (https://leetcode.com/problems/min-cost-climbing-stairs/)
 *
 * You are given an integer array `cost` where `cost[i]` is the cost of the i-th step on a staircase.
 * Once you pay the cost you can climb either one or two steps. You may start from the step with
 * index 0 or the step with index 1. Return the minimum cost to reach the top of the floor — the
 * position just past the last step (index `cost.size`).
 *
 * Constraints:
 * - 2 <= cost.size <= 1000
 * - 0 <= cost[i] <= 999
 * - The "top" is index `cost.size`, not the last element — stepping off the last step is free.
 * - Starting on either index 0 or index 1 is free; you pay a step's cost only when you leave it.
 */
typealias I0746 = (IntArray) -> Int

class I0746minCostClimbingStairs {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0746> {

        override val cases = leetcode.testCases<I0746>(
            "[10,15,20]" expects 15,
            "[1,100,1,1,1,100,1,1,100,1]" expects 6,
            "[10,15]" expects 10,
            "[0,0,0,0]" expects 0,
            "[1,2,3]" expects 2,
        )

        @Test
        fun test() = check(::minCostClimbingStairs)

        /**
         * Bottom-up 1-D dynamic programming (tabulation).
         *
         * ## State & recurrence
         * `dp[i]` = minimum total cost paid to be *standing on* step `i` (i.e. `cost[i]` is already
         * included, so from `i` you may leave for free to `i+1` or `i+2`).
         *
         * ```
         * dp[i] = cost[i] + min(dp[i-1], dp[i-2])
         * ```
         *
         * The two "free start" positions fall out of the `getOrElse(...) { 0 }` guards rather than
         * needing explicit base cases: at `i = 0` both predecessors are missing so `dp[0] = cost[0]`,
         * and at `i = 1` the `i-2` lookup yields `0`, so `dp[1] = min(dp[0] + cost[1], cost[1]) = cost[1]`.
         * That is exactly "starting on index 0 or 1 is free". Neat, and it is the single trickiest
         * part of this problem — most wrong answers here come from forcing a start at index 0.
         *
         * The answer is `min(dp[n-1], dp[n-2])`: the top is index `n`, which is reachable in one hop
         * from either of the last two steps, and stepping off is free. Guaranteed safe because the
         * constraints promise `n >= 2`.
         *
         * ## Complexity
         * - **Time: O(n)** — one `forEachIndexed` pass over `cost`; each iteration does two O(1)
         *   `getOrElse` index lookups on an `ArrayList` and one `minOf`. No nesting, no recursion.
         * - **Space: O(n)** auxiliary — the `dp` list of `n` boxed `Int`s. No recursion stack.
         *   Output space is O(1) (a single `Int`), so the list is pure working space.
         *
         * ## Correctness notes
         * - Optimal substructure holds: step `i` is reachable only from `i-1` or `i-2`, and the cost
         *   of the prefix is independent of what happens after `i`, so the greedy-looking `min` over
         *   two predecessors is safe. (A plain greedy "always take the cheaper next step" is *not* —
         *   `[1,100,1,1,1,100,1,1,100,1]` is the case that punishes it.)
         * - `MutableList(n) { Int.MAX_VALUE }` is only a sentinel; every cell is overwritten before it
         *   is read, so no `MAX_VALUE + cost` overflow can occur. Still, seeding with `0` would make
         *   that impossible-by-construction rather than true-by-argument.
         * - Overflow is a non-issue anyway: `n <= 1000`, `cost[i] <= 999`, so the total is bounded by
         *   999_000, comfortably inside `Int`.
         * - Edge cases: `n == 2` returns `min(cost[0], cost[1])` (start on the cheaper step, hop
         *   straight to the top) — case `[10,15] -> 10`; all-zero input returns 0.
         * - Leftover `println(dp)` is debug output — harmless for the harness, but worth deleting.
         *
         * ## Pattern
         * Classic **1-D bottom-up DP over a linear scan with a fixed-width lookback** (here, window
         * of 2). Same skeleton as Fibonacci, Climbing Stairs (70), House Robber (198), Delete and Earn
         * (740), Decode Ways (91). Recognise it whenever `answer[i]` depends on a constant number of
         * earlier indices — that constant is exactly what lets you drop the array (see below).
         *
         * ## Alternatives
         * - **O(1) space rolling variables.** Because the recurrence looks back at most two cells,
         *   the whole `dp` list can collapse into two `Int`s:
         *   ```
         *   var prev2 = 0; var prev1 = 0
         *   for (c in cost) { val cur = c + minOf(prev1, prev2); prev2 = prev1; prev1 = cur }
         *   return minOf(prev1, prev2)
         *   ```
         *   Same O(n) time, O(1) space, and it also avoids the boxing that `MutableList<Int>` incurs.
         *   This is the canonical "optimal" form; the array version is the better *teaching* form
         *   because the table is inspectable.
         * - **Top-down memoised recursion** — `f(i) = cost[i] + min(f(i-1), f(i-2))` with a cache.
         *   Same O(n)/O(n), but adds an O(n) call stack (up to 1000 frames here — fine, but a stack
         *   overflow risk if the bound grew).
         * - **Naive recursion without memo** — O(2^n), exponential. Useful only as the thing DP fixes.
         * - Asymptotically, **O(n) time is optimal**: every element of `cost` can change the answer,
         *   so any correct algorithm must read all `n` inputs. Only the space constant is improvable.
         *
         * ## Parallelism
         * Not applicable, and that is the lesson. The recurrence is a strict serial dependency chain:
         * `dp[i]` cannot start until `dp[i-1]` is known, so the critical path is `n` long and no
         * partitioning of the loop helps. (Formally this is a min-plus / tropical-semiring linear
         * recurrence; it *can* be parallelised by a scan over 2x2 min-plus matrices in O(log n) depth,
         * but with `n <= 1000` the thread/vector overhead dwarfs a loop that finishes in microseconds.)
         * If you ever had a *batch* of independent staircases, the parallelism is at that outer level —
         * embarrassingly parallel across inputs, not within one.
         *
         * ## Real world
         * This is a shortest-path-on-a-DAG in disguise: nodes are steps, edges go `i -> i+1` and
         * `i -> i+2`, edge weight is the entry cost. The linear scan is just Bellman-Ford relaxation in
         * topological order. In production the same shape shows up in:
         * - **Text layout / line breaking** (Knuth-Plass): minimum "badness" to reach each break point,
         *   with a bounded lookback window — structurally identical, just a wider window.
         * - **Speech / sequence decoding** (Viterbi): min-cost path through a trellis, same relaxation.
         * - **Query planners and build schedulers**: min-cost DAG traversal where the "steps" are
         *   operator choices or cached artifacts.
         * The realistic differences from the interview version: the cost function is usually not a
         * precomputed array but an expensive call (so you memoise the *cost*, not just the DP), the
         * lookback window is wider than 2 (so the O(1)-space trick becomes a ring buffer of size `k`),
         * and inputs can be streaming — which the rolling-variable form handles natively while the
         * full-table form does not.
         */
        fun minCostClimbingStairs(cost: IntArray): Int {
            val dp = MutableList(cost.size) { Int.MAX_VALUE }
            cost.forEachIndexed { index, cellPrice ->
                val minusOneCost = dp.getOrElse(index - 1) { 0 }
                val minusTwoCost = dp.getOrElse(index - 2) { 0 }
                dp[index] = minOf(minusOneCost + cellPrice, minusTwoCost + cellPrice)
            }
            println(dp)
            return minOf(dp.last(), dp[dp.lastIndex - 1])
        }

    }
}
