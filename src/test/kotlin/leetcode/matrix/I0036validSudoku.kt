package leetcode.matrix

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 36. Valid Sudoku  (https://leetcode.com/problems/valid-sudoku/)
 *
 * Determine if a 9 x 9 Sudoku board is valid. Only the filled cells need to be validated according to the
 * following rules:
 *   1. Each row must contain the digits 1-9 without repetition.
 *   2. Each column must contain the digits 1-9 without repetition.
 *   3. Each of the nine 3 x 3 sub-boxes of the grid must contain the digits 1-9 without repetition.
 *
 * Note: A board is valid even if it is not necessarily solvable — only the filled cells are checked against
 * the three rules above. Empty cells are represented by '.'.
 *
 * Constraints:
 * - board.length == 9 and board[i].length == 9 (always a full 9 x 9 grid).
 * - Each board[i][j] is a digit '1'-'9' or '.'.
 */
typealias I0036 = (Array<CharArray>) -> Boolean

class I0036validSudoku {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0036> {

        override val cases = leetcode.testCases<I0036>(
            // Example 1 — valid board.
            """
                [["5","3",".",".","7",".",".",".","."]
                ,["6",".",".","1","9","5",".",".","."]
                ,[".","9","8",".",".",".",".","6","."]
                ,["8",".",".",".","6",".",".",".","3"]
                ,["4",".",".","8",".","3",".",".","1"]
                ,["7",".",".",".","2",".",".",".","6"]
                ,[".","6",".",".",".",".","2","8","."]
                ,[".",".",".","4","1","9",".",".","5"]
                ,[".",".",".",".","8",".",".","7","9"]]
            """ expects true,
            // Example 2 — same as Example 1 but top-left cell changed 5 -> 8, so the top-left 3x3 box
            // now has two 8's. Invalid.
            """
                [["8","3",".",".","7",".",".",".","."]
                ,["6",".",".","1","9","5",".",".","."]
                ,[".","9","8",".",".",".",".","6","."]
                ,["8",".",".",".","6",".",".",".","3"]
                ,["4",".",".","8",".","3",".",".","1"]
                ,["7",".",".",".","2",".",".",".","6"]
                ,[".","6",".",".",".",".","2","8","."]
                ,[".",".",".","4","1","9",".",".","5"]
                ,[".",".",".",".","8",".",".","7","9"]]
            """ expects false,
        )

        @Test
        fun test() = check(::isValidSudoku, ::referenceSolution)

        /**
         * Analysis (verified: `test()` passes both official examples).
         *
         * Pattern
         * -------
         * Hash-set duplicate detection over independent groups. The board's three constraints
         * (rows, columns, 3x3 boxes) are checked as three separate scans; within each group a fresh
         * `Set<Char>` records seen digits and `add(...) == false` signals a repeat. '.' cells are
         * simply skipped, which is exactly what "only filled cells are validated" requires — so this
         * correctly reports a *valid* board even when it isn't solvable.
         *
         * Complexity
         * ----------
         * - Time: O(1) in the strict sense — the grid is fixed at 9x9, so this is bounded work.
         *   Generalized to an n x n board it is O(n^2): each of the three passes visits every cell
         *   once (81 cells x 3 = 243 `add` calls in the worst case), and `HashSet.add` is amortized
         *   O(1).
         * - Space: O(1) here (each set holds <= 9 chars). Generalized, O(n) auxiliary — at any instant
         *   only one row/column/box set is live; the box pass discards each 9-element set before the
         *   next box. Output is a single Boolean, so working space dominates.
         *
         * Correctness notes / Kotlin detail worth knowing
         * ------------------------------------------------
         * The `return false` sits inside lambdas passed to `forEach`. This is a **non-local return** —
         * it returns from `isValidSudoku`, not just the lambda — and it only compiles because `forEach`
         * (and `run {}`) are `inline`. With a non-inline higher-order function the bare `return` would
         * be a compile error and you'd need a labeled `return@forEach` (which would *not* short-circuit
         * the whole function). Good instinct to rely on; just know the inline requirement is load-bearing.
         *
         * Alternative approaches
         * ----------------------
         * 1. Single pass, 27 sets: keep 9 row sets, 9 column sets, 9 box sets; visit each cell once and
         *    update all three at that cell. Box index = (i / 3) * 3 + j / 3. Same O(n^2) time but one
         *    traversal instead of three, at O(n^2) space. Trades the current code's readability for
         *    fewer passes.
         * 2. Bitmask instead of HashSet: represent each group's seen digits as a 9-bit `Int`; testing
         *    is `mask and (1 shl d) != 0` and marking is `mask = mask or (1 shl d)`. No hashing / boxing,
         *    O(1) space per group. This is what real Sudoku solvers use — the backtracking search asks
         *    "can digit d go at (i,j)?" millions of times, and a bit-AND makes that answer branch-free.
         * 3. Single set of encoded keys ("r5#3", "c2#3", "b0#3") — elegant one-liner but allocates a
         *    string per cell; slowest in practice.
         *
         * None of these beats O(n^2) asymptotically — every filled cell must be inspected at least once,
         * so O(n^2) is the lower bound for a full board. The choice is purely constant-factor / clarity.
         *
         * Parallelism
         * -----------
         * Not worth it. 243 operations is far below any threading break-even; the row/column/box passes
         * are data-independent and could run on three threads, but the fork/join overhead dwarfs the work
         * by orders of magnitude. The teaching point is recognizing that "independent = parallelizable"
         * is necessary but not sufficient — input size has to clear the overhead floor first.
         *
         * Real-world
         * ----------
         * This exact shape — validate a grid/board against per-region uniqueness — shows up as the
         * feasibility check inside constraint solvers and CSP engines, where the bitmask form (approach 2)
         * is standard because the check is on the inner loop of a backtracking search. At scale the input
         * is rarely a tidy fixed board: think incremental validation as a user edits one cell (recheck
         * only that cell's row/col/box, O(n) not O(n^2)), or a distributed grid where each region is
         * checked independently and results reduced.
         */
        fun isValidSudoku(board: Array<CharArray>): Boolean {
            //1 3x3
            run {
                val ranges = listOf(0..2, 3..5, 6..8)
                ranges.forEach { iRange ->
                    ranges.forEach { jRange ->
                        val digits = mutableSetOf<Char>()
                        iRange.forEach { i ->
                            jRange.forEach { j ->
                                if (board[i][j] != '.') {
                                    if (digits.add(board[i][j]).not()) return false
                                }
                            }
                        }
                    }
                }
            }
            //2 horizontal
            run {
                (0..8).forEach { i ->
                    val digits = mutableSetOf<Char>()
                    (0..8).forEach { j ->
                        if (board[i][j] != '.') {
                            if (digits.add(board[i][j]).not()) return false
                        }
                    }
                }
            }
            //3 vertical
            run {
                (0..8).forEach { j ->
                    val digits = mutableSetOf<Char>()
                    (0..8).forEach { i ->
                        if (board[i][j] != '.') {
                            if (digits.add(board[i][j]).not()) return false
                        }
                    }
                }
            }

            return true
        }

        /**
         * Reference solution — single pass, 27 bitmasks (approach 1 + 2 from the analysis combined).
         *
         * Intuition
         * ---------
         * Your `isValidSudoku` is already correct; this is the "canonical" form to check against. The
         * key realization is that every cell participates in exactly three groups — one row, one column,
         * one 3x3 box — so all three rules can be verified in a *single* traversal instead of three. As
         * we visit cell (i, j) we ask three questions at once: has this digit already appeared in row i,
         * in column j, or in box b? If any answer is yes, the board is invalid.
         *
         * The box a cell belongs to is `b = (i / 3) * 3 + j / 3`: integer-dividing each coordinate by 3
         * collapses the 9 positions into 3 band indices, and the `*3` lays the 3x3 grid of boxes out in
         * row-major order (0..8). Memorize this formula — it recurs in every grid/box problem.
         *
         * Why bitmask instead of HashSet
         * ------------------------------
         * A group only ever holds the 9 digits '1'..'9', so a 9-bit integer is a perfect set: bit d set
         * means "digit d already seen". Testing membership is `mask and (1 shl d) != 0` and inserting is
         * `mask = mask or (1 shl d)` — both branch-free bit ops, no hashing, no boxing, no allocation.
         * Here it is a constant-factor win; in a real Sudoku *solver* the backtracking search asks "can
         * digit d go here?" millions of times and this is the reason bitmasks are the standard encoding.
         *
         * Complexity
         * ----------
         * - Time: O(n^2) — one visit per cell (81 for the fixed board). Same asymptotics as the 3-pass
         *   version, but one traversal and ~1/3 the constant factor.
         * - Space: O(n) — three arrays of n masks (27 ints total). Note this is *more* live state than
         *   your version (which keeps one set alive at a time), traded for the single pass. Both are O(1)
         *   for the fixed 9x9 board.
         *
         * Pitfall preserved from your code: '.' cells must be skipped, never inserted — the rules only
         * validate filled cells, and a board can be "valid but unsolvable".
         */
        fun referenceSolution(board: Array<CharArray>): Boolean {
            val rows = IntArray(9)
            val cols = IntArray(9)
            val boxes = IntArray(9)
            for (i in 0..8) {
                for (j in 0..8) {
                    val c = board[i][j]
                    if (c == '.') continue
                    val bit = 1 shl (c - '1')          // digit '1'..'9' -> bit 0..8
                    val b = (i / 3) * 3 + j / 3
                    if (rows[i] and bit != 0) return false
                    if (cols[j] and bit != 0) return false
                    if (boxes[b] and bit != 0) return false
                    rows[i] = rows[i] or bit
                    cols[j] = cols[j] or bit
                    boxes[b] = boxes[b] or bit
                }
            }
            return true
        }

    }
}
