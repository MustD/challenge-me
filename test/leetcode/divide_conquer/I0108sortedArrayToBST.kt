package leetcode.divide_conquer

import leetcode.expects
import leetcode.expectsAnyOf
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 108. Convert Sorted Array to Binary Search Tree  (https://leetcode.com/problems/convert-sorted-array-to-binary-search-tree/)
 *
 * Given an integer array `nums` sorted in strictly increasing order, build a
 * height-balanced binary search tree from it. A binary tree is height-balanced
 * when, for every node, the depths of its two subtrees differ by at most one.
 *
 * Constraints:
 * - 1 <= nums.length <= 10^4
 * - -10^4 <= nums[i] <= 10^4
 * - nums is sorted in strictly increasing order.
 *
 * NOTE — multiple valid answers: LeetCode accepts any height-balanced BST, so
 * several tree shapes are correct. The harness compares by exact tree structure
 * (toString), which is order-/shape-sensitive, so for even-sized spans (where
 * there are two legal middles) a single `expects` would only ever accept ONE
 * convention. Because both the lower-middle midpoint ((lo+hi)/2, used by
 * `sortedArrayToBST`) and the upper-middle midpoint ((lo+hi+1)/2, used by
 * `referenceSolution`) are correct, those cases use `expectsAnyOf` and list both
 * legal shapes. Odd-fully-balanced spans (e.g. [0], [1,2,3,4,5,6,7]) collapse to
 * the same tree under either convention, so they stay as plain `expects`.
 */
typealias I0108 = (IntArray) -> TreeNode?

class I0108sortedArrayToBST {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0108> {

        override val cases = testCases<I0108>(
            // even spans → two legal middles → two legal shapes (upper, lower)
            "[-10,-3,0,5,9]".expectsAnyOf("[0,-3,9,-10,null,5]", "[0,-10,5,null,-3,null,9]"),
            "[1,3]".expectsAnyOf("[3,1]", "[1,null,3]"),
            // fully-balanced spans → identical under either convention
            "[0]" expects "[0]",
            "[1,2,3,4,5,6,7]" expects "[4,2,6,1,3,5,7]",
        )

        @Test
        fun test() = check(::sortedArrayToBST, ::referenceSolution, ::solutionLegacy)

        /**
         * ANALYSIS of the solution below (your `dq` recursion).
         *
         * PATTERN — Divide & Conquer on a sorted array ("sorted array → balanced
         * BST"). A BST's in-order traversal is the sorted array; picking the middle
         * as the root and recursing on each half is the exact inverse of an in-order
         * walk, and because each half differs in size by at most one the resulting
         * tree is height-balanced by construction. Same family as building a segment
         * tree / kd-tree by median splitting.
         *
         * MIDPOINT CONVENTION — you compute `mid = first + (last - first) / 2`. That
         * is the overflow-safe rewrite of `(first + last) / 2`, i.e. the *lower*
         * middle. For even spans this yields a different — but equally valid —
         * balanced BST than `referenceSolution`'s upper middle `(lo + hi + 1) / 2`.
         * Both are correct answers, which is exactly why the even-span cases use
         * `expectsAnyOf` (listing your lower-middle shape as one of the accepted
         * outputs). Writing it as `first + (last-first)/2` rather than `(first+last)/2`
         * is a good habit even though n <= 10^4 can't actually overflow here.
         *
         * TIME — O(n). Each recursive call consumes exactly one index (the mid) and
         * emits exactly one node; the n nodes are produced by n calls plus O(n) empty
         * base-case calls. Crucially you recurse over `IntRange` *views* (`first..mid-1`),
         * never copying the array, so there is no hidden O(n) per level — contrast the
         * copyOfRange approach below.
         *
         * SPACE — O(log n) auxiliary: the recursion stack is bounded by the tree
         * height, which is ceil(log2(n)) because the tree is balanced. The returned
         * tree itself is O(n) output, not counted as working space. An `IntRange` is a
         * tiny value object (two ints), so slicing adds no meaningful allocation.
         *
         * CORRECTNESS / EDGE CASES —
         *   - Base case is `range.isEmpty()` (i.e. first > last), so `mid` is only
         *     evaluated on non-empty spans — no reading past the ends, and leaves are
         *     kept (a `>=` guard would wrongly drop single-element spans).
         *   - Single element [x] → mid = its own index, both children empty → leaf.
         *   - Default arg `range = nums.indices` cleanly seeds the whole array; since
         *     constraints guarantee length >= 1 the top-level range is never empty.
         *
         * ALTERNATIVES —
         *   - Upper-middle variant (referenceSolution): same O(n)/O(log n), only the
         *     even-span shape differs.
         *   - Slice with `nums.copyOfRange(...)` per call: still correct but O(n log n)
         *     time and O(n) extra memory from the copies — strictly worse; index
         *     windows (what you did) are the standard fix.
         *   - Iterative with an explicit stack of (node, lo, hi) frames: removes the
         *     O(log n) call stack in favor of an O(log n) explicit stack — same
         *     asymptotics, no real win here. Your recursion is already optimal: any
         *     algorithm must emit n nodes, so O(n) time is a hard lower bound.
         *
         * PARALLELISM — divide & conquer is structurally fork/join-able: the left and
         * right subtrees are fully independent, so each recursive call could spawn a
         * task. In practice it is not worth it here — with n <= 10^4 the work per node
         * is a single allocation, and thread/task scheduling overhead dwarfs it; you'd
         * only fork above a large sequential cutoff. This is the honest teaching point:
         * the *shape* is parallel, the *scale* isn't.
         *
         * REAL-WORLD — this is the "bulk-load a balanced index" pattern. When you
         * already have sorted data you build the balanced structure in one O(n) pass
         * (balanced BST, static B-tree/B+-tree bulk load, segment tree, kd-tree)
         * instead of inserting one key at a time and paying O(n log n) with rotations.
         * Real systems care about memory layout too — a pointer-per-node BST thrashes
         * cache, so production "balanced BST over sorted data" is usually an implicit
         * array-backed tree (like a heap's index math) or a cache-friendly B-tree.
         */
        fun sortedArrayToBST(nums: IntArray): TreeNode? {
            fun dq(range: IntRange = nums.indices): TreeNode? {
                if (range.isEmpty()) return null

                val mid = range.first + (range.last - range.first) / 2

                return TreeNode(`val` = nums[mid]).apply {
                    left = dq(range.first..mid - 1)
                    right = dq(mid + 1..range.last)
                }
            }

            return dq()
        }

        /**
         * PATTERN — Divide & Conquer on a sorted array (the "sorted array → BST"
         * template). A BST is defined by in-order = sorted order, and a *balanced*
         * BST is what you get by always choosing the middle element as the root:
         * everything left of it becomes the left subtree, everything right becomes
         * the right subtree, and each side has (almost) equal size, so heights
         * differ by at most one. Recurse the same rule on each half. This is the
         * exact inverse of an in-order traversal.
         *
         * APPROACH — recurse over an index window [lo, hi] (inclusive) instead of
         * slicing the array:
         *   1. Base case: lo > hi  → empty range → null.
         *   2. Pick the midpoint as the root value.
         *   3. root.left  = build(lo, mid - 1); root.right = build(mid + 1, hi).
         *
         * MIDPOINT CONVENTION (this is the whole trick for passing the fixed
         * cases): for an even-sized span there are two valid middles. The class
         * doc pins the "pick index size/2" = *upper* middle shape. With an
         * inclusive window that is mid = (lo + hi + 1) / 2, NOT the usual
         * (lo + hi) / 2 (which rounds to the lower middle and would produce an
         * equally-valid but different tree that fails these exact-match cases).
         *   e.g. [1,3] → lo=0,hi=1 → mid=(0+1+1)/2=1 → root=3, left=1 → "[3,1]".
         *
         * COMPLEXITY — O(n) time: each element is visited once and becomes exactly
         * one node (no array copying). O(log n) auxiliary space for the recursion
         * stack since the tree is balanced (plus O(n) for the returned tree).
         *
         * PITFALLS —
         *   - Using (lo + hi) / 2 → valid BST, wrong shape for these cases.
         *   - Slicing with copyOfRange on each call → correct but O(n log n) time
         *     and O(n) extra memory (the older divide_conquer copy does this).
         *   - Off-by-one on the inclusive window: children are mid-1 and mid+1, and
         *     the base case must be lo > hi (not lo >= hi, which would drop leaves).
         */
        fun referenceSolution(nums: IntArray): TreeNode? {
            fun build(lo: Int, hi: Int): TreeNode? {
                if (lo > hi) return null
                val mid = (lo + hi + 1) / 2   // upper middle → matches the size/2 shape
                return TreeNode(nums[mid]).apply {
                    left = build(lo, mid - 1)
                    right = build(mid + 1, hi)
                }
            }
            return build(0, nums.lastIndex)
        }

        fun solutionLegacy(nums: IntArray): TreeNode? {
            fun dfs(nums: IntArray): TreeNode? {
                val half = nums.size / 2
                return TreeNode(nums[half]).apply {
                    left = if (half > 0) dfs(nums.copyOfRange(0, half)) else null
                    right = if (half < nums.size - 1) dfs(nums.copyOfRange(half + 1, nums.size)) else null
                }
            }
            return dfs(nums)
        }

    }
}
