package leetcode.math

import leetcode.args
import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.math.abs
import kotlin.test.Test

/**
 * 4034. Minimum Bishop Moves to Reach Target  (https://leetcode.com/problems/minimum-bishop-moves-to-reach-target/)
 *
 * There is an 8 x 8 empty chessboard with 1-indexed rows and columns.
 *
 * You are given an array source = [sr, sc] representing the starting position of a bishop, and an array
 * target = [tr, tc] representing the target position.
 *
 * In one move, the bishop travels one or more squares along a single diagonal direction, staying within the board.
 *
 * Return the minimum number of moves for the bishop to land exactly on target. If it can never reach target,
 * return -1.
 *
 * Constraints:
 * - source.length == target.length == 2
 * - 1 <= sr, sc, tr, tc <= 8
 * - source != target
 */
typealias I4034 = (IntArray, IntArray) -> Int

class I4034minBishopMoves {

    @Nested
    inner class Solution : leetcode.ProblemTest<I4034> {

        override val cases = leetcode.testCases<I4034>(
            args("[8,1]", "[1,8]") expects 1,
            args("[4,2]", "[1,3]") expects 2,
            args("[1,1]", "[3,4]") expects -1,
            args("[1,1]", "[8,8]") expects 1,   // main diagonal corner-to-corner, direction (+1,+1), full length
            args("[6,6]", "[2,2]") expects 1,   // direction (-1,-1)
            args("[2,7]", "[7,2]") expects 1,   // direction (+1,-1)
            args("[4,4]", "[5,5]") expects 1,   // adjacent diagonal square (one-step walk)
            args("[1,1]", "[1,3]") expects 2,   // same colour, same row -> needs an intermediate square
            args("[1,8]", "[8,8]") expects -1,  // opposite colours along an edge (sum 9 vs 16)
            args("[2,1]", "[1,2]") expects 1,   // anti-diagonal hugging the board edge
            args("[8,1]", "[2,1]") expects 2,   // same colour on the same column, from a corner
        )

        @Test
        fun test() = check(::minBishopMoves, ::referenceSolution)

        /**
         * ## Analysis (validated: 3 original + 8 added cases pass)
         *
         * ### Idea
         * The answer can only be -1, 1 or 2. That follows from three facts:
         * - **Colour invariant**: a diagonal step changes both row and col by ±1, so `(r + c) % 2` never changes.
         *   Different colours mean the target can never be reached → -1.
         * - **One move**: the target is on one of the source's diagonals, so `|sr - tr| == |sc - tc|`.
         * - **Two moves otherwise**: two squares of the same colour always share an intermediate square. The
         *   source's "/" diagonal crosses the target's "\" diagonal at a square with integer coordinates because the
         *   parities match. One of the two such crossings is always on the 8x8 board.
         *
         * ### Correctness of this code
         * - `isBlack` is a hand-written `(x + y) % 2 == 0`. Both branches agree with it: odd+odd and even+even are even.
         * - `foundInDiag` compares with the target *before* it checks the bounds. That order is safe only because
         *   `target` is always on the board. If the bounds check came first, the result would be the same, so this
         *   is not a bug, but it is worth noticing.
         * - The colour check comes before the diagonal scan, so the scan only runs on reachable targets. The
         *   "else 2" fallback depends on the "always 2 when same colour" fact above. That fact is the real
         *   insight here, and the comment block above the code shows you found it.
         * - All four directions are covered (the added cases test each one), along with edge and corner squares.
         *   `source != target` is guaranteed, so a 0-move case never comes up.
         *
         * ### Complexity
         * - **Time**: O(1). Each `foundInDiag` call walks at most 7 squares, and there are 4 directions, so at most
         *   about 28 steps. On an n×n board the same code would be O(n), because the scan walks the diagonal.
         * - **Space**: O(1) auxiliary in theory. `foundInDiag` is recursive, though, so it uses up to 7 stack frames
         *   per direction (O(n) stack on an n×n board). It is not `tailrec`. The call is in tail position, so adding
         *   `tailrec` would turn it into a loop for free. Each call also allocates a fresh `IntArray` for the direction.
         *
         * ### Pattern
         * **Invariant / parity argument + closed-form case analysis.** This is a math problem that looks like a
         * graph problem. You could BFS over the 64 squares, but once you see the invariant (colour) and bound the
         * diameter (≤ 2), the search is unnecessary. The same thinking applies to knight-parity puzzles, the
         * "can you tile this board with dominoes" mutilated-chessboard problem, and grid problems where
         * `(r + c) % 2` or `r - c` / `r + c` define equivalence classes.
         *
         * ### Alternatives
         * - **Closed form, O(1) with no loops**:
         *   ```
         *   // if ((sr + sc) % 2 != (tr + tc) % 2) return -1
         *   // return if (abs(sr - tr) == abs(sc - tc)) 1 else 2
         *   ```
         *   This replaces the 4-direction walk with one arithmetic check: `r - c` is constant on "\" diagonals and
         *   `r + c` on "/" diagonals. It does the same thing as your scan, and it also works on boards of any size.
         * - **BFS on the 64 squares**: always correct, and it generalises to boards with obstacles or blocked
         *   squares, where the "≤ 2" shortcut no longer holds. It costs O(n³) here because each square has O(n)
         *   sliding moves. It is the right tool once pieces can block the bishop.
         *
         * ### Parallelism
         * Not applicable. The input is two coordinates and the work is a constant handful of comparisons, so thread
         * startup alone would cost more than the whole computation. The four direction scans are independent, but
         * they are about 28 integer operations in total.
         *
         * ### Real world
         * - Chess engines never search for this. They precompute diagonal masks (bitboards; "magic bitboards"
         *   handle blocking pieces) and test reachability with one AND, which is the same `r ± c` idea packed
         *   into 64-bit words.
         * - More generally, look for an invariant before you search. Parity/colouring arguments let schedulers,
         *   puzzle solvers and model checkers prune whole unreachable state spaces up front.
         */
        fun minBishopMoves(source: IntArray, target: IntArray): Int {
            //board[1][1] is black
            fun isBlack(coords: IntArray): Boolean {
                val (x, y) = coords
                return if (x % 2 == 0) {
                    y % 2 == 0
                } else {
                    (y % 2 == 0).not()
                }
            }

            if (isBlack(source) != isBlack(target)) return -1 //not reachable colors are different


            tailrec fun foundInDiag(x: Int = source[0], y: Int = source[1], direction: IntArray): Boolean {
                val nX = x + direction[0]
                val nY = y + direction[1]
                return when {
                    nX == target[0] && nY == target[1] -> true
                    nX !in 1..8 || nY !in 1..8 -> false
                    else -> foundInDiag(nX, nY, direction)
                }
            }


            if (foundInDiag(direction = intArrayOf(1, 1))) return 1
            if (foundInDiag(direction = intArrayOf(1, -1))) return 1
            if (foundInDiag(direction = intArrayOf(-1, 1))) return 1
            if (foundInDiag(direction = intArrayOf(-1, -1))) return 1
            return 2
        }

        /**
         * ## Reference solution — closed form
         *
         * ### Restatement
         * A bishop is on one square of an empty 8x8 board and has to land on another square. Each move slides it
         * any distance along one diagonal. Return the fewest moves needed, or -1 if the target can never be reached.
         *
         * ### Pattern: invariant / parity argument, then case analysis on a tiny answer space
         * This looks like a shortest-path problem (BFS over 64 squares), but the movement rule has a strong
         * **invariant**, and the board's **diameter** for this piece is at most 2. Once you know the answer is
         * one of {-1, 1, 2}, you only need to tell those three cases apart, and you can do that with arithmetic
         * instead of a search.
         *
         * ### Intuition
         * Every diagonal step changes the row and the column by ±1 each, so `r + c` changes by 0 or ±2. Its
         * parity (the square's colour) never changes. A bishop on a light square stays on light squares, so
         * different colours give -1. If both squares are the same colour, either they share a diagonal (1 move)
         * or the source's "/" diagonal crosses the target's "\" diagonal at a square that is also on the board
         * (2 moves). It never takes more than 2 moves.
         *
         * ### Core concepts
         * - **Invariant**: a quantity that no legal move can change. Here it is `(r + c) % 2`. If source and
         *   target differ in it, no sequence of moves connects them, so you can return -1 without searching.
         * - **Diagonal identifiers**: `r - c` is constant along a "\" diagonal and `r + c` is constant along a
         *   "/" diagonal. Two squares share a diagonal exactly when one of these matches. That is the same as
         *   `|Δr| == |Δc|`. This trick shows up in N-Queens, diagonal-traversal problems and bitboards.
         * - **Diagonal intersection**: the "/" line `r + c = a` and the "\" line `r - c = b` meet at
         *   `r = (a + b) / 2`, `c = (a - b) / 2`. That point is a whole-number square only when `a` and `b` have
         *   the same parity, which holds when the two squares are the same colour.
         * - **Bounded diameter**: if every reachable pair is at most k moves apart, you can replace a search with
         *   a check for each distance from 0 to k.
         *
         * ### Approach
         * 1. If `(sr + sc) % 2 != (tr + tc) % 2`, the colours differ, so return -1.
         * 2. If `|sr - tr| == |sc - tc|`, the squares share a diagonal, so return 1. (`source != target` is
         *    guaranteed, so this is never the 0-distance case.)
         * 3. Otherwise return 2. Use one of the two diagonal crossings as the stop in between. At least one of
         *    them is always on the board. For example, from (1,1) to (1,3), the crossing is (2,2).
         *
         * ### Complexity
         * - **Time**: O(1). There are two parity checks and one absolute-value comparison, with no loops, and the
         *   cost does not depend on the board size.
         * - **Space**: O(1). It only uses a few local integers.
         *
         * ### Common pitfalls
         * - **Missing the colour check**, so the code returns 2 for an unreachable target. Check the invariant
         *   before anything else.
         * - **Using `r - c` for the colour test.** In Kotlin `%` keeps the sign, so `(1 - 2) % 2 == -1` while
         *   `(2 - 1) % 2 == 1`, even though both squares are the same colour. Use `r + c`, which is always
         *   positive here.
         * - **Testing only one diagonal**, for example `sr - sc == tr - tc`, and forgetting the anti-diagonal
         *   `sr + sc == tr + tc`. `|Δr| == |Δc|` covers both directions at once.
         * - **Using BFS when you don't need it.** It is correct, but it does a lot more work than this. Keep BFS
         *   for the variant with blocking pieces, where the "at most 2" bound no longer holds.
         * - **Forgetting that a move can cover several squares.** If you treat each move as a single step (like a
         *   king moving diagonally), you get a distance like `max(|Δr|, |Δc|)`, which is the wrong answer.
         */
        fun referenceSolution(source: IntArray, target: IntArray): Int {
            val (sr, sc) = source
            val (tr, tc) = target
            if ((sr + sc) % 2 != (tr + tc) % 2) return -1
            return if (abs(sr - tr) == abs(sc - tc)) 1 else 2
        }


    }
}
