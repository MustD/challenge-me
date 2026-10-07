package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.expectsAnyOf
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 108. Convert Sorted Array to Binary Search Tree  (https://leetcode.com/problems/convert-sorted-array-to-binary-search-tree/)
 *
 * Given an integer array `nums` where the elements are sorted in ascending order, convert it to a height-balanced
 * binary search tree.
 *
 * Constraints:
 * - 1 <= nums.length <= 10^4
 * - -10^4 <= nums[i] <= 10^4
 * - nums is sorted in a strictly increasing order.
 */
typealias I0108 = (IntArray) -> TreeNode?

class I0108sortedArrayToBST {

    @Nested
    inner class Solution : ProblemTest<I0108> {

        override val cases = testCases<I0108>(
            "[-10,-3,0,5,9]".expectsAnyOf("[0,-3,9,-10,null,5]", "[0,-10,5,null,-3,null,9]"),
            "[1,3]".expectsAnyOf("[3,1]", "[1,null,3]"),
        )

        @Test
        fun test() = check(::sortedArrayToBST, ::referenceSolution)

        fun sortedArrayToBST(nums: IntArray): TreeNode? {
            fun toNode(lo: Int = 0, hi: Int = nums.lastIndex): TreeNode? {
                if (lo > hi) return null
                val midI = lo + (hi - lo) / 2
                return TreeNode(nums[midI]).apply {
                    left = toNode(lo, midI - 1)
                    right = toNode(midI + 1, hi)
                }
            }

            return toNode()
        }

        /**
         * ## Reference solution — divide & conquer on the middle element
         *
         * **Restatement.** You get a strictly increasing array. Build *any* binary search tree that holds exactly
         * these values and is height-balanced: for every node, the heights of its left and right subtrees differ by
         * at most 1.
         *
         * **Pattern: divide & conquer (recursive construction from a range).** A sorted array *is* the in-order
         * traversal of every BST over those values. So the only real decision is "which element becomes the root?"
         * — everything to its left must go in the left subtree and everything to its right in the right subtree.
         * That splits the problem into two independent, smaller copies of itself: a textbook divide-and-conquer
         * shape, the same one as merge sort or binary search.
         *
         * ### Intuition
         * Pick the **middle** element as the root. The two halves then differ in size by at most one element, so
         * the two subtrees can differ in height by at most one. Apply the same rule recursively to each half and
         * balance holds at every node — not just at the root.
         *
         * ### Core concepts
         * - **In-order traversal of a BST is sorted** — left, node, right visits values in ascending order. Here it
         *   means the array already fixes which values go left/right of any chosen root; only the root choice is free.
         * - **Height-balanced tree** — every node's subtree heights differ by ≤ 1. Splitting the range in half at
         *   each step guarantees it, and gives height ⌈log₂(n+1)⌉.
         * - **Divide & conquer over an index range `[lo, hi]`** — recurse on sub-ranges defined by two indices
         *   instead of copying sub-arrays; this keeps each step O(1) extra work.
         * - **Empty-range base case** — `lo > hi` returns `null`; it is what produces the leaves' null children.
         *
         * ### Approach
         * 1. `build(lo, hi)`: if `lo > hi` return `null`.
         * 2. `mid = lo + (hi - lo) / 2`; create `TreeNode(nums[mid])`.
         * 3. `node.left = build(lo, mid - 1)`, `node.right = build(mid + 1, hi)`.
         * 4. Return `build(0, nums.lastIndex)`.
         *
         * ### Complexity
         * - **Time O(n)** — each element becomes exactly one node, with O(1) work per call.
         * - **Space O(log n)** extra for the recursion stack (the tree is balanced), plus O(n) for the output tree.
         *
         * ### Common pitfalls
         * - **Several answers are valid.** For even-length ranges either middle (`(lo+hi)/2` or `(lo+hi+1)/2`)
         *   works; lower-middle vs. upper-middle just produces a different but equally correct tree. Tests must
         *   accept both (hence `expectsAnyOf`).
         * - **Slicing the array** (`copyOfRange`) at every level costs O(n log n) time and memory — pass indices.
         * - **Off-by-one in the range**: with an inclusive `[lo, hi]` the base case is `lo > hi` and the children
         *   are `mid - 1` / `mid + 1`. Mixing inclusive and half-open conventions drops or duplicates elements.
         * - `(lo + hi) / 2` can overflow in general; `lo + (hi - lo) / 2` is the safe habit (harmless here with
         *   n ≤ 10⁴, but it transfers to binary search on large ranges).
         */
        fun referenceSolution(nums: IntArray): TreeNode? {
            fun build(lo: Int, hi: Int): TreeNode? {
                if (lo > hi) return null
                val mid = lo + (hi - lo) / 2
                return TreeNode(nums[mid]).apply {
                    left = build(lo, mid - 1)
                    right = build(mid + 1, hi)
                }
            }
            return build(0, nums.lastIndex)
        }

    }
}
