package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 543. Diameter of Binary Tree  (https://leetcode.com/problems/diameter-of-binary-tree/)
 *
 * Given the root of a binary tree, return the length of the diameter of the tree.
 *
 * The diameter of a binary tree is the length of the longest path between any two nodes in a tree.
 * This path may or may not pass through the root.
 *
 * The length of a path between two nodes is represented by the number of edges between them.
 *
 * Example 1:
 * Input: root = [1,2,3,4,5]
 * Output: 3
 * Explanation: 3 is the length of the path [4,2,1,3] or [5,2,1,3].
 *
 * Example 2:
 * Input: root = [1,2]
 * Output: 1
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 10^4].
 * - -100 <= Node.val <= 100
 */
typealias I0543 = (TreeNode?) -> Int

class I0543diameterOfBinaryTree {

    @Nested
    inner class Solution : ProblemTest<I0543> {

        override val cases = testCases<I0543>(
            "[1,2,3,4,5]" expects 3,
            "[1,2]" expects 1,
            "[1]" expects 0,
            // Longest path does NOT pass through the root: it lives in the left subtree (8-6-4-2-5-7-9 = 6 edges).
            "[1,2,null,4,5,6,null,null,7,8,null,null,9]" expects 6,
            // Degenerate "linked list" tree: diameter = n - 1.
            "[1,2,null,3,null,4,null,5]" expects 4,
        )

        @Test
        fun test() = check(::diameterOfBinaryTree, ::referenceSolution)

        fun diameterOfBinaryTree(root: TreeNode?): Int {
            var best = 0
            fun dfs(node: TreeNode? = root): Int {
                if (node == null) return 0

                val l = dfs(node.left)
                val r = dfs(node.right)
                best = maxOf(l + r, best)
                return maxOf(l, r) + 1

            }

            dfs()
            return best
        }

        /**
         * ## Reference solution — post-order DFS returning height, tracking the best "bend" globally
         *
         * **Restatement.** Among all pairs of nodes, find the longest path between them, counting *edges*
         * (not nodes). The path may bend at any node, not necessarily at the root.
         *
         * **Pattern: "tree DP" / post-order DFS with a global answer.** Each recursive call returns one value
         * to its parent (here: the subtree height), while a *different* value (the best path that bends at this
         * node) is folded into a shared max. It fits because any path in a tree has exactly one highest node
         * (its "apex"), and the longest path through a given apex is fully determined by information the
         * children already computed. Same template as 124 (Max Path Sum), 687 (Longest Univalue Path),
         * 110 (Balanced Binary Tree).
         *
         * ### Intuition
         * Every path has a single topmost node where it turns from going up to going down. If we fix that apex,
         * the longest path through it is `height(left) + height(right)` edges — go as deep as possible on both
         * sides. So the diameter is `max over all nodes of (leftHeight + rightHeight)`, and heights come out of
         * one post-order pass for free.
         *
         * ### Core concepts
         * - **Height (in edges / in nodes)** — the length of the longest downward path from a node to a leaf.
         *   Here it is convenient to measure height in *nodes* (`null` → 0, leaf → 1): then
         *   `leftHeight + rightHeight` is exactly the number of *edges* of the path bending at this node.
         * - **Apex of a path** — the unique highest node on a tree path. Enumerating apexes enumerates all
         *   candidate longest paths without double counting.
         * - **Post-order traversal** — process children before the parent, so the parent can combine results
         *   that are already known. Every "aggregate from subtrees" tree problem is post-order.
         * - **Return one thing, record another** — the value returned upward (a single branch, the height) is
         *   *not* the answer; the answer (a two-branch path) can't be extended by the parent, so it is recorded
         *   in an outer variable instead of returned.
         *
         * ### Approach
         * 1. `height(node)`: if `node == null` return 0.
         * 2. Recursively get `l = height(node.left)`, `r = height(node.right)`.
         * 3. Update `best = max(best, l + r)` — the path bending at `node`.
         * 4. Return `1 + max(l, r)` — the parent can extend only one branch.
         * 5. Call `height(root)` and return `best`.
         *
         * ### Complexity
         * - **Time O(n)** — each node is visited exactly once and does O(1) work.
         * - **Space O(h)** — recursion stack, `h` = tree height: O(log n) balanced, O(n) for a skewed tree.
         *
         * ### Common pitfalls
         * - **Assuming the path goes through the root.** `height(root.left) + height(root.right)` fails when the
         *   deepest bend is inside a subtree (see the test case where the root has only a left child).
         * - **Edges vs. nodes off-by-one.** The answer counts edges; a single node has diameter 0. Pick one height
         *   convention and check it on `[1]` and `[1,2]`.
         * - **Returning `l + r` upward.** The parent can only continue along one branch; returning both sides
         *   produces "paths" that fork, which aren't paths.
         * - **Recomputing height per node** (calling a separate `height()` from every node) gives O(n²) on a
         *   skewed tree. Compute height and the answer in the same pass.
         * - **Deep recursion.** With up to 10^4 nodes in a skewed tree the recursion is 10^4 deep — fine on the
         *   JVM default stack here, but an iterative post-order is the fix if limits grow.
         */
        fun referenceSolution(root: TreeNode?): Int {
            var best = 0

            fun height(node: TreeNode?): Int {
                if (node == null) return 0
                val l = height(node.left)
                val r = height(node.right)
                best = maxOf(best, l + r)
                return 1 + maxOf(l, r)
            }

            height(root)
            return best
        }

    }
}
