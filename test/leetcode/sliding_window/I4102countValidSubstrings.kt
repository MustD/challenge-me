package leetcode.sliding_window

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4102. Count Valid Substrings I  (https://leetcode.com/problems/count-valid-substrings-i/)
 *
 * You are given a string `s` consisting only of the characters 'a', 'b', and 'c'.
 *
 * A string is valid if it does not contain "abc" as a substring.
 *
 * Return the number of valid substrings of `s`.
 *
 * Substrings with different start or end indices are counted separately, even if they have the same content.
 *
 * Constraints:
 * - 1 <= s.length <= 10^5
 * - s consists only of 'a', 'b', and 'c'.
 */
typealias I4102 = (String) -> Int

class I4102countValidSubstrings {

    @Nested
    inner class Solution : ProblemTest<I4102> {

        override val cases = testCases<I4102>(
            "abac" expects 10,
            "abc" expects 5,
            "ac" expects 3,
            "a" expects 1,              // single element, n = 1
            "abca" expects 8,           // "abc" at index 0, short tail
            "abcabc" expects 14,        // back-to-back occurrences, first at index 0
            "aabcc" expects 11,         // occurrence in the middle, valid chars on both sides
            "abcbcabc" expects 25,      // multiple occurrences, one ending at the last index
            "ababab" expects 21,        // near misses only: no "abc", all substrings valid
            "abcc" expects 8,           // occurrence at 0 followed by one char
        )

        @Test
        fun test() = check(
            ::countValidSubstrings,
            ::referenceSolution,
        )

        fun countValidSubstrings(s: String): Int {
            var lastAbc = -1
            var result = 0L
            s.indices.forEach { r ->
                if (s.getOrNull(r - 2) == 'a' && s.getOrNull(r - 1) == 'b' && s.getOrNull(r) == 'c') lastAbc = r - 2
                result += r - lastAbc
            }
            return result.toInt()
        }

        /**
         * ## Reference (lt-ref) — count by right endpoint ("last blocker" scan)
         *
         * **Restatement.** Over a string of 'a'/'b'/'c', count the pairs `(l, r)` with `l <= r` such that the slice
         * `s[l..r]` does not contain the three-letter block "abc" anywhere inside it.
         *
         * **Pattern.** *Count substrings by their right endpoint*, a close relative of the sliding window. Fix the end
         * `r` and ask how many starts `l` give a valid substring. Validity is monotone in `l`: if `s[l..r]` is valid,
         * then any shorter suffix `s[l'..r]` with `l' > l` is valid too, because removing characters can't create
         * "abc". So the valid starts for a given `r` form one contiguous range `(blocker, r]`, and you only need the
         * left boundary of that range.
         *
         * ### Intuition
         * A substring ending at `r` is invalid exactly when it fully contains some occurrence of "abc", meaning an
         * occurrence starting at `i` with `l <= i` and `i + 2 <= r`. Only the **latest** such occurrence matters: once
         * `l` is past it, `l` is also past every earlier one. Let `last` be the start of the most recent "abc" that ends
         * at or before `r` (or `-1` if there is none). The valid starts are `last + 1 .. r`, which is `r - last` of them.
         * Sum that over every `r`.
         *
         * ### Core concepts
         * - **Monotone validity / downward-closed property.** If a window is valid, every sub-window is valid too.
         *   This is the precondition for every "count windows ending at r" or two-pointer counting technique, and it
         *   holds here because a forbidden *substring* can only disappear when you shrink.
         * - **Counting by right endpoint.** Every substring has exactly one right end, so `Σ_r (#valid starts for r)`
         *   counts each substring exactly once, with no double counting and no enumeration.
         * - **Blocker index (the "last bad position").** Each forbidden pattern acts as a wall, and the nearest wall
         *   to the left fixes the answer for `r`. This is the same idea as "substrings without a repeated char" (last
         *   seen index) or "subarrays with all elements ≤ k" (last violating index).
         * - **Fixed-length pattern ⇒ O(1) detection.** "abc" has length 3, so checking `s[r-2..r]` at each step finds
         *   every occurrence the moment it is completed. No KMP or automaton is needed.
         *
         * ### Approach
         * 1. `last = -1` (no "abc" seen yet), `total = 0`.
         * 2. For each `r` from `0` to `n-1`:
         *    - if `r >= 2` and `s[r-2..r] == "abc"`, set `last = r - 2` (the occurrence just completed);
         *    - add `r - last` to `total`. With no blocker this is `r + 1`, meaning every start `0..r`.
         * 3. Return `total`.
         *
         * Example `"abc"`: r=0 → 1, r=1 → 2, r=2 → blocker at 0, so 2 - 0 = 2. Total 5.
         *
         * ### Complexity
         * O(n) time / O(1) space.
         * - Time: one left-to-right pass with a constant-size window check at each index.
         * - Space: two scalars (`last`, `total`). No lists of ranges and no substring copies.
         *
         * ### Common pitfalls
         * - **Off-by-one on the blocker.** The occurrence starting at `i` blocks starts `l <= i`, so the valid range is
         *   `i+1 .. r` (count `r - i`), not `i .. r`. Also, an occurrence only counts once it is *complete*
         *   (`i + 2 <= r`). Before that, `"ab"` ending at `r` is still valid.
         * - **Overlap / adjacency.** Occurrences can't overlap ("abc" has no self-overlap), but they can touch
         *   ("abcabc"). The scan handles that naturally, while a "split into segments" approach must be careful with
         *   shared boundaries.
         * - **Overflow.** With no "abc" the answer is `n(n+1)/2`, which is about 5·10⁹ for `n = 10^5`, above
         *   `Int.MAX_VALUE`. Accumulate in a `Long`. (The `Int` return type here only holds for small inputs. On
         *   LeetCode, use a 64-bit return type if the signature has one.)
         * - **Index bounds.** Guard `r >= 2` before reading `s[r-2]`.
         */
        fun referenceSolution(s: String): Int {
            var last = -1          // start index of the latest complete "abc", -1 if none
            var total = 0L         // Long: n(n+1)/2 overflows Int for n = 10^5
            for (r in s.indices) {
                if (r >= 2 && s[r - 2] == 'a' && s[r - 1] == 'b' && s[r] == 'c') last = r - 2
                total += r - last  // valid starts for this end: last+1 .. r
            }
            return total.toInt()
        }

    }
}
