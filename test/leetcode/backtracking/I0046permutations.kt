package leetcode.backtracking

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0046 = (IntArray) -> List<List<Int>>

/**
 * LeetCode 46. Permutations  (https://leetcode.com/problems/permutations/)
 *
 * Given an array `nums` of DISTINCT integers, return all the possible permutations.
 * The answer may be returned in any order (these tests pin a specific order — see below).
 *
 * Example:  nums = [1,2,3]
 *   -> [[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]   (3! = 6 permutations)
 *
 * Constraints:  1 <= nums.length <= 6,  every element within -10..10,  all distinct.
 *
 * ── The idea: backtracking ────────────────────────────────────────────────────
 * A permutation is built one slot at a time. At each slot we try every value that
 * hasn't been used yet, recurse to fill the next slot, then UNDO the choice and try
 * the next value. That "make a choice → recurse → undo the choice" loop is the heart
 * of backtracking. It explores a decision tree:
 *
 *                                  []
 *                 ┌────────────────┼────────────────┐
 *               [1]               [2]               [3]
 *             ┌───┴───┐         ┌───┴───┐         ┌───┴───┐
 *          [1,2]   [1,3]     [2,1]   [2,3]     [3,1]   [3,2]
 *            │       │         │       │         │       │
 *         [1,2,3] [1,3,2]   [2,1,3] [2,3,1]   [3,1,2] [3,2,1]   <- leaves = answers
 *
 * When the current partial list reaches length == nums.size we've hit a leaf: copy it
 * into the results (copy! the working list keeps mutating after this point).
 *
 * ── Why this exact output order ───────────────────────────────────────────────
 * Iterating candidates in their ORIGINAL order and tracking which are used (the
 * `used` boolean array below) visits the tree left-to-right, producing the
 * lexicographic order the tests expect. The classic "swap elements in place" variant
 * is also O(n·n!) and correct, but emits a different order — it would fail these
 * order-sensitive assertions.
 *
 * ── Complexity ────────────────────────────────────────────────────────────────
 * Time:  O(n · n!) — there are n! permutations and copying each leaf costs O(n).
 * Space: O(n) auxiliary for the recursion stack + working list (output excluded).
 */
class I0046permutations {

    @Nested
    inner class Solution : ProblemTest<I0046> {

        override val cases = testCases<I0046>(
            "[1,2,3]" expects "[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]",
            "[0,1]" expects "[[0,1],[1,0]]",
            "[1]" expects "[[1]]",
        )

        @Test
        fun test() = check(::permute, ::permuteRef)


        fun permute(nums: IntArray): List<List<Int>> {
            val result: MutableList<List<Int>> = mutableListOf()

            val usedIdx = MutableList(nums.size) { false }
            val current = mutableListOf<Int>()

            fun backtrack() {
                if (current.size == nums.size) {
                    result.add(current.toList())
                    return
                }

                for (i in nums.indices) {
                    if (usedIdx[i]) continue

                    current.addLast(nums[i])
                    usedIdx[i] = true

                    backtrack()

                    current.removeLast()
                    usedIdx[i] = false
                }
            }

            backtrack()
            return result
        }

        /**
         * Reference solution: backtracking with a `used` flag per element.
         * Preserves original-order iteration so the result is lexicographic.
         */
        fun permuteRef(nums: IntArray): List<List<Int>> {
            val result = mutableListOf<List<Int>>()
            val current = mutableListOf<Int>()
            val used = BooleanArray(nums.size)

            fun backtrack() {
                // Base case: a full-length permutation is complete — record a COPY.
                if (current.size == nums.size) {
                    result.add(current.toList())
                    return
                }
                // Try each not-yet-used value as the next slot.
                for (i in nums.indices) {
                    if (used[i]) continue
                    used[i] = true            // choose
                    current.add(nums[i])
                    backtrack()               // explore
                    current.removeAt(current.lastIndex) // un-choose (backtrack)
                    used[i] = false
                }
            }

            backtrack()
            return result
        }
    }
}
