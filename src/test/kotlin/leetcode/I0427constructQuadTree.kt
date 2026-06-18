package leetcode

import leetcode.utils.QuadNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 427. Construct Quad Tree  (https://leetcode.com/problems/construct-quad-tree/)
 *
 * Given an n x n matrix `grid` of 0s and 1s, build a Quad-Tree representing it.
 *
 * A Quad-Tree node has two booleans, `isLeaf` and `val`, plus four child pointers
 * (topLeft, topRight, bottomLeft, bottomRight). Construction rule:
 * - If every cell in the current region is the same value, make a leaf node:
 *   `isLeaf = true`, `val` = that value, and all four children = null.
 * - Otherwise, set `isLeaf = false` (and `val` arbitrary; LeetCode prints it as true),
 *   split the region into its four equal quadrants, and recurse into each child.
 *
 * Serialization (used by the tests) is level-order: each present node is `[isLeaf,val]`
 * (rendered as 1/0) and absent child slots are `null`; trailing nulls are trimmed. A leaf
 * enqueues no children, so it never produces `null` placeholders.
 *
 * Constraints:
 * - n == grid.length == grid[i].length
 * - n == 2^x for some 0 <= x <= 6  (so n is a power of two, up to 64)
 * - grid[i][j] is 0 or 1
 *
 * Note: this problem uses a dedicated `QuadNode` type (see utils/QuadNode.kt) — the harness's
 * `Node` type is a graph node and is unrelated.
 */
typealias I0427 = (Array<IntArray>) -> QuadNode?

class I0427constructQuadTree {

    @Nested
    inner class Solution : ProblemTest<I0427> {

        override val cases = testCases<I0427>(
            // Example 1: 2x2 grid, all cells differ -> non-leaf root with 4 leaf children.
            "[[0,1],[1,0]]" expects "[[0,1],[1,0],[1,1],[1,1],[1,0]]",

            // Example 2 (the canonical LeetCode case): 8x8 grid -> root non-leaf; TL all-1 leaf,
            // BL all-1 leaf, BR all-1 leaf, and TR splits further into four 4x4-quadrant leaves.
            // Left half (cols 0-3) is all 1; right half is 0 in rows 0-1 and 1 from row 2 down.
            // (Single-line: the 2D parser splits rows on the literal "],[" separator, so
            // newlines/indentation between rows would break parsing.)
            "[[1,1,1,1,0,0,0,0],[1,1,1,1,0,0,0,0],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1]," +
                    "[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1]]" expects
                    "[[0,1],[1,1],[0,1],[1,1],[1,1],[1,0],[1,0],[1,1],[1,1]]",

            // Edge case: 1x1 grid -> single leaf.
            "[[0]]" expects "[[1,0]]",

            // Edge case: uniform grid collapses to one leaf regardless of size.
            "[[1,1],[1,1]]" expects "[[1,1]]",
        )

        @Test
        fun test() = check(::referenceSolution)

        fun construct(grid: Array<IntArray>): QuadNode? {

            fun toQuadNode(): QuadNode? {
                return null
            }

            TODO("implement")
        }

        /**
         * Pattern: divide-and-conquer recursion on a 2D region (the quad-tree analogue of building
         * a binary tree top-down). Each call owns a square sub-region of `grid` and answers one
         * question: "is this whole region uniform?" If yes, it's a leaf; if no, split into four
         * equal quadrants and recurse.
         *
         * Approach:
         *  1. Define a recursive helper over a region by its top-left corner (row, col) and its
         *     side length `size`. Work with corner + size rather than copying sub-grids — copying
         *     would add an O(n^2) cost per level and defeat the point.
         *  2. Uniformity check: scan every cell in the region against grid[row][col]. If all equal,
         *     return a leaf: QuadNode(val = that value as Bool, isLeaf = true).
         *  3. Otherwise return an internal node (isLeaf = false; `val` is arbitrary — LeetCode
         *     prints it as 1, so we set true) whose four children are recursive calls on the four
         *     half-size quadrants, in order TL, TR, BL, BR. The corner offsets are:
         *       TL (row, col), TR (row, col + half), BL (row + half, col), BR (row + half, col + half).
         *  4. The constraint n == 2^x guarantees `size` is always even when we split, so `half` is exact.
         *     n == 1 is the natural base case: a 1x1 region is trivially uniform -> leaf.
         *
         * Complexity:
         *  - Time: O(n^2 * log n) worst case. Each level of recursion scans, in total, every cell of
         *    the grid once for its uniformity checks (O(n^2) work across a level), and the recursion
         *    is at most log2(n) levels deep. (When large regions are uniform it short-circuits and is
         *    much faster.)
         *  - Space: O(log n) recursion depth plus O(number of nodes) for the output tree.
         *
         * Common pitfalls:
         *  - Building the uniformity test by recursing into children and *then* trying to merge four
         *    same-valued leaves back into one parent leaf is a valid alternative, but the direct scan
         *    is simpler and avoids creating-then-discarding nodes. If you do merge, the condition is:
         *    all four children are leaves AND share the same `val`.
         *  - Child order matters: the serializer enqueues TL, TR, BL, BR in that exact order, so
         *    swapping them produces a tree that looks correct structurally but fails equality.
         *  - For an internal node, `val` is irrelevant to correctness but the test compares via
         *    toString which renders it; LeetCode uses 1, so set `val = true` to match the examples.
         *  - Don't slice/copy sub-arrays per call — pass indices.
         */
        fun referenceSolution(grid: Array<IntArray>): QuadNode? {
            fun build(row: Int, col: Int, size: Int): QuadNode {
                val first = grid[row][col]
                var uniform = true
                outer@ for (r in row until row + size) {
                    for (c in col until col + size) {
                        if (grid[r][c] != first) {
                            uniform = false
                            break@outer
                        }
                    }
                }

                if (uniform) {
                    return QuadNode(`val` = first == 1, isLeaf = true)
                }

                val half = size / 2
                return QuadNode(`val` = true, isLeaf = false).apply {
                    topLeft = build(row, col, half)
                    topRight = build(row, col + half, half)
                    bottomLeft = build(row + half, col, half)
                    bottomRight = build(row + half, col + half, half)
                }
            }

            if (grid.isEmpty()) return null
            return build(0, 0, grid.size)
        }

    }
}
