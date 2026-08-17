package leetcode.graph_bfs

import leetcode.expects
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 404. Sum of Left Leaves  (https://leetcode.com/problems/sum-of-left-leaves/)
 *
 * Given the `root` of a binary tree, return the sum of all left leaves.
 * A leaf is a node with no children. A left leaf is a leaf that is the left
 * child of another node.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 1000].
 * - -1000 <= Node.val <= 1000
 */
typealias I0404 = (TreeNode?) -> Int

class I0404sumOfLeftLeaves {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0404> {

        override val cases = leetcode.testCases<I0404>(
            "[3,9,20,null,null,15,7]" expects 24,  // left leaves 9 and 15
            "[1]" expects 0,                       // single node is not a left leaf
            "[]" expects 0,                        // empty tree
            "[1,2,3,4,5]" expects 4,               // only leaf 4 is a left child (5 is a right leaf)
            "[1,2]" expects 2,                     // 2 is a left leaf
            "[1,null,2]" expects 0,                // 2 is a right leaf
        )

        @Test
        fun test() = check(::sumOfLeftLeaves)

        /**
         * ## Approach: iterative BFS carrying an `isLeft` flag
         *
         * Pattern: **level-order tree traversal (BFS) with per-node parent context.**
         * Whether a node is a *left* leaf is not a property of the node itself — it
         * depends on which side of its parent it hangs off of. The queue element is a
         * `Pair<TreeNode, Boolean>` so that context ("I was reached as a left child")
         * travels with the node. A node contributes when `isLeft && node.left == null
         * && node.right == null` (a leaf that is a left child).
         *
         * ### Complexity
         * - **Time: O(n)** — the `while` loop dequeues each node exactly once; every
         *   node is enqueued exactly once (each is some parent's `left`/`right`, plus
         *   the root). Per-node work is O(1).
         * - **Space: O(n)** auxiliary — the `ArrayDeque` holds at most one tree level
         *   at a time. For a balanced tree the widest level is ~n/2 nodes → O(n).
         *   (Output space is O(1): a single accumulator.)
         *
         * ### Correctness notes
         * - Root is seeded with `false`, so a single-node tree (`"[1]"`) correctly
         *   returns 0 — the root is never a left leaf. Handles the `"[1,2]"` vs
         *   `"[1,null,2]"` distinction purely via the flag.
         * - `root == null` guard covers the empty tree.
         * - The `isLeft` check precedes the enqueue, and the flag is set from the
         *   parent's perspective, so an internal left child (e.g. `2` in `"[1,2,3,4,5]"`)
         *   is not counted — only its leaf descendants that are themselves left children.
         * - No overflow risk: ≤1000 nodes, values in [-1000, 1000] → max |sum| ≤ 1e6,
         *   well within `Int`.
         *
         * ### Alternatives (same O(n) time — this is optimal, every leaf must be seen)
         * - **Recursive DFS** with an `isLeft` parameter: the canonical form. Space is
         *   O(h) stack (h = height) instead of O(n) queue — O(log n) for a balanced
         *   tree, but O(n) for a degenerate/skewed tree, so neither strictly dominates.
         * - **Iterative DFS** with an explicit stack: same shape as this BFS, just LIFO;
         *   same O(n)/O(n) bounds. The choice between BFS and DFS here is stylistic —
         *   there is no level-ordering requirement, so both are equally valid.
         *
         * ### Parallelism
         * Not worth it. n ≤ 1000 makes thread/overhead dwarf the work. In principle a
         * left-leaf sum is a parallel reduce over independent subtrees (divide at a
         * node, sum children concurrently, combine by `+` which is associative), so it
         * *could* be a fork/join like parallel tree-sum — but the tree is far too small
         * to overcome scheduling cost, and pointer-chasing has poor cache/SIMD behavior.
         *
         * ### Real-world
         * BFS-with-context is the everyday shape of tree/graph walks in production:
         * DOM traversal, file-system scans, dependency-graph resolution — the "who is
         * my parent / how did I get here" flag generalizes to carrying depth, path, or
         * visited-state. At real scale the differences that matter are stack-overflow
         * safety (iterative BFS/DFS beats deep recursion on adversarial/skewed input)
         * and memory pressure from the frontier, not Big-O.
         */
        fun sumOfLeftLeaves(root: TreeNode?): Int {
            if (root == null) return 0
            var result = 0

            val queue = ArrayDeque<Pair<TreeNode, Boolean>>()
            queue.addLast(root to false)
            while (queue.isNotEmpty()) {
                val (node, isLeft) = queue.removeFirst()
                if (isLeft && node.left == null && node.right == null) {
                    result += node.`val`
                }
                node.left?.let { queue.addLast(it to true) }
                node.right?.let { queue.addLast(it to false) }
            }
            return result
        }

    }
}
