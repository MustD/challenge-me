package leetcode.array_string

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 455. Assign Cookies  (https://leetcode.com/problems/assign-cookies/)
 *
 * You want to give cookies to children, at most one cookie per child. Child i has a greed factor
 * g[i] (the minimum cookie size that will satisfy them), and cookie j has size s[j]. Cookie j can
 * be assigned to child i if s[j] >= g[i], making that child content. Return the maximum number of
 * content children you can achieve.
 *
 * Constraints:
 * - 1 <= g.length <= 3 * 10^4
 * - 0 <= s.length <= 3 * 10^4   (note: the cookie array may be empty)
 * - 1 <= g[i], s[j] <= 2^31 - 1
 */
typealias I0455 = (IntArray, IntArray) -> Int

class I0455findContentChildren {

    @Nested
    inner class Solution : ProblemTest<I0455> {

        override val cases = testCases<I0455>(
            args("[1,2,3]", "[1,1]") expects 1,
            args("[1,2]", "[1,2,3]") expects 2,
            // edge cases
            args("[1,2,3]", "[]") expects 0,        // no cookies at all
            args("[10]", "[1,2,3]") expects 0,      // no cookie large enough
            args("[1,1,1]", "[1,1,1]") expects 3,   // exact match for everyone
        )

        @Test
        fun test() = check(::referenceSolution)

        fun findContentChildren(g: IntArray, s: IntArray): Int {
            TODO("implement")
        }

        /**
         * Pattern: Greedy + two pointers on two sorted arrays.
         *
         * Intuition:
         * To maximize the number of content children, never "waste" a cookie. If you sort both the
         * greed factors and the cookie sizes ascending, the optimal move is: give the smallest cookie
         * that can satisfy the least-greedy remaining child. Spending a bigger cookie on a small-greed
         * child can only hurt — that bigger cookie might have been the only one able to satisfy a
         * greedier child later. This local "smallest sufficient cookie" choice is provably globally
         * optimal (a classic exchange-argument greedy).
         *
         * Approach:
         * 1. Sort g (greed) and s (sizes) ascending.
         * 2. Walk a pointer `child` over g and `cookie` over s.
         * 3. If the current cookie satisfies the current child (s[cookie] >= g[child]), this child is
         *    content: advance both pointers and increment the count.
         * 4. Otherwise the cookie is too small for even the least-greedy remaining child, so it's
         *    useless — advance only the cookie pointer.
         * 5. Stop when either array is exhausted; the count is the answer.
         *
         * Complexity:
         * - Time:  O(n log n + m log m) for the two sorts, then a single O(n + m) linear pass.
         * - Space: O(1) extra (in-place sort aside; the harness passes fresh arrays per run, so
         *   sorting in place here is safe).
         *
         * Common pitfalls:
         * - Empty cookie array (s.length == 0) → answer is 0; the loop guard handles it naturally.
         * - Forgetting to sort — the two-pointer logic is only correct on sorted input.
         * - Advancing both pointers when the cookie is too small (you'd skip a child who could still
         *   be served by a later, larger cookie). Only advance the cookie pointer on a miss.
         * - g[i]/s[j] can be up to 2^31 - 1, but a plain Int comparison (s >= g) is fine — no overflow
         *   since there's no arithmetic on the values.
         */
        fun referenceSolution(g: IntArray, s: IntArray): Int {
            g.sort()
            s.sort()
            var child = 0
            var cookie = 0
            while (child < g.size && cookie < s.size) {
                if (s[cookie] >= g[child]) child++
                cookie++
            }
            return child
        }

    }
}
