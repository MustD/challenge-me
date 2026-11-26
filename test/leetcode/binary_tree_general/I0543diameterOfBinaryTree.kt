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
 * Given the `root` of a binary tree, return the length of the diameter of the tree.
 *
 * The diameter of a binary tree is the length of the longest path between any two nodes in a tree. This path may or
 * may not pass through the `root`.
 *
 * The length of a path between two nodes is represented by the number of edges between them.
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
            "[1]" expects 0,                                             // single node, n = 1
            "[1,2,null,3,4,5,null,null,6]" expects 4,                    // best path avoids the root
            "[1,2,null,3,null,4,null,5]" expects 4,                      // left-skewed chain
            "[1,null,2,null,3,null,4]" expects 3,                        // right-skewed chain
            "[-100,-100,-100]" expects 2,                                // negative / duplicate values
            "[4,-7,-3,null,null,-9,-3,9,-7,-4,null,6,null,-6,-6,null,null,0,6,5,null,9,null,null,-1,-4,null,null,null,-2]" expects 8, // deep lopsided subtree
        )

        @Test
        fun test() = check(
            ::diameterOfBinaryTree,
            ::diameterOfBinaryTreeQueue
        )

        /**
         * ## Analysis (lt-check) — verified: 2 official + 6 added cases
         *
         * **Time: O(n).** `dfs` is a post-order traversal that visits each node exactly once and does O(1) work per
         * visit (two `maxOf`s and an addition). The `null` children add at most n + 1 extra O(1) calls.
         *
         * **Space: O(h) auxiliary**, h = tree height: one stack frame per level of recursion, plus the single captured
         * `max`. Balanced tree → O(log n); skewed chain (allowed by the constraints) → O(n) = 10^4 frames. That depth
         * is fine on the JVM's default stack here, but it is the one real limit of the recursive form.
         *
         * Optimal: O(n) time / O(h) space — reached. Every node must be inspected (an unseen leaf could extend the
         * longest path), so nothing beats O(n) time.
         *
         * **Correctness.** `dfs(node)` returns the *height in nodes* of the subtree (0 for `null`). For a node, `l` and
         * `r` are then exactly the number of *edges* from that node down to the deepest leaf on each side **plus the
         * edge into that child** — so `l + r` is the edge-length of the longest path whose topmost node ("apex") is
         * `node`. Every path in a tree has exactly one apex, so taking the max of `l + r` over all nodes covers every
         * path, including ones that never touch the root (the `[1,2,null,3,4,5,null,null,6]` case). The nodes-vs-edges
         * conversion is the classic off-by-one here; returning node counts and summing them without `+ 1` gets it
         * right for free. A single node gives `0 + 0 = 0`, and `max` starting at 0 covers n = 1.
         *
         * **Pattern: "tree DP with a global answer"** (a.k.a. post-order return-one-thing / record-another). The
         * recursion *returns* what the parent can extend (a single downward branch: `1 + max(l, r)`) while it
         * *records* what can't be extended (the bent path `l + r`). Same template: 124 Binary Tree Maximum Path Sum
         * (weights instead of counts, clamp negatives at 0), 687 Longest Univalue Path, 2246 Longest Path With
         * Different Adjacent Characters (n-ary: keep the top two child heights), 1522 Diameter of N-Ary Tree.
         *
         * **Alternatives.**
         * - *Naive height-per-node*: for each node call a separate `height(left) + height(right)` — O(n²) on a skewed
         *   tree because heights are recomputed. Your version fuses height and answer into one pass.
         * - *Double BFS/DFS on the tree as an undirected graph*: BFS from any node to the farthest node `u`, then BFS
         *   from `u`; the farthest distance is the diameter. Also O(n), but needs parent links / an adjacency list
         *   (O(n) extra space) since `TreeNode` has no parent pointer. It is the go-to for general (unrooted) trees.
         * - *Iterative post-order* with an explicit stack and a `HashMap<TreeNode, Int>` of heights — same O(n) time,
         *   removes the call-stack-overflow risk on very deep trees at the cost of O(n) map space and more code.
         *
         * **Parallelism.** The two recursive calls are independent, so fork/join on the left and right subtrees is
         * possible (each returns height and its local best diameter instead of writing a shared `max`). For n ≤ 10^4
         * the task-spawn overhead dwarfs ~10^4 trivial operations; and on a skewed tree the critical path is the whole
         * chain (span = O(n)), so the speedup ceiling is ~1×. Only for huge, balanced trees does it pay off (span
         * O(log n)), and then you'd cut off spawning below some subtree size.
         *
         * **Real world.** Tree/graph diameter shows up as the "worst-case hop count" of a network spanning tree or a
         * DOM/component hierarchy, the critical-path length in a dependency tree, and phylogenetic tree analysis. The
         * same post-order "return one value, record another" shape is how compilers compute expression-tree register
         * needs (Sethi–Ullman) and how file-system tools aggregate directory sizes. At production scale trees can be
         * millions deep, which is why iterative traversals are preferred there.
         */
        fun diameterOfBinaryTree(root: TreeNode?): Int {
            var max = 0
            fun dfs(node: TreeNode? = root): Int {
                if (node == null) return 0
                val l = dfs(node.left)
                val r = dfs(node.right)
                max = maxOf(max, l + r)
                return 1 + maxOf(l, r)
            }
            dfs()
            return max
        }


        fun diameterOfBinaryTreeQueue(root: TreeNode?): Int {
            if (root == null) return 0

            // Phase 1: walk down. Parents end up before their children in `order`.
            val order = ArrayList<TreeNode>()
            val stack = ArrayDeque<TreeNode>().apply { addLast(root) }
            while (stack.isNotEmpty()) {
                val node = stack.removeLast()
                order += node
                node.right?.let { stack.addLast(it) }
                node.left?.let { stack.addLast(it) }
            }

            // Phase 2: bubble up. Reversed, so children are processed before their parent.
            val down = java.util.IdentityHashMap<TreeNode, Int>()  // node -> what dfs(node) would return
            var max = 0
            for (node in order.asReversed()) {
                val l: Int = if (node.left == null) 0 else down.getOrDefault(node.left, 0)
                val r: Int = if (node.right == null) 0 else down.getOrDefault(node.right, 0)
                max = maxOf(max, l + r)
                down[node] = 1 + maxOf(l, r)
            }
            return max
        }


    }
}
