package leetcode.matrix

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 463. Island Perimeter  (https://leetcode.com/problems/island-perimeter/)
 *
 * You are given `row x col` `grid` representing a map where `grid[i][j] = 1` represents land and
 * `grid[i][j] = 0` represents water.
 *
 * Grid cells are connected horizontally/vertically (not diagonally). The `grid` is completely surrounded by water,
 * and there is exactly one island (i.e., one or more connected land cells).
 *
 * The island doesn't have "lakes", meaning the water inside isn't connected to the water around the island.
 * One cell is a square with side length 1. The grid is rectangular, width and height don't exceed 100.
 * Determine the perimeter of the island.
 *
 * Constraints:
 * - row == grid.length
 * - col == grid[i].length
 * - 1 <= row, col <= 100
 * - grid[i][j] is 0 or 1.
 * - There is exactly one island in grid.
 */
typealias I0463 = (Array<IntArray>) -> Int

class I0463islandPerimeter {

    @Nested
    inner class Solution : ProblemTest<I0463> {

        override val cases = testCases<I0463>(
            "[[0,1,0,0],[1,1,1,0],[0,1,0,0],[1,1,0,0]]" expects 16,
            "[[1]]" expects 4,
            "[[1,0]]" expects 4,
            "[[0,0],[0,1]]" expects 4,                          // land only at last row/col (getOrNull bounds)
            "[[1,1],[1,1]]" expects 8,                          // all land: both up and left neighbours count
            "[[1,1,1,1]]" expects 10,                           // single row: only left-neighbour subtraction
            "[[1],[1],[1]]" expects 8,                          // single column: only up-neighbour subtraction
            "[[1,1,1],[1,0,1]]" expects 12,                     // U shape: open bay is water, not a lake
            "[[0,1,0],[1,1,1],[0,1,0]]" expects 12,             // cross: centre shares all four edges
            "[[1,1,1,1,1],[1,1,1,1,1],[1,1,1,1,1]]" expects 16, // full rectangle = 2*(rows+cols)
        )

        @Test
        fun test() = check(
            ::islandPerimeter,
            ::referenceSolution,
        )

        /**
         * ## Analysis (lt-check) — verified: 3 official + 7 added cases
         *
         * **Time:** O(R·C) — one pass over every cell, each doing O(1) work (two `getOrNull` lookups and a few
         * comparisons). Best = average = worst: there is no early exit, so all-water rows cost the same as land.
         *
         * **Space:** O(1) auxiliary — one `Int` accumulator; `forEachIndexed` is inline, so no lambda allocation.
         *
         * Optimal: O(R·C) time / O(1) space — reached. Every cell must be read at least once (any unread cell could
         * be land and change the answer), so nothing asymptotically faster exists.
         *
         * **Correctness.** Each land cell contributes 4 edges. Every edge shared by two land cells was counted twice
         * (once from each side) but is not on the perimeter, so it must be removed twice: `-2`. Looking only *up* and
         * *left* visits each shared edge exactly once (from the cell below / to the right of it), so there is no double
         * subtraction. Equivalent closed form: `perimeter = 4·land − 2·sharedEdges`. Grid borders are handled by
         * `getOrNull(...) ?: 0` treating out-of-bounds as water, which is exactly what "surrounded by water" means.
         * Note the "one island, no lakes" guarantees are not actually needed by this formula — it gives the total
         * boundary length of *any* land set (a lake's shoreline would simply be counted too); they only matter for the
         * DFS alternative's single-start assumption.
         *
         * Minor nits (no effect on correctness): `grid.getOrNull(rowI)` is just `row`, so `row.getOrNull(colI - 1)`
         * reads clearer; and `up`/`left` are computed for water cells too — moving them inside `if (cell == 1)` skips
         * that work.
         *
         * **Pattern:** grid counting with *half-neighbourhood dedup* — check only "already-visited" neighbours
         * (up/left in row-major order) so every pair is seen once. Same trick as counting adjacent equal pairs, building
         * a union-find over a grid by unioning only with up/left, or 2D prefix sums.
         *
         * **Alternatives.**
         * - Count all four neighbours per land cell: `+1` for each side that is water or out of bounds. Same O(R·C),
         *   arguably more obvious, ~2× the lookups.
         * - DFS/BFS from the first land cell, adding 1 whenever a step leaves the island or the grid. Same O(R·C) time
         *   but O(R·C) space (visited set + recursion depth up to R·C = 10⁴ on a snake-shaped island — a stack-overflow
         *   risk with naive recursion). It only wins if you must touch *one* island among many, or the island is tiny
         *   relative to the grid and you already know a land cell.
         *
         * **Parallelism.** Each row's contribution depends only on itself and the row above, so rows partition
         * trivially (parallel map + sum reduction). At ≤ 100×100 = 10⁴ cells the whole thing runs in microseconds —
         * thread start-up and the join dwarf the work; speedup would be < 1. Only on huge rasters (gigapixel masks)
         * would striping by row bands approach the core count, and then memory bandwidth, not compute, is the ceiling.
         *
         * **Real world.** Perimeter of a binary mask is a standard image-processing/GIS feature: region boundary length
         * in segmentation masks, shoreline length on raster maps, shape descriptors (compactness = 4π·area/perimeter²)
         * in computer vision. There the inputs are millions of pixels, often run-length encoded, and the same
         * `4·area − 2·shared` counting is done per run or with SIMD over bit-packed rows.
         */
        fun islandPerimeter(grid: Array<IntArray>): Int {
            var result = 0
            grid.forEachIndexed { rowI, row ->
                row.forEachIndexed { colI, cell ->
                    val up = grid.getOrNull(rowI - 1)?.getOrNull(colI) ?: 0
                    val left = grid.getOrNull(rowI)?.getOrNull(colI - 1) ?: 0
                    if (cell == 1) {
                        result += 4
                        if (up == 1) result -= 2
                        if (left == 1) result -= 2
                    }
                }
            }

            return result
        }

        /**
         * ## Reference (lt-ref) — grid edge counting (per-cell contribution)
         *
         * **Restatement.** A grid of 0/1 cells contains exactly one 4-connected island of 1s. Each cell is a unit
         * square. Return the total length of the island's boundary — the number of unit edges that separate a land
         * cell from water or from the outside of the grid.
         *
         * **Pattern.** *Local contribution counting* on a grid: the perimeter is a sum of independent per-edge facts
         * ("is this edge between land and non-land?"), so it can be computed by one linear scan without any graph
         * traversal. It fits because the answer decomposes over cells: no connectivity information is needed.
         *
         * ### Intuition
         * Ask every land cell, side by side: "is my neighbour on this side water (or off the grid)?" Each "yes" is one
         * unit of coastline. Every perimeter edge belongs to exactly one land cell, so summing these answers counts
         * each perimeter edge exactly once — no traversal, no visited set.
         *
         * ### Core concepts
         * - **Decomposition over local units** — a global quantity that is a sum of per-cell/per-edge terms can be
         *   computed in one pass; here each land cell contributes 0–4.
         * - **4-neighbourhood with direction vectors** — iterate `(dr, dc)` over up/down/left/right instead of four
         *   copied `if`s; the same loop shape appears in every grid BFS/DFS.
         * - **Out of bounds = background** — treating cells outside the grid as water removes all border special-cases;
         *   the statement guarantees the grid is surrounded by water, so this is exact.
         * - **Equivalent closed form** — `perimeter = 4·land − 2·sharedEdges`, because each land–land edge was "lost"
         *   by both of its cells. Useful as a cross-check.
         *
         * ### Approach
         * 1. `perimeter = 0`.
         * 2. For each cell `(r, c)` with `grid[r][c] == 1`:
         *    for each of the four directions, compute neighbour `(nr, nc)`; if it is outside the grid or
         *    `grid[nr][nc] == 0`, add 1.
         * 3. Return `perimeter`.
         *
         * ### Complexity
         * O(R·C) time / O(1) space
         * - Time: every cell is read once and each land cell inspects 4 neighbours — constant work per cell.
         * - Space: a counter and loop indices; the direction table is a fixed 4-element constant.
         *
         * ### Common pitfalls
         * - **Reaching for DFS/BFS by reflex.** It works, but costs O(R·C) extra space and risks a stack overflow on a
         *   long snake-shaped island (depth up to 10⁴) — and connectivity is irrelevant to the answer.
         * - **Forgetting the grid border.** A land cell on row 0 or the last column has an edge to the outside; an
         *   unchecked index throws, and skipping out-of-bounds neighbours under-counts.
         * - **Double-subtracting shared edges** in the `4·land − 2·shared` variant: if you check all four neighbours
         *   and subtract 2 for each land neighbour, every shared edge is subtracted twice per side (−4 total). Either
         *   check all four and subtract 1 each, or check only up/left and subtract 2.
         * - **Assuming square grids** — use `grid[0].size` for columns, not `grid.size`.
         */
        fun referenceSolution(grid: Array<IntArray>): Int {
            val rows = grid.size
            val cols = grid[0].size
            val directions = arrayOf(intArrayOf(-1, 0), intArrayOf(1, 0), intArrayOf(0, -1), intArrayOf(0, 1))

            var perimeter = 0
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (grid[r][c] == 0) continue
                    for ((dr, dc) in directions) {
                        val nr = r + dr
                        val nc = c + dc
                        val isWaterOrOutside = nr !in 0 until rows || nc !in 0 until cols || grid[nr][nc] == 0
                        if (isWaterOrOutside) perimeter++
                    }
                }
            }
            return perimeter
        }

    }
}
