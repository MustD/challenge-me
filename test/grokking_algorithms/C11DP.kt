package grokking_algorithms

import kotlin.test.Test

/**
 * Grokking Algorithms — dynamic programming: the **0/1 knapsack** problem.
 *
 * A thief with a knapsack that holds `maxWeight` pounds stands in a store. Each item `i` has a value `cost[i]` and a
 * weight `weight[i]`. Every item may be taken **at most once** (you cannot take half a stereo, and there is only one of
 * each) — hence "0/1". Maximise the total value carried out.
 *
 * The book's running example, scaled down by 100:
 * ```
 *   stereo  $30  4 lb
 *   laptop  $20  3 lb
 *   guitar  $15  1 lb
 *   knapsack capacity: 4 lb   ->  best is laptop + guitar = $35
 * ```
 * Note the trap that makes this a DP and not a greedy: the single most valuable item (the stereo, $30) fills the bag
 * completely and is *worse* than the two cheaper items that fit alongside each other.
 *
 * NOTE: despite the function name below, this is not LeetCode 198 "House Robber" (that one is 1-D DP over a line of
 * houses with a no-two-adjacent constraint — see `leetcode/I0198rob.kt`). Same family, different recurrence.
 */
class C11DP {

    /**
     * Your slot — implement it, then wire it into `check` in the test below.
     */
    fun houseRobber(
        cost: List<Int> = listOf(30, 20, 15),
        weight: List<Int> = listOf(4, 3, 1),
        maxWeight: Int = 4,
    ): Int {
        if (cost.isEmpty()) return 0
        val dp = List(cost.size) { MutableList(maxWeight + 1) { 0 } }

        for (itemIdx in cost.indices) {
            val itemCost = cost[itemIdx]
            val itemWeight = weight[itemIdx]
            for (size in 1..maxWeight) {
                val prev = dp.getOrNull(itemIdx - 1)?.getOrNull(size) ?: 0
                val prevWithIt = dp.getOrNull(itemIdx - 1)?.getOrNull(size - itemWeight)?.let { it + itemCost } ?: 0
                val result = if (size >= itemWeight) maxOf(prev, prevWithIt, itemCost)
                else prev
                dp[itemIdx][size] = result
            }
        }

        return dp.last().last()
    }

    /**
     * ### Intuition
     * The brute force is "try every subset of items, keep the best one that fits" — 2^n subsets. The reason that is
     * wasteful is that the same *sub-question* keeps coming back: after deciding about the first few items, all that
     * matters about the past is **how much capacity is left**, not which particular items produced it. Two different
     * item choices that both leave 2 lb free face an identical remaining problem. DP is exactly the trick of noticing
     * that collapse and answering each distinct sub-question once.
     *
     * So the state is a pair: *how many items have I been offered* × *how much capacity remains*. That is the grid the
     * book draws — rows = items, columns = capacities `0..maxWeight`.
     *
     * ### Pattern: 2-D DP over (items × capacity)
     * Define
     * ```
     * dp[i][w] = the best value achievable using only the first i items, with a knapsack of capacity w
     * ```
     * For item `i` (0-based `i - 1` in the arrays) there are exactly two choices, and they are exhaustive:
     *
     * - **Skip it** → the answer is whatever the previous row already achieved: `dp[i - 1][w]`.
     * - **Take it** (only legal if `weight[i-1] <= w`) → collect `cost[i-1]`, and solve the *smaller* problem of
     *   filling the remaining capacity with the remaining items: `cost[i-1] + dp[i - 1][w - weight[i-1]]`.
     *
     * ```
     * dp[i][w] = max( dp[i-1][w],  cost[i-1] + dp[i-1][w - weight[i-1]] )        // second term if it fits
     * dp[0][w] = 0                                                              // no items -> no value
     * ```
     *
     * Two properties make this legal, and they are the two things to check for *any* DP:
     * - **Optimal substructure** — an optimal solution containing item `i` must use an optimal solution of the
     *   sub-problem `(first i-1 items, capacity w - weight[i-1])`; otherwise you could swap in the better sub-solution.
     * - **Overlapping sub-problems** — those `(i, w)` cells are reached over and over by different item subsets, which
     *   is what turns 2^n into n × maxWeight.
     *
     * The key detail, and the thing beginners get wrong: the "take" branch reads from **row `i - 1`**, never row `i`.
     * That is precisely what enforces *at most one copy* of each item. Reading `dp[i][w - weight]` instead would let
     * item `i` be reused — that variant is the **unbounded knapsack** (coin change with unlimited coins).
     *
     * ### Filling the table for the book's example
     * ```
     *            w=0   1    2    3    4
     * (none)      0    0    0    0    0
     * stereo 30/4 0    0    0    0   30
     * laptop 20/3 0    0    0   20   30
     * guitar 15/1 0   15   15   20   35   <- answer: dp[3][4] = 35 (laptop + guitar)
     * ```
     * Read the last row left to right and watch the recurrence work: at `w=4` the guitar row asks
     * `max(30, 15 + dp[laptop][3]) = max(30, 15 + 20) = 35`. The stereo's 30 gets *out-competed* only once the grid can
     * express "15 plus the best use of the other 3 pounds".
     *
     * ### Complexity
     * - **Time O(n · maxWeight)** — one constant-work cell per (item, capacity).
     * - **Space O(n · maxWeight)** for this version; `referenceSolution2` collapses it to **O(maxWeight)**.
     *
     * This is *pseudo-polynomial*, not polynomial: the cost grows with the numeric **value** of `maxWeight`, not with
     * the number of bits needed to write it. Doubling capacity doubles the work, which is why knapsack is NP-hard in
     * general yet trivially solvable for small integer capacities. Worth knowing — it is the standard follow-up
     * question after "what's the complexity?".
     *
     * ### Common pitfalls
     * - **Off-by-one between rows and item indices.** The table has `n + 1` rows so that row 0 ("no items") gives the
     *   base case for free; row `i` therefore describes item `i - 1`. Mixing these up is the #1 bug here.
     * - **Forgetting the `w + 1` column count.** Capacity `0` is a real column; sizing the grid `maxWeight` instead of
     *   `maxWeight + 1` silently drops the answer column.
     * - **Fractional weights break the grid.** The columns are integer capacities; with weights like 2.5 lb you must
     *   rescale to integers (× 2) or the state space is not enumerable.
     * - **Greedy by value, or by value-per-pound, is wrong.** Value/weight ratio is the *fractional* knapsack solution;
     *   for 0/1 it fails, e.g. `cost = [15, 20, 20]`, `weight = [1, 3, 1]`, capacity 4.
     * - **Item order does not matter.** If reordering the input changes your answer, the recurrence is reading the
     *   wrong row (the reuse bug above).
     *
     * ### Extensions worth trying next, in increasing difficulty
     * - **Reconstruct the chosen items**: walk back from `dp[n][maxWeight]`; if `dp[i][w] != dp[i-1][w]` then item
     *   `i - 1` was taken, so record it and jump to `dp[i-1][w - weight[i-1]]`. (Cheap because the full grid is kept —
     *   this is what you give up in `referenceSolution2`.)
     * - **Unbounded knapsack** — change the read to the *current* row and see the meaning shift.
     * - LeetCode **416. Partition Equal Subset Sum** and **494. Target Sum** are 0/1 knapsack in disguise
     *   (`cost == weight`, capacity `sum / 2`). Recognising the disguise is the transferable skill.
     */
    fun referenceSolution(
        cost: List<Int>,
        weight: List<Int>,
        maxWeight: Int,
    ): Int {
        val n = cost.size
        // dp[i][w] — best value using the first i items within capacity w. Row 0 and column 0 stay 0 (base cases).
        val dp = Array(n + 1) { IntArray(maxWeight + 1) }

        for (i in 1..n) {
            val itemCost = cost[i - 1]
            val itemWeight = weight[i - 1]
            for (w in 0..maxWeight) {
                val skip = dp[i - 1][w]
                // Reading row i - 1 (not i) is what forbids taking the same item twice.
                val take = if (itemWeight <= w) itemCost + dp[i - 1][w - itemWeight] else 0
                dp[i][w] = maxOf(skip, take)
            }
        }

        return dp[n][maxWeight]
    }

    /**
     * Same recurrence, one row instead of a grid — O(n · maxWeight) time, **O(maxWeight)** space.
     *
     * Each cell only ever reads the row above at a column `<= w`, so a single array can be updated in place *provided*
     * the stale (previous-row) values survive until they are read. Iterating capacity **downwards** guarantees that:
     * when writing `dp[w]`, the cell `dp[w - itemWeight]` sits to the left and has not been touched yet this iteration,
     * so it still holds the row-`i-1` value.
     *
     * **This descending loop is the single most important line in the space-optimised form.** Flip it to ascending and
     * `dp[w - itemWeight]` will already have been updated *with the current item*, letting that item be counted
     * repeatedly — you silently switch to unbounded knapsack. The test case `cost = [10, 10]`, `weight = [3, 3]`,
     * capacity 3 pins this down: correct answer 10, ascending loop gives 20.
     */
    fun referenceSolution2(
        cost: List<Int>,
        weight: List<Int>,
        maxWeight: Int,
    ): Int {
        val dp = IntArray(maxWeight + 1)

        for (i in cost.indices) {
            val itemCost = cost[i]
            val itemWeight = weight[i]
            for (w in maxWeight downTo itemWeight) {          // descending: keeps dp[w - itemWeight] on the old row
                dp[w] = maxOf(dp[w], itemCost + dp[w - itemWeight])
            }
        }

        return dp[maxWeight]
    }

    private data class Case(
        val cost: List<Int>,
        val weight: List<Int>,
        val maxWeight: Int,
        val expected: Int,
        val note: String,
    )

    @Test
    fun houseRobberTest() {
        val cases = listOf(
            Case(listOf(30, 20, 15), listOf(4, 3, 1), 4, 35, "book example: laptop + guitar beats the lone stereo"),
            Case(listOf(30, 20, 15), listOf(4, 3, 1), 3, 20, "capacity 3: laptop only"),
            Case(listOf(30, 20, 15), listOf(4, 3, 1), 1, 15, "capacity 1: guitar only"),
            Case(listOf(30, 20, 15), listOf(4, 3, 1), 0, 0, "capacity 0: nothing fits"),
            Case(listOf(30, 20, 15), listOf(4, 3, 1), 8, 65, "capacity 8: everything fits"),
            Case(listOf(30, 20, 15, 20), listOf(4, 3, 1, 1), 4, 40, "book's iPhone variant: laptop + iPhone"),
            Case(emptyList(), emptyList(), 4, 0, "no items"),
            Case(listOf(10), listOf(5), 4, 0, "single item heavier than the knapsack"),
            Case(listOf(10, 10), listOf(3, 3), 3, 10, "0/1: each item once — ascending inner loop would say 20"),
            Case(listOf(15), listOf(1), 4, 15, "not unbounded: one guitar is 15, not 4 x 15"),
            Case(listOf(15, 20, 20), listOf(1, 3, 1), 4, 40, "greedy by value/weight ratio would stop at 20 + 15 = 35"),
        )

        val solutions = listOf<Pair<String, (List<Int>, List<Int>, Int) -> Int>>(
            "referenceSolution" to ::referenceSolution,
            "referenceSolution2" to ::referenceSolution2,
            "mySolution" to ::houseRobber,
        )

        solutions.forEach { (name, solution) ->
            cases.forEach { case ->
                val actual = solution(case.cost, case.weight, case.maxWeight)
                assert(actual == case.expected) {
                    "$name(${case.cost}, ${case.weight}, ${case.maxWeight}) was $actual, " +
                            "but expected ${case.expected} — ${case.note}"
                }
            }
        }
    }
}
