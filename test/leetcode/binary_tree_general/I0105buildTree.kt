package leetcode.binary_tree_general

import leetcode.expects
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 105. Construct Binary Tree from Preorder and Inorder Traversal
 * (https://leetcode.com/problems/construct-binary-tree-from-preorder-and-inorder-traversal/)
 *
 * Given two integer arrays `preorder` and `inorder`, where `preorder` is the preorder traversal of a
 * binary tree and `inorder` is the inorder traversal of the *same* tree, construct the tree and
 * return its root.
 *
 * Constraints:
 * - 1 <= preorder.length <= 3000
 * - inorder.length == preorder.length
 * - -3000 <= preorder[i], inorder[i] <= 3000
 * - `preorder` and `inorder` consist of **unique** values.
 * - Each value of `inorder` also appears in `preorder`.
 * - `preorder` is guaranteed to be the preorder traversal of the tree.
 * - `inorder` is guaranteed to be the inorder traversal of the tree.
 */
typealias I0105 = (IntArray, IntArray) -> TreeNode?

class I0105buildTree {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0105> {

        // Expected values are the LeetCode level-order array form of the tree; equality is by
        // TreeNode.toString(), so the reconstructed shape must match exactly.
        override val cases = leetcode.testCases<I0105>(
            leetcode.args("[3,9,20,15,7]", "[9,3,15,20,7]") expects "[3,9,20,null,null,15,7]",
            leetcode.args("[-1]", "[-1]") expects "[-1]",
            leetcode.args("[1,2]", "[2,1]") expects "[1,2]",
            leetcode.args("[1,2]", "[1,2]") expects "[1,null,2]",
        )

        @Test
        fun test() = check(::buildTree, ::referenceSolution)

        /**
         * ## Verdict
         *
         * Correct and asymptotically optimal — O(n) time, which is the lower bound (every one of the
         * n values must be read and one node created per value). Analysis below refers to this
         * implementation specifically.
         *
         * ## Pattern
         *
         * **Divide & conquer over index ranges, with a value→index map and a single monotone preorder
         * cursor.** The recursion carries only the *inorder* window `[lo, hi]`; the preorder side is
         * tracked implicitly by `preIndex`, because visiting root → left → right *is* preorder order.
         *
         * ## Time — O(n)
         *
         * - `inorder.mapIndexed { … }.toMap()` — one pass, O(n).
         * - `build(lo, hi)` is entered exactly `2n + 1` times (n real nodes plus the `lo > hi` null
         *   leaves), and each call does only O(1) work: one `preIndex++`, one `TreeNode(...)`
         *   allocation, one `inorderIndexOf[...]` hash lookup. No call ever scans a range.
         * - Total: O(n) worst, average and best alike — the shape of the tree does not change the
         *   *count* of calls, only their nesting.
         *
         * ## Space — O(n)
         *
         * - `inorderIndexOf`: n entries → O(n) auxiliary. Note this line allocates more than it needs
         *   to: `mapIndexed` materialises an intermediate `List<Pair<Int,Int>>` (n `Pair` objects, both
         *   fields boxed) before `toMap()` copies it into a `LinkedHashMap`. Same O(n) class, ~3x the
         *   garbage of a `HashMap(n)` filled by `forEachIndexed`. Irrelevant at n ≤ 3000, worth knowing
         *   at scale.
         * - Recursion stack: O(h) — O(log n) balanced, **O(n) for a degenerate (fully skewed) tree**.
         *   With n ≤ 3000 that is ~3000 frames, comfortably inside the default JVM stack.
         * - The returned tree itself is O(n) *output* space, not counted as auxiliary.
         *
         * ## Correctness notes
         *
         * - **Left before right is load-bearing.** `preIndex` is shared mutable state; swapping the two
         *   `build` lines would still compile and still produce a tree — a silently wrong one. This is
         *   the one place the implementation is fragile to a harmless-looking edit.
         * - **Uniqueness is a real dependency.** `inorderIndexOf` is keyed by value; duplicates would
         *   collapse to the last index and mis-split. Guaranteed by the constraints, but it is an
         *   assumption, not a property of the algorithm.
         * - `lo > hi` handles the empty subtree, so `if (preorder.isEmpty()) return null` and
         *   `if (preorder.size == 1) …` are both **redundant** — `build(0, -1)` already returns null and
         *   `build(0, 0)` already returns a single leaf. Harmless, but they are two extra branches that
         *   duplicate invariants the recursion already enforces.
         * - The `?: throw IllegalStateException("")` branch is unreachable given "each value of inorder
         *   also appears in preorder". If it is kept as a defensive assert, give it a message — a bare
         *   empty string tells future-you nothing at 3am.
         * - Values are ints in [-3000, 3000]; no overflow surface here.
         *
         * ## Alternatives and trade-offs
         *
         * 1. **Explicit preorder bounds instead of a shared cursor.** Pass `(preLo, inLo, inHi)` and
         *    derive the left-subtree size `k = rootIn - inLo`; left is `preLo + 1`, right is
         *    `preLo + 1 + k`. Same O(n)/O(n), one more parameter — but the recursion becomes *pure*
         *    (no shared state), which removes the left-before-right hazard above and is the
         *    prerequisite for parallelising (see below).
         * 2. **Iterative with an explicit stack.** Walk `preorder` once, pushing nodes, and pop while
         *    the stack top's value equals `inorder[inIdx]` to find where the right child attaches.
         *    O(n) time, O(n) stack, but heap-allocated — immune to StackOverflowError on skewed input.
         *    This is what you would reach for if n were 10^6 instead of 3000.
         * 3. **No map, linear `indexOf` per node.** O(1) extra beyond the stack, but O(n²) time —
         *    degenerate on a skewed tree. Still passes at n ≤ 3000; the map is the honest answer.
         * 4. **Slicing sub-arrays** (`copyOfRange`) per call. Reads beautifully, allocates O(n) per
         *    level ⇒ O(n²) memory traffic. Index windows exist precisely to avoid this.
         *
         * No approach beats O(n) time; this one is already at the floor.
         *
         * ## Parallelism
         *
         * This problem is a rare LeetCode case where a parallel decomposition is genuinely *meaningful*
         * — after the root split, the left and right subtrees are fully independent subproblems over
         * disjoint index ranges, i.e. textbook fork/join. Two caveats make it unattractive in practice:
         *
         * - **This implementation cannot be parallelised as written.** The shared `preIndex` cursor
         *   creates a strict sequential dependency: the right subtree's starting offset is only known
         *   *after* the left one has finished consuming. Alternative 1 above removes that.
         * - **The economics do not work at this size.** n ≤ 3000 means ~3000 tiny allocations; a
         *   fork/join task has an overhead in the hundreds of nanoseconds, so a sensible sequential
         *   cutoff (~10⁴ nodes per task) would never fire. The work is also allocation-bound rather
         *   than compute-bound, so threads contend on the allocator and GC rather than on CPU. And
         *   there is no balance guarantee — a skewed tree gives one thread all the work, capping
         *   speedup at ~1x by Amdahl's law regardless of core count.
         *
         * Honest summary: parallelisable in principle, not worth it here. The map-building pass is the
         * only trivially data-parallel part, and it is O(n) with a tiny constant.
         *
         * ## Where this shows up in production
         *
         * Reconstructing a tree from linear traversals is the deserialisation half of every
         * tree-over-a-wire format: ASTs persisted as a preorder token stream by compilers and linters,
         * DOM/virtual-DOM hydration, protobuf/JSON document trees, Merkle-tree and B-tree page
         * reconstruction, and filesystem/archive layouts (a tar stream is essentially preorder).
         *
         * What changes at real scale:
         *
         * - Real formats **do not need two traversals**. Preorder alone is enough if you also record
         *   null sentinels or child counts — which is why serialised trees carry explicit structure
         *   markers. Needing both preorder *and* inorder is an artefact of the puzzle.
         * - **Recursion depth becomes the failure mode**, not time. Adversarially skewed input on a
         *   recursive parser is a classic stack-overflow DoS; production parsers use an explicit stack
         *   or a depth limit (alternative 2).
         * - **Streaming, not arrays.** Inputs arrive as a stream; you cannot random-access `inorder`
         *   to build an index map, which pushes you toward the single-pass iterative form.
         * - Cache behaviour dominates: the map lookups are random-access over the heap, and the node
         *   allocations pointer-chase. An arena / flat array-of-structs layout with integer child
         *   indices routinely beats the "optimal" pointer tree by several times, despite identical
         *   Big-O — a good reminder that O(n) is a floor on *operations*, not on wall clock.
         */
        fun buildTree(preorder: IntArray, inorder: IntArray): TreeNode? {
            if (preorder.isEmpty()) return null
            if (preorder.size == 1) return TreeNode(preorder[0])

            val inorderIndexOf = inorder.mapIndexed { index, i -> i to index }.toMap()
            var preIndex = 0

            fun build(lo: Int, hi: Int): TreeNode? {
                if (lo > hi) return null
                val node = TreeNode(preorder[preIndex++])

                val indexOf = inorderIndexOf[node.`val`] ?: throw IllegalStateException("")
                node.left = build(lo, indexOf - 1)
                node.right = build(indexOf + 1, hi)
                return node
            }


            return build(0, inorder.lastIndex)
        }


        /**
         * ## Restatement
         *
         * `preorder` visits **root, left subtree, right subtree**; `inorder` visits **left, root, right**.
         * Both list the same `n` unique values. Rebuild the unique tree that produces both.
         *
         * ## Pattern: divide & conquer over index ranges (+ a value→index map)
         *
         * Two facts do all the work:
         *
         * 1. `preorder[0]` is *always* the root of the current subtree.
         * 2. Find that root value inside `inorder`: everything to its **left** is exactly the left
         *    subtree, everything to its **right** is exactly the right subtree. That split also tells
         *    you the **size** `k` of the left subtree.
         *
         * Sizes are the bridge back to `preorder`: after the root, the next `k` entries of `preorder`
         * are the whole left subtree, and the rest are the right subtree. So each recursive call is
         * "here is a preorder window and an inorder window describing the same subtree".
         *
         * ## Approach
         *
         * - Pre-build `HashMap<value, indexInInorder>` so locating the root is O(1) instead of a scan.
         * - Walk a single shared `preIndex` cursor forward. Because recursion goes root → left → right,
         *   consuming `preorder` left-to-right *is* preorder order — so you never need explicit
         *   preorder bounds, only the inorder window `[lo, hi]`.
         * - `lo > hi` ⇒ empty subtree ⇒ `null`. (This also covers the empty-input case.)
         *
         * ## Complexity
         *
         * - Time **O(n)** — every node is created once and its root position is an O(1) map lookup.
         * - Space **O(n)** — the map, plus O(h) recursion stack (O(n) for a degenerate/skewed tree).
         *
         * Without the map (linear `indexOf` per node) it degrades to **O(n²)** on a skewed tree —
         * still accepted for n ≤ 3000, but the map is the "real" answer.
         *
         * ## Pitfalls
         *
         * - Recursing **right before left** silently corrupts the tree: the shared `preIndex` cursor
         *   depends on the left subtree being consumed first.
         * - Splitting by *value ranges* (BST style) is wrong here — this is a plain binary tree, values
         *   carry no ordering. The only splitter is the inorder position.
         * - Off-by-one: the root itself belongs to neither half — left is `[lo, rootIn - 1]`, right is
         *   `[rootIn + 1, hi]`.
         * - Slicing new sub-arrays each call is easy to read but allocates O(n) per level ⇒ O(n²)
         *   memory traffic; pass indices instead.
         *
         * ## On your attempt
         *
         * You started by asking *"is the next preorder element the left child?"*. That question is
         * answerable, but it only decides **one edge**, so it doesn't compose into a recursion. Flip
         * it: instead of asking where the next element goes, ask **how big the left subtree is**
         * (`inorder.indexOf(root) - lo`) — that single number partitions *both* arrays at once and
         * hands you two identical sub-problems. Your `inRootIndex` is already the right quantity; you
         * were one step away.
         */
        fun referenceSolution(preorder: IntArray, inorder: IntArray): TreeNode? {
            val inorderIndexOf = HashMap<Int, Int>(inorder.size)
            inorder.forEachIndexed { index, value -> inorderIndexOf[value] = index }

            var preIndex = 0

            fun build(lo: Int, hi: Int): TreeNode? {
                if (lo > hi) return null

                val rootValue = preorder[preIndex++]
                val node = TreeNode(rootValue)
                val rootIn = inorderIndexOf.getValue(rootValue)

                // Left first: the shared preIndex cursor must consume the whole left subtree
                // before the right one starts.
                node.left = build(lo, rootIn - 1)
                node.right = build(rootIn + 1, hi)

                return node
            }

            return build(0, inorder.lastIndex)
        }

    }
}
