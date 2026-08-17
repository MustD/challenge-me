package leetcode.binary_search_tree

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 938. Range Sum of BST  (https://leetcode.com/problems/range-sum-of-bst/)
 *
 * Given the `root` node of a binary search tree and two integers `low` and `high`, return the sum of the values of all
 * nodes whose value lies in the inclusive range `[low, high]`.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range `[1, 2 * 10^4]` — the tree is never empty.
 * - `1 <= Node.val <= 10^5`
 * - `1 <= low <= high <= 10^5`
 * - All `Node.val` are unique (which is what keeps the worst-case sum inside `Int` range).
 */
typealias I0938 = (TreeNode?, Int, Int) -> Int

class I0938rangeSumBST {

    @Nested
    inner class Solution : ProblemTest<I0938> {

        override val cases = testCases<I0938>(
            // Official examples
            args("[10,5,15,3,7,null,18]", 7, 15) expects 32,
            args("[10,5,15,3,7,13,18,1,null,6]", 6, 10) expects 23,
            // Edge cases implied by the constraints
            args("[10]", 5, 15) expects 10,              // single node, inside the range
            args("[10]", 11, 20) expects 0,              // single node, outside the range -> empty sum
            args("[10,5,15,3,7,null,18]", 1, 100) expects 58,   // range covers every node
            args("[10,5,15,3,7,null,18]", 18, 18) expects 18,   // low == high, matches a leaf
            args("[10,5,15,3,7,null,18]", 8, 9) expects 0,      // low == high-1, matches nothing
        )

        @Test
        fun test() = check(::rangeSumBST, ::referenceSolution, ::referenceSolutionIterative)

        fun rangeSumBST(root: TreeNode?, low: Int, high: Int): Int {
            if (root == null) return 0
            var result = 0

            fun dfs(node: TreeNode = root) {
                if (node.`val` in low..high) result += node.`val`
                node.left?.let { if (node.`val` >= low) dfs(it) }
                node.right?.let { if (node.`val` <= high) dfs(it) }
            }
            dfs()
            return result
        }

        /**
         * **Restatement.** Walk a binary *search* tree and add up every value inside `[low, high]`.
         *
         * **Pattern — DFS with BST pruning ("search-tree short-circuit").** The naive answer is: visit all nodes, keep
         * the ones in range. That is `O(n)` and it *works*, but it throws away the only interesting property of the
         * input — the BST ordering. The pattern to internalize is: *in a BST, a comparison at a node tells you that an
         * entire subtree is irrelevant*.
         *
         * The three-way decision at each node is the whole solution:
         * - `node.val < low`  → every value in the **left** subtree is even smaller, so the left subtree cannot
         *   contribute anything. Recurse **right only**.
         * - `node.val > high` → every value in the **right** subtree is even larger. Recurse **left only**.
         * - otherwise the node is in range → add it and recurse **both** ways (the left subtree may still hold values
         *   `>= low`, the right subtree values `<= high`).
         *
         * Note that this is the same shape as binary search: compare, then discard a half. Here we discard *subtrees*
         * instead of array halves, and because the target is a *range* rather than a single key we sometimes have to
         * keep both halves. Geometrically the visited nodes form two search paths (one hunting for `low`, one for
         * `high`) plus everything fully enclosed between them.
         *
         * **Complexity.**
         * - Time: `O(h + k)` where `h` is the tree height and `k` is the number of in-range nodes — the two boundary
         *   search paths cost `O(h)` each, and every other visited node is a node we actually sum. Worst case (range
         *   covers the whole tree, or the tree is a degenerate chain) that degrades to `O(n)`, which is unavoidable
         *   since we must at least touch what we sum.
         * - Space: `O(h)` recursion stack — `O(log n)` if balanced, `O(n)` for a chain. With `n <= 2 * 10^4` a
         *   degenerate chain is deep enough to be worth knowing about, though the JVM default stack survives it; the
         *   iterative variant below removes the risk entirely.
         *
         * **Pitfalls.**
         * - Forgetting the pruning and writing a plain full traversal: correct, but it misses the point of the problem
         *   (this is the "easy" warm-up for LC 700 / 230 / 98, all of which are about exploiting BST order).
         * - Using `<=`/`>=` in the pruning comparisons. `node.val == low` must **not** prune the left subtree away —
         *   it prunes nothing, it just means the node itself is included. Prune only on strict `<low` / `>high`.
         * - Treating the range as exclusive: `[low, high]` is inclusive on **both** ends, hence `in low..high`.
         * - `null` handling: the constraints promise a non-empty tree, but the recursion still bottoms out at `null`
         *   children, so the `null -> 0` base case is what makes the empty sum work (see the `args("[10]", 11, 20)`
         *   case, which must return `0`, not the node value).
         * - Overflow is a non-issue *here* only because values are unique and bounded: at most `10^5` distinct values
         *   `<= 10^5`, so the sum stays well inside `Int`. Do not generalize that habit — with duplicates allowed this
         *   would need `Long`.
         */
        fun referenceSolution(root: TreeNode?, low: Int, high: Int): Int {
            if (root == null) return 0
            return when {
                root.`val` < low -> referenceSolution(root.right, low, high)
                root.`val` > high -> referenceSolution(root.left, low, high)
                else -> root.`val` +
                        referenceSolution(root.left, low, high) +
                        referenceSolution(root.right, low, high)
            }
        }

        /**
         * Same pruning logic, explicit stack instead of recursion — `O(1)` JVM stack usage, so it is immune to a
         * degenerate (linked-list shaped) BST. Worth writing once: every "DFS with pruning" recursion converts to this
         * shape mechanically — push the children you did *not* prune, pop until empty.
         */
        fun referenceSolutionIterative(root: TreeNode?, low: Int, high: Int): Int {
            var sum = 0
            val stack = ArrayDeque<TreeNode>()
            root?.let { stack.addLast(it) }
            while (stack.isNotEmpty()) {
                val node = stack.removeLast()
                when {
                    node.`val` < low -> node.right?.let { stack.addLast(it) }
                    node.`val` > high -> node.left?.let { stack.addLast(it) }
                    else -> {
                        sum += node.`val`
                        node.left?.let { stack.addLast(it) }
                        node.right?.let { stack.addLast(it) }
                    }
                }
            }
            return sum
        }

    }
}
