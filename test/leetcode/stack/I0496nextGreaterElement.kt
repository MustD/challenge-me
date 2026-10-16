package leetcode.stack

import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 496. Next Greater Element I  (https://leetcode.com/problems/next-greater-element-i/)
 *
 * The next greater element of some element x in an array is the first greater element that is to the right of x in the
 * same array.
 *
 * You are given two distinct 0-indexed integer arrays nums1 and nums2, where nums1 is a subset of nums2.
 *
 * For each 0 <= i < nums1.length, find the index j such that nums1[i] == nums2[j] and determine the next greater
 * element of nums2[j] in nums2. If there is no next greater element, then the answer for this query is -1.
 *
 * Return an array ans of length nums1.length such that ans[i] is the next greater element as described above.
 *
 * Constraints:
 * - 1 <= nums1.length <= nums2.length <= 1000
 * - 0 <= nums1[i], nums2[i] <= 10^4
 * - All integers in nums1 and nums2 are unique.
 * - All the integers of nums1 also appear in nums2.
 *
 * Follow up: Could you find an O(nums1.length + nums2.length) solution?
 */
typealias I0496 = (IntArray, IntArray) -> IntArray

class I0496nextGreaterElement {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0496> {

        override val cases = testCases<I0496>(
            args("[4,1,2]", "[1,3,4,2]") expects "[-1,3,-1]",
            args("[2,4]", "[1,2,3,4]") expects "[3,-1]",
            args("[1]", "[1]") expects "[-1]",                          // single element, nothing to the right
            args("[5,4,3]", "[5,4,3,2,1]") expects "[-1,-1,-1]",        // strictly decreasing: no answers
            args("[1,2,3]", "[1,2,3,4]") expects "[2,3,4]",             // strictly increasing: immediate neighbour
            args("[3,1,2]", "[3,1,2,5]") expects "[5,2,5]",             // one big value resolves several at once
        )

        @Test
        fun test() = check(::nextGreaterElement, ::referenceSolution)

        fun nextGreaterElement(nums1: IntArray, nums2: IntArray): IntArray {
            val nextGreater = mutableMapOf<Int, Int>()
            val monotonicStack = ArrayDeque<Int>()
            nums2.forEach { num ->
                while (monotonicStack.isNotEmpty() && monotonicStack.last() < num) {
                    val item = monotonicStack.removeLast()
                    nextGreater[item] = num
                }
                monotonicStack.addLast(num)
            }
            return IntArray(nums1.size) { idx -> nextGreater[nums1[idx]] ?: -1 }
        }

        /**
         * ## Reference solution — monotonic stack + hash map
         *
         * **Restatement.** For every value in `nums2`, find the first value to its right that is strictly larger
         * (or -1 if none). `nums1` is just a list of lookups into that table: answer each query by value.
         *
         * **Pattern: "next greater element" via a monotonic decreasing stack.** Whenever a problem asks "for each
         * element, what is the first element to the right/left that is greater/smaller?", a monotonic stack answers
         * it for *all* elements in one linear pass. The brute force re-scans to the right for each element
         * (O(n²)); the stack avoids that by remembering exactly the elements still "waiting" for their answer.
         * Because all values are unique, a `HashMap<value, nextGreater>` then answers each `nums1` query in O(1).
         *
         * ### Intuition
         * Scan `nums2` left to right and keep a stack of elements that have not yet seen anything bigger. Those
         * elements are necessarily in decreasing order from bottom to top (if a bigger one came later, it would
         * already have resolved the smaller ones). When a new value `x` arrives, every stacked value smaller than
         * `x` has just met its first greater element — and it is `x`. Pop them, record `x` as their answer, then
         * push `x` to wait for its own. Whatever is left on the stack at the end has no greater element (-1).
         *
         * ### Core concepts
         * - **Monotonic stack invariant** — the stack is kept sorted (here: strictly decreasing bottom→top). It
         *   matters because the top is always the smallest pending element, so one comparison tells you whether
         *   `x` resolves anything, and popping stops at the first element `>= x`.
         * - **Amortized analysis** — the inner `while` looks quadratic, but each element is pushed once and popped
         *   at most once, so total work across the whole scan is O(n). This is the reason the stack beats rescanning.
         * - **Precompute-then-query** — solve the problem for the whole "universe" (`nums2`) once, store it in a map,
         *   then answer the subset of queries (`nums1`) by lookup. Works here because values are unique keys.
         * - **Pending/unresolved set** — the stack holds exactly the elements whose answer is still unknown; what
         *   remains at the end are the "no answer" cases, handled by a default value.
         *
         * ### Approach
         * 1. Create an empty stack and an empty map `nextGreater`.
         * 2. For each `x` in `nums2`: while the stack is non-empty and `top < x`, pop `top` and set
         *    `nextGreater[top] = x`. Then push `x`.
         * 3. For each `v` in `nums1`, output `nextGreater[v] ?: -1`.
         *
         * ### Complexity
         * - Time: O(n + m), n = `nums2.size`, m = `nums1.size` — each `nums2` element is pushed/popped once, each
         *   query is an O(1) map lookup. (This meets the follow-up.)
         * - Space: O(n) — the stack and the map each hold at most n entries.
         *
         * ### Common pitfalls
         * - **Indexing by position instead of value.** `nums1[i]` is a *value*; its position in `nums2` differs.
         *   Key the map by value (safe because all values are unique).
         * - **Wrong stack direction / comparison.** For "next greater" pop while `top < x` (strict). Using `<=` is
         *   harmless here only because values are unique; with duplicates it changes the semantics.
         * - **Forgetting the leftovers.** Elements still on the stack never found a greater one — they must map
         *   to -1 (here via the `?: -1` default rather than an explicit drain loop).
         * - **Looking left instead of right.** The greater element must be to the *right* in `nums2`; scanning
         *   left-to-right and resolving on pop guarantees that.
         */
        fun referenceSolution(nums1: IntArray, nums2: IntArray): IntArray {
            val nextGreater = HashMap<Int, Int>(nums2.size)
            val stack = ArrayDeque<Int>()
            for (x in nums2) {
                while (stack.isNotEmpty() && stack.last() < x) {
                    nextGreater[stack.removeLast()] = x
                }
                stack.addLast(x)
            }
            return IntArray(nums1.size) { nextGreater[nums1[it]] ?: -1 }
        }

    }
}
