package leetcode.math

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4061. Minimum Queen Moves to Reach Target  (https://leetcode.com/problems/minimum-queen-moves-to-reach-target/)
 *
 * You are given two integer arrays source = [sr, sc] and target = [tr, tc], representing the positions of a queen
 * and a target square on an 8 x 8 chessboard. Rows and columns are 1-indexed.
 *
 * In one move, the queen can move any number of squares along a row, a column, or a diagonal, without leaving
 * the board.
 *
 * Return the minimum number of moves required for the queen to reach the target square.
 *
 * Constraints:
 * - source.length == target.length == 2
 * - 1 <= source[i], target[i] <= 8
 */
typealias I4061 = (IntArray, IntArray) -> Int

class I4061minQueenMoves {

    @Nested
    inner class Solution : ProblemTest<I4061> {

        override val cases = testCases<I4061>(
            args("[8,1]", "[1,8]") expects 1,
            args("[4,2]", "[1,3]") expects 2,
            args("[1,1]", "[1,1]") expects 0,
            args("[3,3]", "[3,8]") expects 1, // same row
            args("[1,5]", "[8,5]") expects 1, // same column, full board length
            args("[1,1]", "[8,8]") expects 1, // main diagonal, corner to corner
            args("[2,7]", "[6,3]") expects 1, // anti-diagonal, row up / col down (second loop)
            args("[6,3]", "[2,7]") expects 1, // anti-diagonal, row down / col up (first loop)
            args("[1,1]", "[2,3]") expects 2, // knight hop: no shared line
            args("[1,2]", "[8,8]") expects 2, // off-by-one from the main diagonal
            args("[1,8]", "[8,1]") expects 1, // anti-diagonal, corner to corner
            args("[8,8]", "[1,1]") expects 1, // main diagonal, reverse direction
            args("[4,4]", "[5,5]") expects 1, // adjacent diagonal step
            args("[5,4]", "[4,5]") expects 1, // adjacent anti-diagonal step
            args("[8,8]", "[8,8]") expects 0, // same square at max coordinates
            args("[1,1]", "[8,7]") expects 2, // near-diagonal far corner: neither diff nor sum matches
            args("[2,4]", "[4,2]") expects 1, // sum invariant, diff flips sign (abs trap)
        )

        @Test
        fun test() = check(::minQueenMoves)

        /**
         * ## Analysis (validated: 17/17 cases pass)
         *
         * **Time: O(1).** Four constant-time comparisons and an early `return`, with no loops. The 8x8 board size
         * never enters the computation.
         *
         * **Space: O(1).** The four destructured `Int`s (`val (sX, sY) = source`) and nothing else. No recursion.
         *
         * ### Why it is correct
         * - **The answer is only ever 0, 1 or 2.** A queen includes a rook, and a rook reaches any square in at most
         *   two moves: first along the row to column `tY`, then along the column to row `tX`. That corner square
         *   `(sX, tY)` is always on the board, so the "without leaving the board" rule never forces a detour.
         *   This is the key insight: you don't need BFS because the distance is bounded by a structural argument.
         * - **1 move happens exactly when both squares share a line.** There are four kinds of line, each with an
         *   *invariant* that stays constant along it:
         *   - row → `x`, column → `y`
         *   - main diagonal (↘) → `x - y`
         *   - anti-diagonal (↙) → `x + y`
         *
         *   Two squares share a line iff they agree on one of these four invariants. Your four checks are exactly
         *   that list, in the same order. You check equality first, so a match on any invariant really means 1 move.
         * - **The commented-out `abs(sX - tX) == abs(sY - tY)`** is equivalent to your two diagonal checks
         *   combined. `|dx| == |dy|` means `dx == dy` (same `x - y`) or `dx == -dy` (same `x + y`). The invariant
         *   form is the better habit: it names *which* diagonal and has no `abs`. The test `[2,4] -> [4,2]`
         *   (same sum, diff flips sign) covers the case where a one-sided `sX - sY == tX - tY` check would wrongly
         *   say 2.
         * - Ordering pitfall avoided: the `source == target` check must come before the row/column check, or
         *   0 would come back as 1. Yours does.
         *
         * ### Pattern
         * **Line invariants on a grid** (`r`, `c`, `r - c`, `r + c`). It is the same trick as N-Queens'
         * `diag[r - c + n]` / `antiDiag[r + c]` occupancy arrays, Bishop/Queen attack checks, and
         * "Queens That Can Attack the King" (1222). The general move: **bound the answer with a construction, then
         * just classify which bucket the input falls into.** Compare the bishop variant (3001 "Minimum Moves to
         * Capture the Queen"), where colour parity (`(x + y) % 2`) decides between 2 and unreachable.
         *
         * ### Alternatives
         * - **BFS over the 64 squares** with sliding moves in 8 directions. That is O(64 · 8 · 7) here: correct and
         *   general, but it hides the 0/1/2 structure. It is the right tool once obstacles appear (blocked squares
         *   break the "rook in two" bound), and then 0-1 BFS or BFS that expands along each ray until it is blocked
         *   becomes necessary.
         * - **Precomputed 64x64 table.** Also O(1), but pointless when the formula is four comparisons.
         * - Yours is optimal: Ω(1) is trivially the lower bound, since you must read the four inputs.
         *
         * ### Parallelism
         * None worth having. There are four dependent comparisons on scalars, so thread spawn cost would be millions
         * of times the work. The only realistic "parallel" form is a *batch* of queries: vectorise the four
         * comparisons over SoA arrays of coordinates (SIMD compare + blend). That is embarrassingly parallel and
         * memory-bound, but only relevant when you have millions of queries.
         *
         * ### Real world
         * Chess engines use exactly these invariants, but as **bitboards**: precomputed 64-bit masks per rank, file,
         * diagonal and anti-diagonal indexed by `r - c` / `r + c`, so "do these two squares share a line?" is
         * `(lineMask[s] & bit(t)) != 0`. With pieces on the board, sliding attacks need magic bitboards or
         * PEXT lookups, because blockers break the closed form, the same way obstacles would force BFS here.
         * The same diagonal-index trick shows up in raster graphics (Bresenham's diagonal cases) and in
         * grid pathfinding heuristics (Chebyshev / octile distance for 8-directional movement).
         */
        fun minQueenMoves(source: IntArray, target: IntArray): Int {
            val (sX, sY) = source
            val (tX, tY) = target
            if (sX == tX && sY == tY) return 0 //source == target
            if (sX == tX || sY == tY) return 1 //same row/col

            if (sX - sY == tX - tY) return 1 //1:1 to 8:8
            if (sX + sY == tX + tY) return 1 //1:8 to 8:1

            //or
//            if(abs(sX - tX) == abs(sY - tY)) return 1

            return 2
        }

    }
}
