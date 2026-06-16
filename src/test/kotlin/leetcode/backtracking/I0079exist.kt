package leetcode.backtracking

import leetcode.args
import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 79. Word Search  (https://leetcode.com/problems/word-search/)
 *
 * Given an m x n grid of characters `board` and a string `word`, return true if `word` exists in
 * the grid. The word can be constructed from letters of sequentially adjacent cells, where adjacent
 * cells are horizontally or vertically neighboring. The same letter cell may not be used more than
 * once.
 *
 * Constraints:
 * - m == board.length, n == board[i].length
 * - 1 <= m, n <= 6
 * - 1 <= word.length <= 15
 * - board and word consist of only lowercase and uppercase English letters.
 */
typealias I0079 = (Array<CharArray>, String) -> Boolean

class I0079exist {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0079> {

        override val cases = leetcode.testCases<I0079>(
            args("""[["A","B","C","E"],["S","F","C","S"],["A","D","E","E"]]""", "ABCCED") expects true,
            args("""[["A","B","C","E"],["S","F","C","S"],["A","D","E","E"]]""", "SEE") expects true,
            args("""[["A","B","C","E"],["S","F","C","S"],["A","D","E","E"]]""", "ABCB") expects false,
            args("""[["a"]]""", "a") expects true,
            args("""[["a"]]""", "b") expects false,
            args(
                """[["A","A","A","A","A","A"],["A","A","A","A","A","A"],["A","A","A","A","A","A"],["A","A","A","A","A","A"],["A","A","A","A","A","A"],["A","A","A","A","A","A"]]""",
                "AAAAAAAAAAAAAAa"
            ) expects false
        )

        @Test
        fun test() = check(::exist, ::referenceSolution)

        /**
         * ## Pattern: DFS backtracking on a grid (a.k.a. flood-fill with undo)
         *
         * Classic "search for a path in a 2D grid" backtracking. Try every cell as a potential
         * start of `word`, then walk to the 4 orthogonal neighbours, matching one character per
         * depth level. The "no cell reused" rule is enforced by a visited set that grows as you
         * descend and shrinks as you return — that growth/shrink *is* the backtracking. This
         * template transfers directly to Number of Islands, Word Search II (with a Trie),
         * flood fill, and maze/path-existence problems.
         *
         * ## How this particular implementation works
         * - `backtrack` validates the *current* cell at the top (visited? in bounds? right
         *   letter?) rather than validating neighbours before recursing. This "check on entry"
         *   style is clean but means it recurses into out-of-bounds / wrong coordinates and
         *   rejects them one frame later — a constant-factor cost, not an asymptotic one.
         * - The visited set is an **immutable** `Set<Pair<Int,Int>>`; `used + crd` allocates a
         *   *new* set on every recursive call. That is the main efficiency wart (see below) but
         *   it also makes the undo automatic — there is nothing to "remove" on the way back up,
         *   because each frame holds its own copy.
         * - Outer loop only launches DFS from cells equal to `word[0]`, a small but real prune.
         *
         * ## Time complexity: O(m·n · 4^L)  (L = word.length)
         * Worst case there are m·n starting cells, and from each, the DFS branches into 4
         * neighbours at every level down to depth L → 4^L paths (really 3^L after the first
         * step since you never immediately revisit the parent, but Big-O keeps 4^L). The "A…A"
         * grid + long word case in the tests is exactly this pathological branching.
         * On top of that, **this** implementation multiplies in the set operations: `used + crd`
         * is O(current depth) ≈ O(L) work per node, and `used.contains` is O(1) average on a
         * HashSet, so the true bound is closer to O(m·n · 4^L · L). The L factor is the price of
         * copying the set instead of mutating it.
         *
         * ## Space complexity: O(L²) auxiliary (this implementation), O(L) optimal
         * Recursion depth is at most L, so the call stack is O(L). But each of the up-to-L live
         * frames holds its own immutable visited set of size up to L → O(L²) live memory from the
         * copies, plus per-call allocation churn for the GC. A mutable set / in-place board
         * marking (see alternative) brings this down to O(L). No separate output space — the
         * result is a single Boolean.
         *
         * ## Correctness notes
         * - Edge cases in the test suite all hold: single-cell board (`[["a"]]`), no-match, and
         *   the long repeated-letter grid that forces full exhaustion.
         * - The visited check is by coordinate (`Pair`), so the "same cell not reused" rule is
         *   enforced correctly even when many cells share a letter.
         * - `word` is guaranteed length >= 1 by constraints, so `word[0]` in the outer loop is
         *   safe; if the harness ever passed an empty word this would throw. Worth knowing as a
         *   latent assumption rather than a bug given the stated constraints.
         *
         * ## Alternative approaches & trade-offs
         * - **In-place board marking** (most common optimal form): temporarily overwrite the
         *   visited cell (e.g. set it to `'#'`), recurse, then restore it on the way out. No
         *   visited set at all → O(L) stack space, O(1) extra, and no per-node allocation. Same
         *   O(m·n·4^L) time but a much smaller constant. The trade-off is mutating the input
         *   (fine here since it's restored; not fine if the board must stay const or is shared
         *   across threads).
         * - **Mutable HashSet with add/remove** instead of the immutable copy: keeps your visited
         *   structure but drops the O(L) copy per node and the O(L²) memory — a minimal edit to
         *   this code that recovers most of the optimal characteristics.
         * - There is **no asymptotically better** general algorithm: word search on an arbitrary
         *   grid is essentially path search and the exponential branching is inherent. Pruning
         *   (start-letter filter, frequency check that the board even contains enough of each
         *   letter in `word`, searching from the rarer end of the word) only shrinks constants.
         *
         * ## Parallelism / multithreading
         * The outer "try every starting cell" loop is **embarrassingly parallel** — each start
         * cell launches an independent DFS with no shared mutable state (this immutable-set
         * design is, ironically, already thread-safe; in-place marking would need a per-thread
         * board copy). You could fan the up-to-m·n starts across a thread pool and short-circuit
         * on the first `true`. In practice it is **not worth it** at LeetCode scale: the grid is
         * capped at 6×6 = 36 starts and the whole search finishes in microseconds, so thread
         * spin-up and the cross-thread cancellation handshake dominate. Parallelism only starts
         * to pay on large grids (image-segmentation-sized boards), which is exactly the
         * real-world setting below.
         *
         * ## Real-world experience
         * This grid-DFS-with-backtracking pattern shows up in maze/route solvers, connected-
         * component labeling in image processing (flood fill / "magic wand" tool), circuit and
         * PCB trace routing, and game pathfinding. At real scale the differences from the
         * interview version dominate: boards are huge so you (a) use an explicit stack or BFS to
         * avoid blowing the call stack, (b) prefer in-place / bitset visited marking for cache
         * locality, and (c) often replace exact DFS with A / heuristic search or union-find when
         * you only need connectivity rather than a literal letter-by-letter path. Word Search II
         * (find many words at once) is the realistic version — there you build a Trie over the
         * word list and walk the grid once against the Trie, instead of re-running this DFS per
         * word.
         */
        fun exist(board: Array<CharArray>, word: String): Boolean {
            val mX = board.lastIndex
            val mY = board[0].lastIndex

            fun backtrack(idx: Int = 0, crd: Pair<Int, Int>, used: Set<Pair<Int, Int>> = emptySet()): Boolean {
                if (idx > word.lastIndex) return true

                val (x, y) = crd
                if (used.contains(crd)) return false
                if ((0..mX).contains(x).not()) return false
                if ((0..mY).contains(y).not()) return false
                if (board[x][y] != word[idx]) return false

                listOf(
                    x to y + 1,
                    x + 1 to y,
                    x to y - 1,
                    x - 1 to y
                ).forEach { nCord ->
                    if (backtrack(idx + 1, nCord, used + crd)) return true
                }
                return false
            }

            board.forEachIndexed { x, chars ->
                chars.forEachIndexed { y, letter ->
                    if (letter == word[0] && backtrack(0, x to y)) return true
                }
            }
            return false

        }

        /**
         * ## Why your solution times out (and the fix)
         *
         * The algorithm is correct and asymptotically right — the TLE is a *constant-factor*
         * problem from how the visited set is represented:
         *
         * 1. `used + crd` builds a **brand-new immutable `Set`** on every single recursive call.
         *    That is O(depth) ≈ O(L) work per node, turning the real running time into
         *    O(m·n · 4^L · L) instead of O(m·n · 4^L). In the worst-case "AAAA…" grid the node
         *    count is already enormous, so multiplying each node by an L-sized copy is what blows
         *    the budget.
         * 2. Every coordinate is a boxed `Pair<Int,Int>` (heap allocation + hashing). Millions of
         *    short-lived `Pair`s and `Set`s hammer the GC. Pure allocation churn, no algorithmic
         *    gain.
         *
         * The standard fix removes the visited set entirely by **marking the board in place**:
         * temporarily overwrite the current cell, recurse, then restore it on the way out. The
         * "mark / unmark" *is* the backtracking. This gives:
         * - **O(1) extra work per node** (no set copy) → time back down to O(m·n · 4^L).
         * - **O(L) space** (just the recursion stack) instead of O(L²) for the live set copies.
         * - **Zero per-node allocation** — no `Pair`s, no `Set`s.
         *
         * Other constant-factor habits worth keeping:
         * - **Validate the neighbour before recursing** rather than recursing into out-of-bounds
         *   coordinates and rejecting them one frame later — fewer wasted frames.
         * - The start-letter filter you already have (`letter == word[0]`) is a good prune; keep it.
         *
         * The input board is mutated during the search but fully restored before returning, so
         * the harness (which re-parses inputs per run) is unaffected. If the board had to stay
         * const, swap the in-place marks for a mutable `BooleanArray(m*n)` add/remove — same
         * complexity, no input mutation.
         */
        fun referenceSolution(board: Array<CharArray>, word: String): Boolean {
            val rows = board.size
            val cols = board[0].size

            fun dfs(x: Int, y: Int, idx: Int): Boolean {
                if (idx == word.length) return true
                if (x < 0 || x >= rows || y < 0 || y >= cols) return false
                if (board[x][y] != word[idx]) return false

                val saved = board[x][y]
                board[x][y] = '#' // mark visited

                val found = dfs(x + 1, y, idx + 1) ||
                        dfs(x - 1, y, idx + 1) ||
                        dfs(x, y + 1, idx + 1) ||
                        dfs(x, y - 1, idx + 1)

                board[x][y] = saved // restore (undo the mark)
                return found
            }

            for (x in 0 until rows) {
                for (y in 0 until cols) {
                    if (board[x][y] == word[0] && dfs(x, y, 0)) return true
                }
            }
            return false
        }

    }
}
