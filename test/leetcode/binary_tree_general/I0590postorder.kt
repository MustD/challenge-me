package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.NaryNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 590. N-ary Tree Postorder Traversal  (https://leetcode.com/problems/n-ary-tree-postorder-traversal/)
 *
 * Given the `root` of an n-ary tree, return the postorder traversal of its nodes' values.
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
typealias I0590 = (NaryNode?) -> List<Int>

class I0590postorder {

    @Nested
    inner class Solution : ProblemTest<I0590> {

        override val cases = testCases<I0590>(
            "[1,null,3,2,4,null,5,6]" expects "[5,6,3,2,4,1]",
            "[1,null,2,3,4,5,null,null,6,7,null,8,null,9,10,null,null,11,null,12,null,13,null,null,14]" expects "[2,6,14,11,7,3,12,8,4,13,9,10,5,1]",
            "[]" expects "[]", // empty tree (root == null)
            "[7]" expects "[7]", // single node, no children
            "[1,null,2,null,3,null,4]" expects "[4,3,2,1]", // skewed chain: postorder is the exact reverse of the input
            "[1,null,2,3,4,5]" expects "[2,3,4,5,1]", // star: leaves in order, root last
            "[1,null,2,3,null,null,4,5,null,6]" expects "[2,6,4,5,3,1]", // leaf first child, deep last child; not a reversed preorder
            "[0,null,0,10000,0,null,10000,null,null,0]" expects "[10000,0,10000,0,0,0]", // duplicates, min (0) and max (10^4) values
        )

        @Test
        fun test() = check(::postorder, ::referenceSolution)

        // Attempt analysis (leetcode-help)
        // Approach: recursive post-order DFS with a shared accumulator - "recurse into each child, then add
        // the node". It is correct and already optimal: O(n) time, O(h) call stack, the same asymptotics as
        // the reference below. There is no defect to fix.
        // What it does not do is answer the statement's follow-up (iterative). The reference keeps the same
        // O(h) bound but moves the frames from the JVM call stack to a heap deque, so depth stops being a limit.
        // Optional tidy-up, not a bug: `node` can only be null for the root, so one `if (node == null) return`
        // (or a non-null `dfs` parameter plus a root check) replaces the two `node?.` safe calls.
        /**
         * ## Review (leetcode-validate) — verified: passes the 2 official examples + 6 added edge cases
         *
         * ### Pattern
         * Recursive **post-order DFS with an accumulator**: a local `dfs` closes over one shared `result` list,
         * recurses into every child subtree left to right, and records the node on the way *back up*.
         * "Recurse, then visit" = post-order. It is the same function as LC 589 with the two lines swapped —
         * the position of the `add` relative to the recursive calls is the whole difference between the orders.
         * Post-order is the order to reach for whenever a node's answer depends on its children's answers
         * (subtree size, height, diameter — LC 543 — deleting a tree, evaluating an expression tree).
         *
         * ### Time — O(n)
         * `dfs` is entered exactly once per node (plus once for a null root). Each call runs one `forEach` over
         * its own `children` and one amortized-O(1) `result.add`. Summed over the tree, the `forEach` bodies run
         * once per edge = n - 1 times. Total O(n + (n - 1)) = O(n). No best/worst split: every node must be output.
         *
         * ### Space — O(h) auxiliary, O(n) output
         * - Recursion stack: one `dfs` frame per level, so O(h). The statement caps h at 1000, which is safe on
         *   the default JVM stack. Without that cap a degenerate chain (the `[1,null,2,null,3,null,4]` shape)
         *   makes h = n and the recursion is the first thing to break (`StackOverflowError`).
         * - `result`: O(n), but that is the required output, not working space. `mutableListOf` is an `ArrayList`
         *   growing by 1.5x, so it re-copies a few times on the way to n — still amortized O(1) per add.
         *
         * ### Correctness notes
         * - **Empty tree** is handled by the two safe calls: with `root == null` both `node?.children?.forEach`
         *   and `node?.let` are skipped and the empty list is returned. No explicit guard needed.
         * - `children` holds non-null `NaryNode`s, so the nullable `node` parameter can only be null for the
         *   root. The null test is therefore paid twice per node for a condition that is true at most once. A
         *   single `if (node == null) return` on entry — or checking the root once outside and giving `dfs` a
         *   non-null parameter — states the intent more directly.
         * - The default argument `node: NaryNode? = root` works, but hides the starting node at the call site
         *   (`dfs()`); `dfs(root)` reads the same and costs nothing.
         * - Duplicate values are irrelevant: nothing is keyed by `val`, the traversal follows node identity.
         * - No overflow risk: values are only copied, never combined.
         *
         * ### Alternatives (the statement's follow-up: do it iteratively)
         * All are O(n) time — you cannot beat that, since the output itself has n elements. They differ only in
         * where the "pending work" lives:
         * 1. **Reversed modified pre-order (the usual trick).** Post-order is `children L->R, node`. Reverse it
         *    and you get `node, children R->L` — a pre-order that visits children right to left. So: explicit
         *    stack, pop a node, append it, push its children in *natural* order (so the last child pops first),
         *    and reverse the list at the end (or `addFirst` into an `ArrayDeque`/`LinkedList`). O(n) time, O(n)
         *    stack worst case (a star pushes all n - 1 children at once, vs. O(h) for your recursion).
         *    Caveat: nodes are *visited* in a non-post-order sequence; only the output is right. Fine here,
         *    wrong if the visit has side effects that need children processed first.
         * 2. **True iterative post-order.** Stack of (node, index of next child) pairs — i.e. hand-simulate the
         *    call frames: peek the top, if it has an unvisited child push it and bump the index, otherwise pop
         *    and emit. O(h) stack like the recursion, real post-order visit sequence, no depth limit. A
         *    cheaper variant pushes each node twice / with a `visited` flag: first pop expands, second emits.
         * 3. **Functional recursion** — `root.children.flatMap(::postorder) + root.val`. Shortest to write, but
         *    every level copies its subtree's list, so a chain costs O(n^2). Your shared accumulator is the
         *    right choice.
         * No Morris-style O(1)-space traversal applies: that relies on spare null child pointers of a binary
         * tree to thread through, which an n-ary child list does not have.
         *
         * ### Parallelism
         * Not worth it at these sizes (n <= 10^4 is microseconds), and the output is inherently ordered. In
         * principle sibling subtrees are independent: if subtree sizes are known, each subtree owns a fixed
         * slice of the output (a node's children fill consecutive ranges, the node itself goes in the last
         * slot), so a fork/join could write disjoint slices of a pre-sized array with no locking. But computing
         * the sizes is itself a full post-order pass, the work per node is one pointer chase (memory-bound,
         * not CPU-bound), and a skewed tree offers no independent work at all. Task overhead dominates.
         *
         * ### Real world
         * - Post-order is "children before parent" = dependency order: `rm -r` / `du` (a directory after its
         *   contents), freeing a tree, build systems and package installers (dependencies first), compilers
         *   emitting code for an expression AST (operands before the operator), UI layout "measure" passes
         *   (a container sizes itself from its children), bottom-up aggregation in org charts or cost roll-ups.
         * - Differences from the interview version: depth is not capped, so production code uses an explicit
         *   stack (alternative 2) or a visitor with its own work list; trees are often DAGs or have cycles
         *   (symlinks), which needs a visited set; and results are usually consumed lazily (a `sequence { }` /
         *   iterator) rather than materialized into a list.
         */
        fun postorder(root: NaryNode?): List<Int> {
            val result = mutableListOf<Int>()
            fun dfs(node: NaryNode? = root) {
                node?.children?.forEach { dfs(it) }
                node?.let { result.add(it.`val`) }
            }
            dfs()
            return result

        }

        /**
         * ## Reference (leetcode-help) — iterative post-order with a stack of (node, child iterator) frames
         *
         * **Restatement.** Every node has a value and an ordered list of any number of children. Output all
         * values so that each node appears after everything in its subtrees, and the subtrees appear in
         * child order: `subtree(child 0), subtree(child 1), ..., node`.
         *
         * **Pattern: depth-first traversal, post-order.** DFS fits because a whole subtree must be finished
         * before its parent is emitted — last-in-first-out. Post-order is harder to make iterative than
         * pre-order because a node is met twice: once going down (not ready yet) and once coming back up
         * (ready). The stack therefore has to remember *how far through its children* each open node is.
         *
         * ### Intuition
         * A recursive call frame holds two things: the node, and the position inside its `forEach` loop.
         * Store exactly that pair yourself. An `Iterator` over the children *is* the loop position. Look at
         * the top frame: if its iterator still has a child, descend into it (push a new frame); if it is
         * exhausted, every child subtree is done, so pop the frame and emit the node. That is the recursion,
         * transcribed line for line.
         *
         * ### Core concepts
         * - **Post-order** — a node is emitted after all its descendants. It is the order for anything
         *   computed bottom-up (sizes, heights, deletion, dependency order).
         * - **Call-frame simulation** — any recursion can be made iterative by pushing its local state
         *   (here: node + loop cursor) onto an explicit stack. General technique, not specific to trees.
         * - **Iterator as a resumable cursor** — keeping the iterator in the frame lets the loop over children
         *   be paused when descending and resumed on return, without an index variable.
         * - **Peek vs. pop** — a frame stays on the stack while its children are processed and is removed
         *   only when exhausted; popping on first sight is what distinguishes pre-order from post-order.
         * - **Reversal duality** — reversed post-order equals a pre-order that visits children right-to-left.
         *   It gives a shorter alternative (pre-order with children pushed in natural order, then reverse the
         *   output), at the cost of an O(n) stack and a visit sequence that is not really post-order.
         *
         * ### Approach
         * 1. Null root -> return an empty list.
         * 2. Push the frame `(root, root.children.iterator())`.
         * 3. While the stack is not empty, peek the top frame:
         *    - iterator has a next child -> push `(child, child.children.iterator())`;
         *    - otherwise -> pop the frame and append its node's value.
         * 4. Return the list.
         *
         * ### Complexity
         * - **Time O(n)** — each node is pushed once and popped once, and each iterator advances once per
         *   child, so the loop runs 2n times in total.
         * - **Space O(h)** for the stack (one frame per node on the current root-to-node path, like the
         *   recursion), plus the O(n) output list.
         *
         * ### Common pitfalls
         * - Emitting on first sight (when pushed or first popped): that is pre-order.
         * - Popping the frame before descending: the node is lost and never emitted.
         * - Assuming post-order is just reversed pre-order: it is the reverse of the *mirrored* pre-order.
         *   For `[1,null,2,3]` pre-order is 1,2,3 and post-order is 2,3,1, not 3,2,1.
         * - Building results by concatenating child lists (`flatMap { ... } + val`): copies at every level,
         *   O(n^2) on a chain.
         * - Forgetting the empty tree.
         */
        fun referenceSolution(root: NaryNode?): List<Int> {
            val result = mutableListOf<Int>()
            if (root == null) return result
            val stack = ArrayDeque<Pair<NaryNode, Iterator<NaryNode>>>()
            stack.addLast(root to root.children.iterator())
            while (stack.isNotEmpty()) {
                val (node, pending) = stack.last() // peek: the frame stays until its children are done
                if (pending.hasNext()) {
                    val child = pending.next()
                    stack.addLast(child to child.children.iterator())
                } else {
                    stack.removeLast()
                    result.add(node.`val`)
                }
            }
            return result
        }

    }
}
