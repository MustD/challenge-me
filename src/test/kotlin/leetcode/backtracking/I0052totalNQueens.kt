package leetcode.backtracking

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 52. N-Queens II (https://leetcode.com/problems/n-queens-ii/)
 *
 * The n-queens puzzle is the problem of placing n queens on an n×n chessboard such that no two
 * queens attack each other (no two share a row, column, or diagonal). Given an integer n, return
 * the number of distinct solutions to the n-queens puzzle.
 *
 * Constraints:
 * - 1 <= n <= 9
 */
typealias I0052 = (Int) -> Int

class I0052totalNQueens {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0052> {

        override val cases = _root_ide_package_.leetcode.testCases<I0052>(
            4 expects 2,
            1 expects 1,
        )

        @Test
        fun test() = check(::totalNQueens, ::referenceSolution)

        /**
         * ## Analysis (verified: passes n=4 -> 2, n=1 -> 1)
         *
         * ### Pattern
         * Classic **backtracking / DFS over a decision tree** with place-then-undo
         * (`board[row][y] = 1` ... recurse ... `board[row][y] = 0`). The structural
         * invariant "one queen per row" is enforced by recursing one `row` at a time, so
         * conflicts can only come from columns and the two diagonals.
         *
         * ### Time — roughly O(n! * n^2)
         * - The search tree has O(n!) nodes (each row prunes the columns the queens above
         *   already attack), and reaching a leaf at `row == n` counts one solution.
         * - Per `backtrack` node we loop n columns, and each `canPlace` does up to 6 linear
         *   scans of length O(n) -> O(n^2) work per node. Hence ~O(n! * n^2).
         * - With n <= 9 this is tiny (n=9 has only 352 solutions), so it runs instantly;
         *   the n^2 factor is the avoidable part (see alternatives).
         *
         * ### Space — O(n^2)
         * - `board` is n x n = O(n^2), which dominates.
         * - Recursion stack depth is exactly n -> O(n).
         * - Note this is all *working* space; the output is a single Int counter.
         *
         * ### Correctness notes
         * - Sound because `canPlace` is checked *before* placing, and rows >= `row` are
         *   still all-zero during the descent, so scanning the full board never sees a
         *   not-yet-made choice.
         * - Two minor (harmless) inefficiencies worth seeing: the "all on Y" row scan is
         *   fully redundant (the one-queen-per-row recursion already guarantees it), and
         *   every scan covers rows below `row` that are guaranteed empty. Correct, just
         *   ~2x more scanning than needed.
         *
         * ### Alternatives (all O(n!) — only constant-factor wins exist; no sub-factorial
         *   counting algorithm is known)
         * - **O(1) conflict check:** track three sets — `cols`, `row+col` (one diagonal),
         *   `row-col` (the other). `canPlace` becomes O(1) and space drops to O(n). This is
         *   the standard "optimal" form and removes the n^2 factor -> O(n!).
         * - **Bitmask:** represent cols/diagonals as bits in ints and advance with bit
         *   tricks. Same asymptotics, fastest in practice — the usual n-queens benchmark.
         * - **Symmetry:** the board is symmetric, so solving half the first-row columns and
         *   doubling (handling the center separately) buys ~2x.
         *
         * ### Parallelism — genuinely applies, but not worth it at this scale
         * N-queens is embarrassingly parallel at the top: each first-row column choice roots
         * an *independent* subtree whose count can be computed concurrently and summed
         * (map-reduce / fork-join). Speedup ceiling is near-linear since subtrees are
         * independent, minus load imbalance — pruning makes subtrees uneven, so you'd want
         * work-stealing. For n <= 9 the total work is so small that thread spin-up dwarfs it;
         * it only pays off at n >= ~15 (which is exactly why large-n queens is a textbook
         * Cilk/OpenMP fork-join demo).
         *
         * ### Real-world
         * This is constraint satisfaction. In production you reach for a CP solver
         * (OR-Tools, MiniZinc) — Sudoku, timetabling, scheduling, register allocation all
         * reduce to the same backtracking-with-pruning, but solvers add forward checking,
         * AC-3 propagation, and most-constrained-variable heuristics that beat a hand-rolled
         * DFS on real instances. The bitmask queens kernel survives mainly as a benchmark.
         */
        fun totalNQueens(n: Int): Int {
            val board = MutableList(n) { MutableList(n) { 0 } }
            val lastI = n - 1
            fun MutableList<MutableList<Int>>.canPlace(x: Int, y: Int): Boolean {
                return listOf(
                    //all on X
                    { (0..lastI).any { this[it][y] == 1 } },

                    //all on Y
                    { (0..lastI).any { this[x][it] == 1 } },

                    //all up-left
                    { (0..minOf(lastI - x, lastI - y)).any { this[x + it][y + it] == 1 } },

                    //all down-left
                    { (0..minOf(lastI - x, y)).any { this[x + it][y - it] == 1 } },

                    //all down-right
                    { (0..minOf(x, y)).any { this[x - it][y - it] == 1 } },

                    //all up-right
                    { (0..minOf(x, lastI - y)).any { this[x - it][y + it] == 1 } },
                ).any { it() }.not()
            }

            var count = 0
            fun backtrack(row: Int) {
                // Base case: we've successfully filled all n rows -> one complete solution.
                if (row == n) {
                    count++
                    return
                }
                (0..lastI).forEach { y ->
                    if (board.canPlace(row, y)) {
                        board[row][y] = 1
                        backtrack(row + 1)   // descend to the next row
                        board[row][y] = 0    // undo the choice (backtrack)
                    }
                }
            }

            backtrack(0)
            return count
        }

        /**
         * ## Reference solution — the "optimal" O(1)-conflict-check form
         *
         * ### Restatement
         * Count distinct ways to place n queens on an n×n board so none attack another
         * (no shared row, column, or diagonal). Return only the *count*.
         *
         * ### Pattern — backtracking, row by row, with O(1) attack tests
         * Same decision tree as the user's solution (one queen per row, recurse on `row`),
         * but instead of re-scanning the board to detect conflicts we keep three boolean
         * "occupancy" sets and ask each in O(1):
         * - `cols[c]`              — is column `c` already taken?
         * - `diag[row + col]`      — the "↘" anti-diagonal; cells on it share a constant `row+col`.
         * - `antiDiag[row - col]`  — the "↙" diagonal; cells on it share a constant `row-col`
         *                            (shifted by `+ (n-1)` so the index is non-negative, range 0..2n-2).
         *
         * ### Approach
         * 1. At `row`, try every column `c`. It is safe iff none of `cols[c]`,
         *    `diag[row+c]`, `antiDiag[row-c+(n-1)]` is set.
         * 2. Mark all three, recurse on `row + 1`, then unmark (classic place / undo).
         * 3. When `row == n` every row holds a queen — count one solution.
         *
         * ### Complexity
         * - Time: O(n!) — the tree still has factorial leaves; the win over the board-scan
         *   version is dropping its O(n^2)-per-node factor to O(1).
         * - Space: O(n) — three arrays sized n / n / 2n-1 plus recursion depth n. No n×n board.
         *
         * ### Why the diagonal index trick works
         * On any "↘" diagonal `row + col` is invariant; on any "↙" diagonal `row - col` is
         * invariant. `row - col` ranges from -(n-1) to (n-1), so we add `n-1` to land in
         * `0..2n-2` and use a plain array instead of a map.
         *
         * ### Common pitfalls
         * - Forgetting the `+ (n-1)` offset and indexing an array with a negative number.
         * - Sizing the diagonal arrays as `n` instead of `2n - 1` (each diagonal family has
         *   `2n - 1` members).
         * - Counting at the wrong depth (count at `row == n`, after the last row is placed).
         */
        fun referenceSolution(n: Int): Int {
            val cols = BooleanArray(n)
            val diag = BooleanArray(2 * n - 1)      // indexed by row + col
            val antiDiag = BooleanArray(2 * n - 1)  // indexed by row - col + (n - 1)

            var count = 0
            fun backtrack(row: Int) {
                if (row == n) {
                    count++
                    return
                }
                for (col in 0 until n) {
                    val d = row + col
                    val ad = row - col + (n - 1)
                    if (cols[col] || diag[d] || antiDiag[ad]) continue
                    cols[col] = true; diag[d] = true; antiDiag[ad] = true
                    backtrack(row + 1)
                    cols[col] = false; diag[d] = false; antiDiag[ad] = false
                }
            }

            backtrack(0)
            return count
        }

    }
}
