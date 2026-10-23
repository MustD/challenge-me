package leetcode.matrix

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4084. Last Visited Cell in a Directional Grid  (https://leetcode.com/problems/last-visited-cell-in-a-directional-grid/)
 *
 * You are given an m x n grid where each cell contains one of the letters 'L', 'R', 'U', or 'D',
 * representing the direction to move from that cell: left, right, up, or down.
 *
 * You start at cell (0, 0), which is considered visited. From each cell, you move to the adjacent cell
 * indicated by its letter. The process stops if the next move would go outside the grid or reach a cell
 * that has already been visited; that move is not made.
 *
 * Return the 0-based [row, column] of the last cell you visited.
 *
 * Constraints:
 * - 1 <= m, n <= 10^5
 * - m * n <= 10^5
 * - Each cell is 'L', 'R', 'U', or 'D'.
 */
typealias I4084 = (Array<CharArray>) -> IntArray

class I4084lastVisitedCell {

    @Nested
    inner class Solution : ProblemTest<I4084> {

        override val cases = testCases<I4084>(
            """[["R","D"],["U","L"]]""" expects "[1,0]",
            """[["R","R","R"]]""" expects "[0,2]",
            """[["U"]]""" expects "[0,0]",                                  // single cell, first move leaves grid
            """[["L","R"]]""" expects "[0,0]",                              // never moves: start cell is the answer
            """[["R","L"]]""" expects "[0,1]",                              // cycle back to the start cell (0,0)
            """[["D"],["D"],["D"]]""" expects "[2,0]",                      // single column, exits at the bottom edge
            """[["R","D"],["L","U"]]""" expects "[1,1]",                    // cycle that does not include the start
            """[["R","R","D"],["D","L","D"],["U","L","L"]]""" expects "[1,0]", // spiral; stops on a visited cell, (1,1) never reached
        )

        @Test
        fun test() = check(::lastVisitedCell)

        /**
         * ## Analysis (validated: 8/8 cases pass)
         *
         * **Pattern:** functional-graph walk / pointer chasing. Every cell has exactly one outgoing
         * edge (out-degree 1), so starting from (0,0) the path is a "rho" shape: a tail that either
         * falls off the grid or enters a cycle. The simulation stops at the first repeat or exit,
         * just like walking a linked list until `null` or a node you have already seen.
         *
         * **Time — O(m·n) worst case.** Each iteration of `while (true)` either visits a new cell or
         * breaks, so the loop runs at most m·n + 1 times (a snake covering the whole grid). Each step
         * does one `visited.add(next)` on a `HashSet<Pair<Int,Int>>`: O(1) expected, but it allocates
         * a `Pair` (two boxed `Int`s once outside the small-Integer cache) and hashes it. Best case
         * O(1) when the first move already exits or repeats.
         *
         * **Space — O(k) where k = cells visited, ≤ m·n.** The `visited` set holds one `Pair` per
         * visited cell plus HashSet node overhead: roughly 40–60 bytes per entry on the JVM, so ~5 MB
         * at the 10^5 ceiling. Output is O(1).
         *
         * **Correctness notes**
         * - The order of the two checks is subtle: `visited.add(next)` runs *before* the bounds test,
         *   so an out-of-bounds pair like (0,-1) gets inserted into the set. That is harmless — the
         *   loop breaks on the next line and never reads the set again — and it never indexes
         *   `grid` out of range because `grid[m][n]` is only read for the committed `coord`. But
         *   checking bounds first would read more naturally and avoid storing a bogus key.
         * - Revisiting is detected on the *target* cell, so `coord` stays as the last real visited
         *   cell when the loop breaks — exactly what the problem asks for (the move is "not made").
         * - Termination is guaranteed: the set grows by one per iteration and is bounded by m·n.
         * - `else ->` treats anything that isn't L/R/U as 'D'. Fine under the constraints, but a
         *   `'D' ->` branch plus `else -> error(...)` would make bad input fail loudly.
         * - Small typo: `mBoundry` / `nBoundry` → `Boundary`.
         *
         * **Alternatives**
         * - **Boolean grid instead of a hash set:** `val seen = Array(m) { BooleanArray(n) }` (or a
         *   flat `BooleanArray(m*n)` indexed `r*n + c`). Same O(m·n) time, but O(1) array lookups,
         *   no `Pair`/boxing allocations, ~10^5 bytes instead of megabytes — the idiomatic choice
         *   whenever the state space is a dense, bounded grid.
         * - **O(1) extra space by mutating the grid:** overwrite each visited cell with a sentinel
         *   (e.g. `'#'`) after reading its direction; hitting `'#'` means "visited". Destroys the
         *   input, which LeetCode tolerates but production code usually shouldn't.
         * - **Floyd's tortoise-and-hare** finds the cycle in O(1) space without mutation, but here it
         *   costs extra passes and complexity to recover the *last* cell before the repeat; the
         *   boolean grid is simpler and the asymptotics don't improve. Linear time is optimal — in
         *   the worst case you must follow every edge of the path.
         *
         * **Parallelism:** not useful. Each step depends on the previous cell's letter — a strict
         * sequential data dependency with no independent subproblems. (Pointer-jumping /
         * binary-lifting can parallelise list ranking on a PRAM, but for ≤10^5 steps the thread
         * overhead would dwarf a microsecond-scale scan.)
         *
         * **Real world:** the same "follow the single successor until you leave or loop" shape is
         * cycle detection in linked structures (symlink loops, redirect chains in HTTP clients,
         * `parent` pointers in a corrupted tree, Pollard's rho in factorisation). In practice you
         * usually also cap the hop count (like curl's `--max-redirs` or Linux's ELOOP after 40
         * symlinks) rather than tracking a full visited set, because the bound is cheap and good
         * enough.
         */
        fun lastVisitedCell(grid: Array<CharArray>): IntArray {
            val mBoundry = grid.indices
            val nBoundry = grid[0].indices

            var coord = 0 to 0
            val visited = mutableSetOf(coord)

            fun getNextMove(c: Char, current: Pair<Int, Int>): Pair<Int, Int> = when (c) {
                'L' -> current.first to current.second - 1
                'R' -> current.first to current.second + 1
                'U' -> current.first - 1 to current.second
                else -> current.first + 1 to current.second
            }

            while (true) {
                val (m, n) = coord
                val c = grid[m][n]
                val next = getNextMove(c, coord)

                if (visited.add(next).not()) break
                val (nextM, nextN) = next
                if ((nextM in mBoundry).not() || (nextN in nBoundry).not()) break
                coord = next
            }

            return intArrayOf(coord.first, coord.second)
        }

    }
}
