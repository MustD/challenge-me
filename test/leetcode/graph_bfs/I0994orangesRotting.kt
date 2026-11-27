package leetcode.graph_bfs

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 994. Rotting Oranges  (https://leetcode.com/problems/rotting-oranges/)
 *
 * You are given an `m x n` grid where each cell can have one of three values:
 * - `0` representing an empty cell,
 * - `1` representing a fresh orange, or
 * - `2` representing a rotten orange.
 *
 * Every minute, any fresh orange that is 4-directionally adjacent to a rotten orange becomes rotten.
 *
 * Return the minimum number of minutes that must elapse until no cell has a fresh orange.
 * If this is impossible, return `-1`.
 *
 * Constraints:
 * - m == grid.length
 * - n == grid[i].length
 * - 1 <= m, n <= 10
 * - grid[i][j] is 0, 1, or 2.
 */
typealias I0994 = (Array<IntArray>) -> Int

class I0994orangesRotting {

    @Nested
    inner class Solution : ProblemTest<I0994> {

        override val cases = testCases<I0994>(
            "[[2,1,1],[1,1,0],[0,1,1]]" expects 4,
            "[[2,1,1],[0,1,1],[1,0,1]]" expects -1,
            "[[0,2]]" expects 0,
            "[[0]]" expects 0, // no oranges at all
            "[[1]]" expects -1, // single fresh orange, no rotten source
            "[[2]]" expects 0, // single rotten orange, nothing to spread to
            "[[2,0,2]]" expects 0, // only rotten oranges: BFS runs but no minute passes
            "[[2,1,1,1,2]]" expects 2, // multi-source: two fronts meet in the middle
            "[[2],[1],[1],[1]]" expects 3, // vertical chain (row directions)
            "[[1,1,2,1,1]]" expects 2, // source in the middle spreads both ways
            "[[2,1,0,1,1]]" expects -1, // fresh island cut off by an empty cell
            args(
                """
                [[2,1,1,1,1,1,1,1,1,1],
                 [1,1,1,1,1,1,1,1,1,1],
                 [1,1,1,1,1,1,1,1,1,1],
                 [1,1,1,1,1,1,1,1,1,1],
                 [1,1,1,1,1,1,1,1,1,1],
                 [1,1,1,1,1,1,1,1,1,1],
                 [1,1,1,1,1,1,1,1,1,1],
                 [1,1,1,1,1,1,1,1,1,1],
                 [1,1,1,1,1,1,1,1,1,1],
                 [1,1,1,1,1,1,1,1,1,1]]
            """
            ) expects 18, // 10x10 max grid, single corner source: farthest cell is 9+9 away
        )

        @Test
        fun test() = check(::orangesRotting)

        /**
         * ## Analysis (lt-check) — verified: 3 official + 9 added cases
         *
         * **Pattern: multi-source BFS, processed level by level.** All initially rotten oranges are seeded into the
         * queue at once (as if one virtual super-source rotted them all at minute 0), and each BFS layer is one
         * minute. BFS gives the *shortest* distance from the nearest source to every reachable cell, and the answer
         * is the largest such distance — the depth of the last layer that rotted something.
         *
         * **Time — O(m·n).** Let `N = m·n` (≤ 100). The scan is O(N). Each fresh cell is moved from `fresh` into
         * `rotten` at most once (it is removed from the set the moment it rots), so each cell is dequeued at most
         * once and does 4 O(1)-average hash lookups. The per-level `ArrayDeque(rotten)` copy is O(level size),
         * which sums to O(N) over all levels. Constant factors are high-ish: every neighbour check allocates a
         * `Pair` (boxed `Int`s) plus a 4-element `List` per dequeued cell, and hashes it — fine at N = 100.
         *
         * **Space — O(m·n)** auxiliary: the `fresh` hash set (up to N boxed pairs) plus the two deques (up to one
         * BFS frontier each, O(N) worst case). No recursion.
         *
         * Optimal: O(m·n) time / O(m·n) space — reached. Every cell must be read at least once, and a BFS frontier
         * can hold Θ(m·n) cells; an O(1)-extra-space variant exists only by re-scanning the grid every minute,
         * which costs O((m·n)²).
         *
         * **Correctness notes**
         * - Using `fresh` as the "visited" check means out-of-bounds neighbours (`-1`, `m`, `n`) need no explicit
         *   bounds test — they simply are not in the set. Neat, and it removes a classic off-by-one source.
         * - `didRot` handles the classic off-by-one of level-BFS: the last layer dequeues cells that rot nothing,
         *   so a naive `ticks++` per layer would answer one too many (and `[[2]]` would give 1 instead of 0).
         *   An equivalent idiom is `return maxOf(0, levels - 1)`.
         * - The `-1` check (`fresh.isNotEmpty()`) correctly covers both "no rotten source at all" (`[[1]]`) and
         *   "fresh island walled off by empty cells" (`[[2,1,0,1,1]]`).
         * - `grid[row][col] = 2` is never read back — `fresh` is the single source of truth — so it is a dead write
         *   that only mutates the caller's input. Harmless here, but worth noticing: either use the grid as the
         *   state or the set, not both.
         *
         * **Alternative approaches**
         * - *Grid as state + fresh counter* (the common reference): count fresh cells in the scan, BFS over an
         *   `ArrayDeque<IntArray>` / encoded `r * n + c` ints, rot by writing `2` into the grid and decrementing the
         *   counter, bounds-check explicitly. Same O(m·n) asymptotics, but no hashing or boxing, and the `-1` check
         *   becomes `fresh > 0`. Trade-off: mutates the input (or needs a copy) and needs bounds checks.
         * - *Per-level size loop* instead of copying the deque: `repeat(queue.size) { … }` on a single queue
         *   processes exactly one layer without allocating `tickRot`.
         * - *Repeated simulation*: each minute scan the whole grid and rot neighbours of rotten cells (with a marker
         *   to avoid chaining within a minute). O((m·n)·(m+n)) or worse — simple, but the BFS is what makes each
         *   cell cost O(1).
         *
         * **Parallelism.** Not worth it: N ≤ 100, and BFS is inherently layer-sequential (minute k+1 depends on
         * minute k). At scale, a *level-synchronous parallel BFS* splits each frontier across threads with an
         * atomic visited bitmap (the Graph500 approach), but the barrier per level and the frontier-size variance
         * cap the speedup — narrow frontiers (like the 1-wide chain cases) give essentially none (Amdahl).
         *
         * **Real world.** Multi-source BFS is the "distance to nearest X" primitive: distance transforms in image
         * processing, nearest-facility / flood-fill maps in game AI (Dijkstra maps), infection/propagation models,
         * and computing hop distance from a set of seed nodes in network or social graphs. There, grids are huge and
         * sparse, so the frontier is stored compactly (bitsets, int-encoded coordinates) rather than as boxed pairs.
         */
        fun orangesRotting(grid: Array<IntArray>): Int {
            val fresh = mutableSetOf<Pair<Int, Int>>()
            val rotten = ArrayDeque<Pair<Int, Int>>()
            grid.forEachIndexed { row, arr ->
                arr.forEachIndexed { col, value ->
                    when (value) {
                        1 -> fresh.add(row to col)
                        2 -> rotten.addLast(row to col)
                    }
                }
            }

            var ticks = 0

            while (rotten.isNotEmpty()) {
                val tickRot = ArrayDeque(rotten)
                rotten.clear()
                while (tickRot.isNotEmpty()) {
                    tickRot.removeFirst().let { (row, col) ->
                        listOf(
                            row - 1 to col,
                            row + 1 to col,
                            row to col + 1,
                            row to col - 1,
                        )
                    }.forEach { (row, col) ->
                        if (fresh.contains(row to col)) {
                            grid[row][col] = 2
                            rotten.addLast(row to col)
                            fresh.remove(row to col)
                        }
                    }
                }
                if (rotten.isNotEmpty()) ticks++ //if something rot add tick
            }

            return if (fresh.isNotEmpty()) -1 else ticks
        }

    }
}
