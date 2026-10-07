package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 94. Binary Tree Inorder Traversal  (https://leetcode.com/problems/binary-tree-inorder-traversal/)
 *
 * Given the `root` of a binary tree, return the inorder traversal of its nodes' values.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [0, 100].
 * - -100 <= Node.val <= 100
 *
 * Follow up: Recursive solution is trivial, could you do it iteratively?
 */
typealias I0094 = (TreeNode?) -> List<Int>

class I0094inorderTraversal {

    @Nested
    inner class Solution : ProblemTest<I0094> {

        override val cases = testCases<I0094>(
            "[1,null,2,3]" expects "[1,3,2]",
            "[1,2,3,4,5,null,8,null,null,6,7,9]" expects "[4,2,6,5,7,1,3,9,8]",
            "[]" expects "[]",
            "[1]" expects "[1]",
            "[1,2]" expects "[2,1]",                                  // only a left child
            "[1,null,2]" expects "[1,2]",                             // only a right child
            "[5,4,null,3,null,2,null,1]" expects "[1,2,3,4,5]",       // left-skewed chain: deepest node first
            "[1,2,null,null,3,4]" expects "[2,4,3,1]",                // zig-zag: left then right then left
            "[4,2,6,1,3,5,7]" expects "[1,2,3,4,5,6,7]",              // full BST -> sorted output
            "[-100,-100,100]" expects "[-100,-100,100]",              // duplicates + value bounds
            "[0,0,0]" expects "[0,0,0]",                              // all-equal zeros
            "[100,null,99,null,98,null,97,null,96,null,95,null,94,null,93,null,92,null,91,null,90,null,89,null,88,null,87,null,86,null,85,null,84,null,83,null,82,null,81,null,80,null,79,null,78,null,77,null,76,null,75,null,74,null,73,null,72,null,71,null,70,null,69,null,68,null,67,null,66,null,65,null,64,null,63,null,62,null,61,null,60,null,59,null,58,null,57,null,56,null,55,null,54,null,53,null,52,null,51,null,50,null,49,null,48,null,47,null,46,null,45,null,44,null,43,null,42,null,41,null,40,null,39,null,38,null,37,null,36,null,35,null,34,null,33,null,32,null,31,null,30,null,29,null,28,null,27,null,26,null,25,null,24,null,23,null,22,null,21,null,20,null,19,null,18,null,17,null,16,null,15,null,14,null,13,null,12,null,11,null,10,null,9,null,8,null,7,null,6,null,5,null,4,null,3,null,2,null,1]"
                    expects "[100,99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,64,63,62,61,60,59,58,57,56,55,54,53,52,51,50,49,48,47,46,45,44,43,42,41,40,39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1]",                                      // 100-node right-skewed chain (max n, max depth)
        )

        @Test
        fun test() = check(::inorderTraversal)

        /**
         * ## Analysis — recursive in-order via list concatenation
         *
         * **Pattern:** depth-first traversal, *in-order* variant (left → node → right). The base case
         * `root == null → emptyList()` makes the recursion total, so the empty tree and missing children
         * need no special handling. Same skeleton gives pre-/post-order by moving `root.val`.
         *
         * **Correctness:** follows the definition literally — every node is emitted exactly once, after its
         * whole left subtree and before its whole right subtree. On a BST this yields sorted output
         * (the `[4,2,6,1,3,5,7]` case).
         *
         * **Time — O(n·h), worst case O(n²), not O(n).** Each `+` builds a *new* list and copies the operands:
         * `inorderTraversal(left) + val` copies L elements, then `... + inorderTraversal(right)` copies L+1+R.
         * So a node costs ~2× its subtree size, and summing subtree sizes = summing node depths = O(n·h).
         * - balanced tree: h = log n → O(n log n)
         * - skewed chain (the 100-node case): h = n → ~n²/2 element copies (~10⁴ at n = 100 — harmless here,
         *   but the hidden cost of an innocent-looking `+`).
         *
         * **Space:**
         * - recursion stack O(h) → O(n) on a skewed tree (stack overflow risk only for ~10⁴+ depth; n ≤ 100 here).
         * - output O(n). Plus O(n·h) *total* allocation of short-lived intermediate lists (GC churn); at most
         *   O(n) of it is live at once.
         *
         * **Fix-in-place idea (same recursion, O(n)):** pass one `MutableList` accumulator down
         * (`fun dfs(node, out)` doing `dfs(left); out += val; dfs(right)`) — no copying, each node O(1).
         *
         * **Alternatives:**
         * - *Iterative with explicit stack* (the follow-up): push the left spine, pop → emit → go right.
         *   O(n) time, O(h) space; immune to call-stack limits. (Your earlier version in git history did this.)
         * - *Morris traversal*: thread each node's in-order predecessor's `right` back to it, walk without any
         *   stack, then unthread. O(n) time, **O(1)** extra space, but temporarily mutates the tree — unsafe
         *   for shared/concurrent readers.
         * - O(n) time is optimal: every node must be visited to be output.
         *
         * **Parallelism:** in principle left and right subtrees are independent (fork-join, then concatenate),
         * but n ≤ 100 and per-node work is one add — task overhead dwarfs the work. Skewed trees have no
         * parallelism at all (span = n). Not worth it; only meaningful for huge balanced trees with heavy
         * per-node work.
         *
         * **Real world:** in-order iteration is how sorted-map iterators work (`java.util.TreeMap`, B-tree range
         * scans in databases) — always iterative/lazy (an iterator holding the left-spine stack) so callers
         * can stop early and deep trees can't blow the stack. Functional languages hit the same `++` quadratic
         * trap and solve it with accumulators / difference lists, or a lazy `sequence { yieldAll(...) }`.
         */
        fun inorderTraversal(root: TreeNode?): List<Int> {
            if (root == null) return emptyList()
            return inorderTraversal(root.left) + root.`val` + inorderTraversal(root.right)
        }


    }
}
