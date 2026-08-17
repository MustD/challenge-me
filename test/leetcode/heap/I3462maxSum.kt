package leetcode.heap

import leetcode.expects
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test

/**
 * 3462. Maximum Sum With at Most K Elements  (https://leetcode.com/problems/maximum-sum-with-at-most-k-elements/)
 *
 * You are given a 2D integer matrix `grid` of size n x m, an integer array `limits` of length n,
 * and an integer `k`. Select a set of elements from `grid` such that:
 * - The number of elements taken from the i-th row of `grid` does not exceed `limits[i]`.
 * - The total number of elements taken across all rows is at most `k`.
 * Return the maximum possible sum of all the selected elements.
 *
 * Constraints:
 * - n == grid.length == limits.length
 * - m == grid[i].length
 * - 1 <= n, m <= 500
 * - 0 <= grid[i][j] <= 10^5
 * - 0 <= limits[i] <= m
 * - 0 <= k <= min(n * m, sum(limits))
 * - Note: sum can reach ~500 * 500 * 10^5, which overflows Int — return type is Long.
 *   k == 0 is a valid input (select nothing → sum 0).
 */
typealias I3462 = (Array<IntArray>, IntArray, Int) -> Long

class I3462maxSum {

    @Nested
    inner class Solution : leetcode.ProblemTest<I3462> {

        override val cases = leetcode.testCases<I3462>(
            leetcode.args("[[1,2],[3,4]]", "[1,2]", 2) expects 7L,
            leetcode.args("[[5,3,7],[8,2,6]]", "[2,2]", 3) expects 21L,
            leetcode.args("[[1,2],[3,4]]", "[1,2]", 0) expects 0L,
            leetcode.args("[[7,10,3,3,7,7,0],[5,5,9,2,10,5,2]]", "[3,7]", 7) expects 53L
        )

        @Test
        fun test() = check(::maxSum, ::referenceSolution)

        /**
         * Analysis of `maxSum` (verified: all 4 cases pass).
         *
         * ## Approach & pattern
         * **Greedy top-k selection via a max-heap per row.** One max-heap is built per row. Then, k
         * times, every row's heap top is scanned to find the globally largest still-eligible element
         * (rows with `limits[i] == 0` are skipped), that element is polled, added to `result`, and the
         * row's budget decremented. Picking the largest available element at each of the k steps is
         * optimal: the objective is purely additive and the only constraints are element *counts*
         * (per-row `limits[i]` and global `k`), so the answer is exactly the sum of the k largest
         * elements subject to the per-row caps — a standard exchange-argument greedy.
         *
         * ## Complexity
         * Let n = rows, m = cols.
         * - **Time: O(n·m·log m + k·n).**
         *   - Heap build: `addAll` inserts element-by-element → O(m log m) per row → O(n·m log m).
         *   - Selection: k iterations, each scanning all n heap tops (peek is O(1)) → O(n) per
         *     iteration, plus one O(log m) `poll` → O(k·(n + log m)) = **O(k·n)** dominant.
         *   - Worst case k ≤ n·m, so the selection phase can reach O(n²·m) (~1.25e8 at n=m=500).
         * - **Space: O(n·m)** auxiliary — every element is copied into the per-row heaps. Output is a
         *   single `Long`; no recursion, so no stack cost.
         *
         * ## Correctness notes
         * - `k == 0` short-circuits to 0 (also guards the empty-selection case).
         * - Rows with `limits[i] == 0` are correctly skipped; a row is polled at most `limits[i] ≤ m`
         *   times, so `peek()` is never called on a drained heap while its budget is > 0.
         * - `Int.MIN_VALUE` sentinel is safe because `grid[i][j] ≥ 0`.
         * - `result` is `Long`, avoiding the ~500·500·1e5 Int overflow flagged in the constraints.
         * - Note: `limits` is mutated in place (decremented). Harmless here since inputs are re-parsed
         *   per run by the harness, but a "pure" version would copy it.
         *
         * ## Alternative approaches
         * - **Global "heap of row-tops" (asymptotically better selection).** Keep one priority queue of
         *   (rowTop, rowIdx). Pop the max, add it, decrement that row's budget, and push the row's next
         *   element if budget remains. This replaces the O(k·n) repeated scan with O((n + k)·log n),
         *   i.e. total O(n·m log m + k log n) — a real win when k and n are large.
         * - **Decoupled top-k (also proves the greedy).** The per-row cap and global cap are
         *   independent, so the candidate pool is just the top `min(limits[i], m)` values of each row;
         *   the answer is the sum of the k largest candidates. Extract per-row tops (sort O(m log m) or
         *   quickselect O(m)), then take global top-k with a size-k min-heap (Guava's
         *   `Ordering.greatestOf` is exactly this) in O(C log k), C = Σ min(limits[i], m).
         * - Their heap-per-row solution is clean and well within limits; the only sub-optimality is the
         *   linear rescan of all rows on every one of the k picks.
         *
         * ## Parallelism
         * - Building the n row-heaps is embarrassingly parallel (rows independent), and the decoupled
         *   formulation makes per-row top-extraction a pure map step. The greedy *selection* loop is
         *   inherently sequential — each pick depends on the mutated budgets/heaps — but reformulating
         *   to "top `limits[i]` per row, then global top-k" turns most of the work into a parallel
         *   map + parallel reduction.
         * - Honestly not worth threading here: n·m ≤ 250k elements, so thread/heap overhead dominates.
         *   The teaching point is that greedy's data dependency blocks parallelism until you reformulate
         *   it into independent subproblems.
         *
         * ## Real-world
         * "Top-k with per-group quotas" is common: recommendation/ad ranking (top items with per-category
         * caps for diversity), budget/portfolio allocation, and SQL "top-k per group then global LIMIT".
         * At scale you'd reach for a bounded min-heap / partial-sort (C++ `nth_element`, Guava
         * `greatestOf`, Spark `takeOrdered`) rather than a repeated full scan, and for unbounded streams
         * a bounded priority queue or approximate top-k sketch.
         */
        fun maxSum(grid: Array<IntArray>, limits: IntArray, k: Int): Long {
            if (k == 0) return 0
            val heaps = grid.map { nums ->
                PriorityQueue<Int>(compareByDescending { it }).apply { addAll(nums.toList()) }
            }
            var result = 0L
            repeat(k) {

                var nextMax: Pair<Int, PriorityQueue<Int>?> = 0 to null
                heaps.forEachIndexed { idx, heap ->
                    if (limits[idx] > 0) {
                        val (curIdx, curMax) = nextMax
                        if (heap.peek() > (curMax?.peek() ?: Int.MIN_VALUE)) nextMax = idx to heap
                    }
                }

                val (nextIdx, nextHeap) = nextMax
                nextHeap?.let {
                    result += it.poll()
                    limits[nextIdx]--
                }
            }


            return result
        }

        /**
         * Reference solution — same greedy, better selection loop (the "global heap of row-tops"
         * alternative flagged in the analysis above).
         *
         * ## Restatement
         * Pick at most `k` numbers total, at most `limits[i]` of them from row `i`, to maximize the
         * sum. All numbers are non-negative, so you never gain by taking fewer than allowed — the
         * answer is exactly the `k` largest numbers subject to the per-row caps.
         *
         * ## Pattern — greedy + k-way merge via a single max-heap
         * The key reformulation: sort each row descending. Then within a row the eligible candidates
         * are just its first `limits[i]` values, and they are consumed in order. So the whole problem
         * becomes a **k-way merge**: pick the largest current head across all rows, `k` times. That is
         * exactly what a size-`n` max-heap of "row heads" does — the classic "merge k sorted lists"
         * machine. `maxSum` above does the same greedy but rescans all `n` rows on every pick (O(k·n));
         * seeding one heap with the heads and advancing only the row you popped makes each pick O(log n).
         *
         * ## Approach
         * 1. Sort each row descending (index `j` = the (j+1)-th largest in that row).
         * 2. Seed a max-heap with each row's head `(value, rowIdx, 0)`, skipping rows with `limits[i]==0`.
         * 3. Pop the max `k` times: add its value, then push the row's next element `j+1` iff the row
         *    still has budget (`j+1 < limits[row]`) and more elements (`j+1 < size`).
         *
         * ## Complexity
         * - Time **O(n·m·log m + (n + k)·log n)** — sorting the rows dominates the setup; the merge does
         *   at most `n` initial inserts and `k` pop/push pairs, each O(log n). Strictly better than the
         *   O(k·n) rescan when `k` and `n` are large.
         * - Space **O(n·m)** for the sorted copies, plus **O(n)** for the heap.
         *
         * ## Pitfalls
         * - Sum overflows `Int` (~500·500·1e5) → accumulate in `Long`.
         * - `k == 0` must return 0 (take nothing); a row with `limits[i]==0` must never be seeded.
         * - Budget bound is on *count*: row `i` may give its top `limits[i]` elements only, so guard the
         *   "push next" with `j + 1 < limits[row]`, not just array bounds.
         * - Don't mutate `limits` here (the greedy `maxSum` decrements it in place) — the row index `j`
         *   carries the per-row progress instead, keeping the function pure.
         */
        fun referenceSolution(grid: Array<IntArray>, limits: IntArray, k: Int): Long {
            if (k == 0) return 0L
            val rows = grid.map { it.sortedDescending() }
            // Max-heap of [value, rowIdx, idxWithinRow], ordered by value descending.
            val pq = PriorityQueue<IntArray>(compareByDescending { it[0] })
            for (i in rows.indices) {
                if (limits[i] > 0) pq.add(intArrayOf(rows[i][0], i, 0))
            }
            var result = 0L
            var picks = 0
            while (picks < k && pq.isNotEmpty()) {
                val (value, row, j) = pq.poll()
                result += value
                picks++
                if (j + 1 < limits[row] && j + 1 < rows[row].size) {
                    pq.add(intArrayOf(rows[row][j + 1], row, j + 1))
                }
            }
            return result
        }

    }
}
