package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 100. Same Tree  (https://leetcode.com/problems/same-tree/)
 *
 * Given the roots of two binary trees `p` and `q`, write a function to check if they are the same or not.
 *
 * Two binary trees are considered the same if they are structurally identical, and the nodes have the same value.
 *
 * Constraints:
 * - The number of nodes in both trees is in the range [0, 100].
 * - -10^4 <= Node.val <= 10^4
 */
typealias I0100 = (TreeNode?, TreeNode?) -> Boolean

class I0100isSameTree {

    @Nested
    inner class Solution : ProblemTest<I0100> {

        override val cases = testCases<I0100>(
            args("[1,2,3]", "[1,2,3]") expects true,
            args("[1,2]", "[1,null,2]") expects false,
            args("[1,2,1]", "[1,1,2]") expects false,
            args("[]", "[]") expects true,                                    // both empty
            args("[]", "[1]") expects false,                                  // one empty
            args("[1]", "[1]") expects true,                                  // single node, equal
            args("[1]", "[2]") expects false,                                 // single node, different value
            args("[-10000,10000]", "[-10000,10000]") expects true,           // value bounds, negatives
            args("[1,2,3,4,5,null,6]", "[1,2,3,4,5,null,6]") expects true,   // deeper equal tree
            args("[1,2,3,4,5,null,6]", "[1,2,3,4,5,null,7]") expects false,  // differs only at last leaf
            args("[1,2,3]", "[1,2,3,4]") expects false,                      // q has an extra deep node
            args("[2,2,2,null,2]", "[2,2,2,2]") expects false,               // all-equal values, mirrored shape
        )

        @Test
        fun test() = check(::isSameTree)

        /**
         * ## Analysis (lt-check) — verified: 3 official + 9 added cases
         *
         * **Time:** O(min(n, m)) — each loop iteration pops one aligned pair and does O(1) work; the walk stops at the
         * first mismatch, so the worst case (identical trees) visits every node once, plus one null pair per missing
         * child: O(n) with n ≤ 100.
         *
         * **Space:** O(h) auxiliary, h = tree height. `removeLast()` makes the deques *stacks*, so this is an iterative
         * pre-order **DFS**, not a BFS (the `Q` names suggest a queue). Each level leaves at most one pending sibling
         * on the stack, so the stack stays ≤ h + 1 deep — O(log n) for a balanced tree, O(n) for a skewed chain.
         * (A BFS with `removeFirst()` would also be correct but needs O(w), the widest level.)
         *
         * **Optimal:** O(n) time / O(h) space — reached. Every node must be inspected in the "equal" case, and any
         * traversal must remember the path back up (stack or recursion). Morris traversal could reach O(1) extra space
         * by temporarily rewiring pointers, but that mutates the inputs and is not worth it here.
         *
         * **Correctness:** the two stacks are walked in lockstep. Invariant: after every iteration both stacks have the
         * same size and position *i* in one holds the counterpart of position *i* in the other — because a pair is only
         * expanded when both nodes are non-null (a null/non-null pair fails the `!=` check first), and both sides then
         * push exactly two children in the same order. Pushing `null` children is what catches *shape* differences
         * (`[1,2]` vs `[1,null,2]`): a null is compared against a real node and fails. Consequence of the invariant:
         * the loop can only exit with both stacks empty, so the final `pQ.isEmpty() && qQ.isEmpty()` is always `true`
         * there — harmless, but a plain `return true` would say the same.
         *
         * **Pitfall — the `!=` works only because of this repo's `TreeNode`.** Here `TreeNode` is a `data class` with
         * just `` `val` `` in its primary constructor, so the generated `equals` compares values only (children are not
         * part of it) — exactly the node-local check you want. On LeetCode `TreeNode` is a plain class, so `!=` falls
         * back to *identity*: two distinct nodes are never equal and `[1,2,3]` vs `[1,2,3]` would return `false`. (The
         * earlier `!==` was identity in both places, which is why it failed.) The portable comparison is explicit:
         * `if (pN?.`val` != qN?.`val`) return false` — `null?.val` is `null`, so this still separates null from a node.
         *
         * **Pattern:** *simultaneous / lockstep traversal of two structures* — walk both with the same traversal order
         * and compare at each step. The same template solves 101 Symmetric Tree (pair left with right instead),
         * 572 Subtree of Another Tree (call this at every node), 951 Flip Equivalent Trees, and comparing two linked
         * lists.
         *
         * **Alternatives:**
         * - Recursive DFS: `p == null || q == null -> p === q`, else `p.val == q.val && same(p.left, q.left) &&
         *   same(p.right, q.right)`. Same O(n)/O(h), three lines, but uses the call stack (fine at n ≤ 100; with
         *   10^5-deep skewed trees it would overflow, which is where your explicit stack wins).
         * - One stack of pairs (`ArrayDeque<Pair<TreeNode?, TreeNode?>>`) instead of two parallel stacks — makes the
         *   alignment invariant structural instead of something you must reason about.
         * - Serialize both (with null markers) and compare strings — O(n) but always pays the full cost and O(n)
         *   memory, no early exit.
         *
         * **Parallelism:** not worthwhile. n ≤ 100 is far below thread/coroutine overhead. In principle the left and
         * right subtree comparisons are independent (fork/join with an AND reduce plus a cancel-on-first-false), but
         * for any realistic tree the work per node is a single int compare — memory latency dominates, not CPU.
         *
         * **Real world:** structural equality / diffing of trees — virtual-DOM reconciliation (React), comparing ASTs
         * in compilers and refactoring tools, and Merkle trees (git, ZFS, Cassandra anti-entropy), which store a hash
         * per subtree so equal subtrees are skipped in O(1) instead of re-walked — the production answer when the same
         * large trees are compared repeatedly.
         */
        fun isSameTree(p: TreeNode?, q: TreeNode?): Boolean {
            val pQ = ArrayDeque<TreeNode?>().apply { addLast(p) }
            val qQ = ArrayDeque<TreeNode?>().apply { addLast(q) }

            while (pQ.isNotEmpty() && qQ.isNotEmpty()) {
                val pN = pQ.removeLast()
                val qN = qQ.removeLast()
                if (pN != qN) return false
                pN?.let { pQ.addLast(it.left) }
                pN?.let { pQ.addLast(it.right) }
                qN?.let { qQ.addLast(it.left) }
                qN?.let { qQ.addLast(it.right) }
            }
            return pQ.isEmpty() && qQ.isEmpty()
        }

    }
}
