package leetcode.binary_tree_general

import leetcode.expects
import leetcode.testCases
import leetcode.utils.NaryNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 589. N-ary Tree Preorder Traversal  (https://leetcode.com/problems/n-ary-tree-preorder-traversal/)
 *
 * NOTE: statement text, examples and constraints below were written from recall and are NOT verified
 * against leetcode.com.
 *
 * Given the `root` of an n-ary tree, return the preorder traversal of its nodes' values.
 *
 * Nary-Tree input serialization is represented in their level order traversal. Each group of children
 * is separated by the null value (See examples)
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [0, 10^4].
 * - 0 <= Node.val <= 10^4
 * - The height of the n-ary tree is less than or equal to 1000.
 *
 * Follow up: Recursive solution is trivial, could you do it iteratively?
 */
typealias I0589 = (NaryNode?) -> List<Int>

class I0589preorder {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0589> {

        override val cases = testCases<I0589>(
            "[1,null,3,2,4,null,5,6]" expects "[1,3,5,6,2,4]",
            "[1,null,2,3,4,5,null,null,6,7,null,8,null,9,10,null,null,11,null,12,null,13,null,null,14]" expects "[1,2,3,6,7,11,14,4,8,12,5,9,13,10]",
            "[]" expects "[]", // empty tree (root == null)
            "[7]" expects "[7]", // single node, no children
            "[1,null,2,null,3,null,4]" expects "[1,2,3,4]", // skewed chain: every node has one child
            "[1,null,2,3,4,5]" expects "[1,2,3,4,5]", // star: root with only leaf children
            "[1,null,2,3,null,null,4,5,null,6]" expects "[1,2,3,4,6,5]", // leaf first child, deep last child; preorder != level order
            "[0,null,0,10000,0,null,10000,null,null,0]" expects "[0,0,10000,10000,0,0]", // duplicates, min (0) and max (10^4) values
        )

        @Test
        fun test() = check(::preorder, ::referenceSolution)

        // Attempt analysis (leetcode-help)
        // Approach: recursive pre-order DFS with a shared accumulator - "add the node, then recurse into each
        // child". It is correct and already optimal: O(n) time, O(h) call stack, same asymptotics as the
        // reference below. There is no defect to fix.
        // What it does not do is answer the statement's follow-up (iterative). The only real difference from
        // the reference is where the pending work lives: here on the JVM call stack (bounded by thread stack
        // size, fine for h <= 1000), there in a heap-allocated deque (no depth limit, but up to O(n) entries).
        // Optional tidy-up, not a bug: `node` can only be null for the root, so one `if (node == null) return`
        // (or a non-null `dfs` parameter plus a root check) replaces the two `node?.` safe calls.
        /**
         * ## Review (leetcode-validate) — verified: passes the 2 official examples + 6 added edge cases
         *
         * ### Pattern
         * Recursive **pre-order DFS with an accumulator**: a local `dfs` closes over one shared `result` list,
         * records the node on the way *down* (before any child), then recurses into the children left to right.
         * "Visit, then recurse" = pre-order; moving the `add` after the `forEach` gives post-order (LC 590) with
         * no other change. That one-line difference is the thing to remember.
         *
         * Note on the `root->left->right` comment above: that is the binary-tree wording. For an n-ary tree the
         * rule is "node, then each child subtree in order" — there is no left/right, and no in-order at all
         * (in-order needs exactly two sides to sit between).
         *
         * ### Time — O(n)
         * `dfs` is entered exactly once per node (plus once for a null root). Each call does one amortized-O(1)
         * `result.add` and one `forEach` over its own `children`. Summed over the tree, the `forEach` bodies run
         * once per edge = n - 1 times. Total O(n + (n - 1)) = O(n). No best/worst split: every node must be output.
         *
         * ### Space — O(h) auxiliary, O(n) output
         * - Recursion stack: one `dfs` frame per level, so O(h). The statement caps h at 1000, which is safe on
         *   the default JVM stack. Without that cap a degenerate chain (the `[1,null,2,null,3,null,4]` shape)
         *   makes h = n and this would be the first thing to break (`StackOverflowError`).
         * - `result`: O(n), but that is the required output, not working space. `mutableListOf` is an `ArrayList`
         *   that grows by 1.5x, so it does a few re-copies on the way to n — still amortized O(1) per add.
         *
         * ### Correctness notes
         * - **Empty tree** is handled by the two safe calls: with `root == null` both `node?.let` and
         *   `node?.children?.forEach` are skipped and the empty list is returned. No explicit guard needed.
         * - `children` holds non-null `NaryNode`s, so the nullable `node` parameter only ever is null for the
         *   root. The null test is therefore paid twice per node (`node?.let`, then `node?.children`) for a
         *   condition that can only be true once. A single `if (node == null) return` on entry — or checking the
         *   root once outside and making `dfs` take a non-null `NaryNode` — states that intent more directly.
         * - The default argument `node: NaryNode? = root` works, but it hides which node the traversal starts
         *   from at the call site (`dfs()`); `dfs(root)` reads the same and costs nothing.
         * - Leaves need no special case: `forEach` on an empty list is a no-op, which is the recursion's base.
         * - Values are only copied, never compared or summed, so duplicates and the 0 / 10^4 bounds cannot matter.
         *
         * ### Alternatives
         * - **Iterative with an explicit stack (the follow-up).** Push the root; loop: pop, add its value, then
         *   push its children **in reverse** so the first child is on top and gets popped next. Same O(n) time.
         *   Stack space is no longer O(h): it holds the pending siblings of every ancestor, so it is bounded by
         *   O(n) (think of the star shape, where all children sit on the stack at once). The gain is not
         *   asymptotic — it moves the memory from the thread stack to the heap, so depth can no longer overflow.
         *   Forgetting the reversal is the classic bug: it yields root-then-children-right-to-left.
         * - **Stack of iterators.** Keep a stack of `Iterator<NaryNode>`, one per open ancestor; advance the top
         *   one, pop when exhausted. This restores true O(h) space iteratively and is what a lazy
         *   `Sequence`/`Iterator` tree walker looks like.
         * - **Morris traversal does not carry over.** It needs a spare null child pointer to thread through;
         *   an n-ary `children` list has none, so O(1)-space traversal is not available here.
         * - O(n) time is optimal: the output itself has n elements.
         *
         * ### Parallelism — not worth it
         * Sibling subtrees are independent, so they *can* be traversed concurrently, but the output is a single
         * ordered list: each subtree's results must land at an offset equal to the size of everything before it.
         * That needs either a subtree-size pre-pass (itself a full O(n) traversal) or per-task lists that are
         * concatenated afterwards (extra O(n) copying per level of nesting). The work per node is one pointer
         * chase and one append, so task scheduling would dominate; at n <= 10^4 the sequential version finishes
         * in microseconds. It also degenerates on a chain, where there is nothing to split. Pointer-chasing
         * trees are memory-latency bound, not CPU bound, which is a second reason threads help little.
         *
         * ### Real-world
         * Pre-order is the "parent before its contents" order: listing a directory tree (`Files.walk`,
         * `File.walkTopDown()`), printing an outline or table of contents, serializing a DOM/JSON/XML tree,
         * copying a tree (a parent must exist before its children are attached), and AST visitors' `enter`
         * hooks. Production differences from the interview version: depth is not capped, so library walkers are
         * iterative or lazy (iterator-stack form) rather than recursive; results are usually streamed to a
         * visitor/callback instead of materialized into a list; and real hierarchies can contain cycles
         * (symlinks) that need a visited set, which a true tree never does.
         */
        fun preorder(root: NaryNode?): List<Int> {
            val result = mutableListOf<Int>()
            fun dfs(node: NaryNode? = root) {
                node?.let { result.add(it.`val`) }
                node?.children?.forEach { dfs(it) }
            }
            dfs()
            return result
        }

        /**
         * ## Reference (leetcode-help) — iterative pre-order with an explicit stack
         *
         * **Restatement.** Every node has a value and an ordered list of any number of children. Output all
         * values so that each node appears before everything in its subtrees, and the subtrees appear in
         * child order: `node, subtree(child 0), subtree(child 1), ...`.
         *
         * **Pattern: depth-first traversal, pre-order.** DFS fits because the required order is defined
         * recursively per subtree ("finish this whole subtree before touching the next sibling"), which is
         * exactly the last-in-first-out discipline of a stack. The recursive form uses the call stack; this
         * one makes the stack explicit, which is what the follow-up asks for.
         *
         * ### Intuition
         * Recursion is a stack you do not see. Each pending call is a "to-do: traverse this subtree" note.
         * Keep those notes yourself in a deque: pop a node, output it immediately (pre-order = on the way
         * down), then leave notes for its children. The one trap is that a stack reverses whatever you push,
         * so to *pop* children first-to-last you must *push* them last-to-first.
         *
         * ### Core concepts
         * - **Pre-order** — a node is emitted before its descendants. Matters here because it means a node
         *   can be output the moment it is popped; nothing needs to be remembered about it afterwards.
         * - **Explicit stack (LIFO work list)** — a heap-allocated replacement for the call stack. It removes
         *   the recursion-depth limit and is the standard way to turn any DFS iterative.
         * - **Push in reverse** — LIFO flips insertion order, so pushing children right-to-left makes them
         *   come out left-to-right. The same trick applies to any ordered DFS over a stack.
         * - **Height `h` vs. size `n`** — recursion depth is bounded by `h`; an explicit node stack is bounded
         *   by the number of *pending siblings*, which can reach `n`. Knowing which bound applies tells you
         *   what input shape is the worst case (chain vs. star).
         *
         * ### Approach
         * 1. Null root -> return an empty list.
         * 2. Push the root onto a stack.
         * 3. While the stack is not empty: pop a node, append its value, push its children from last to first.
         * 4. Return the list.
         *
         * ### Complexity
         * - **Time O(n)** — each node is pushed once and popped once; each edge is walked once when its child
         *   is pushed.
         * - **Space O(n)** worst case for the stack (a root with n - 1 leaf children puts them all on the
         *   stack at once), O(h + widest sibling group) in general; plus the O(n) output list.
         *
         * ### Common pitfalls
         * - Pushing children in natural order: yields `node, children right-to-left` — a mirrored pre-order.
         * - Using a queue (`removeFirst`) instead of a stack: that silently becomes level-order (BFS).
         * - Forgetting the empty tree: pushing a null root and then reading `.val` fails.
         * - Assuming the iterative version saves memory: it trades thread-stack depth for heap, and for wide
         *   trees holds *more* entries than the recursion has frames.
         * - Reusing binary-tree vocabulary: there is no left/right and no in-order for an n-ary tree.
         */
        fun referenceSolution(root: NaryNode?): List<Int> {
            val result = mutableListOf<Int>()
            if (root == null) return result
            val stack = ArrayDeque<NaryNode>()
            stack.addLast(root)
            while (stack.isNotEmpty()) {
                val node = stack.removeLast()
                result.add(node.`val`)
                // reversed: the first child must end up on top so it is popped next
                for (i in node.children.indices.reversed()) stack.addLast(node.children[i])
            }
            return result
        }

    }
}
