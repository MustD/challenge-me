package leetcode.binary_tree_general

import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 226. Invert Binary Tree  (https://leetcode.com/problems/invert-binary-tree/)
 *
 * Given the `root` of a binary tree, invert the tree, and return its root.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [0, 100].
 * - -100 <= Node.val <= 100
 */
typealias I0226 = (TreeNode?) -> TreeNode?

class I0226invertTree {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0226> {

        override val cases = testCases<I0226>(
            "[4,2,7,1,3,6,9]" expects "[4,7,2,9,6,3,1]",
            "[2,1,3]" expects "[2,3,1]",
            "[]" expects "[]",
            "[1]" expects "[1]", // single node — nothing to swap
            "[1,2]" expects "[1,null,2]", // only a left child — swap with null (depth 1)
            "[1,null,2]" expects "[1,2]", // only a right child
            "[1,2,null,3]" expects "[1,null,2,null,3]", // left-skewed chain -> right-skewed (swap needed below the root)
            "[1,2,3,4,null,null,5]" expects "[1,3,2,5,null,null,4]", // asymmetric: nulls must move with their subtree
            "[-100,100,-100]" expects "[-100,-100,100]", // boundary values, duplicates
            "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15]" expects "[1,3,2,7,6,5,4,15,14,13,12,11,10,9,8]", // perfect tree, 4 levels
            "[1,2,null,null,3]" expects "[1,null,2,3]", // zigzag: left then right -> right then left
            "[1,null,2,null,3,null,4,null,5]" expects "[1,2,null,3,null,4,null,5]", // right-skewed chain, depth 5 — queue never holds more than one node
            "[0,0,0,0]" expects "[0,0,0,null,null,null,0]", // all-equal values: only the shape can tell the result apart
        )

        @Test
        fun test() = check(::invertTree)

        /**
         * ## Review — iterative BFS, swap children at every node (verified: all cases pass)
         *
         * **Pattern:** tree traversal with a per-node O(1) action. The key observation is that
         * "invert" is a *local* operation — swapping `left`/`right` at one node is independent of
         * every other node — so *any* traversal order that visits each node exactly once is correct.
         * Here the order is level-order (BFS) driven by an explicit `ArrayDeque` used as a FIFO queue.
         *
         * **Time: O(n)** — each node is `add`ed once and `removeFirst`ed once (both amortized O(1)
         * on `kotlin.collections.ArrayDeque`, a ring buffer), and the swap is three pointer writes.
         * No best/worst split: every node must be touched, so Omega(n) is also the lower bound.
         *
         * **Space: O(w)** auxiliary, where `w` is the maximum tree width — the queue holds one
         * frontier at a time. Worst case is a perfect tree: the last level has about n/2 nodes, so
         * O(n). Best case is a skewed chain: the queue never holds more than one node, O(1).
         * No recursion stack, and no output space — the tree is mutated in place and the same
         * `root` reference is returned.
         *
         * **Correctness notes**
         * - Empty tree: `root?.let { queue.add(it) }` leaves the queue empty, the loop is skipped
         *   and `null` is returned. Typing the queue as `ArrayDeque<TreeNode>` (non-null) keeps
         *   nulls out of it entirely, so no null check is needed after `removeFirst()`.
         * - The children are enqueued *after* the swap (`node.left` is by then the old right).
         *   That is harmless precisely because order does not matter; enqueueing before the swap
         *   would be equally correct. Worth knowing why, rather than relying on it by accident.
         * - A null child is swapped like any other value, which is what moves a lone child to the
         *   other side (`[1,2]` -> `[1,null,2]`) — a classic bug is to skip the swap when one
         *   side is null.
         * - Termination/no double-visit holds because the input is a tree (each node has one
         *   parent). On a DAG with shared subtrees a node would be swapped twice, i.e. un-inverted.
         * - Style: `node.left.let { leftNode -> ... }` is just a temp variable in disguise. The
         *   idiomatic Kotlin swap is `node.left = node.right.also { node.right = node.left }`,
         *   or a plain `val tmp = node.left`.
         *
         * **Alternatives**
         * - *Recursive DFS* (the 3-line textbook answer): swap, then recurse into both children.
         *   O(n) time, O(h) stack — O(log n) on a balanced tree, O(n) on a skewed one. The exact
         *   mirror image of this solution's space profile: BFS is cheap on skewed trees and
         *   expensive on bushy ones, DFS the reverse. Recursion risks StackOverflowError on very
         *   deep trees (irrelevant at n <= 100, real on 10^5-node chains).
         * - *Iterative DFS*: change `removeFirst()` to `removeLast()` and this exact code becomes
         *   a stack-based DFS with O(h) space. One-word change, different memory behaviour.
         * - *Non-mutating*: build a new mirrored tree (`TreeNode(val).apply { left = inv(right) ... }`).
         *   O(n) extra space, but the input stays intact — preferable when the tree is shared.
         * - Asymptotically nothing beats O(n) time; O(1) extra space is possible only with Morris-style
         *   threading, which is not worth the complexity here.
         *
         * **Parallelism:** structurally it is embarrassingly parallel — the left and right subtrees
         * are disjoint, so they can be inverted by independent tasks (fork/join: fork one child,
         * compute the other, with a sequential cutoff below some subtree size). In practice it does
         * not pay off: the work per node is three pointer writes, so the job is bound by memory
         * latency (pointer chasing, cache misses), not CPU, and task overhead dwarfs the work until
         * the tree has millions of nodes. An unbalanced tree also splits the work unevenly. At
         * n <= 100 a single thread finishes in well under a microsecond.
         *
         * **Real world:** the same "visit every node, apply a local transform" shape is how
         * compilers rewrite ASTs (e.g. commuting operands), how UI toolkits mirror a layout tree
         * for right-to-left locales, and how image/scene graphs are flipped. Differences from the
         * interview version: production trees are usually immutable/persistent, so you return a new
         * tree (often sharing unchanged subtrees) instead of mutating; depth is unbounded, which is
         * why an explicit stack/queue like this one is preferred over recursion; and often you do not
         * invert at all — you keep a "mirrored" flag and swap the meaning of left/right at read time,
         * which is O(1).
         */
        fun invertTree(root: TreeNode?): TreeNode? {
            val queue = ArrayDeque<TreeNode>()
            root?.let { queue.add(it) }
            while (queue.isNotEmpty()) {
                val node = queue.removeFirst()
                node.left.let { leftNode ->
                    node.left = node.right
                    node.right = leftNode
                }
                node.left?.let { queue.add(it) }
                node.right?.let { queue.add(it) }
            }
            return root
        }

    }
}
