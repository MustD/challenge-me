package leetcode.sliding_window

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4104. Count Valid Substrings II  (https://leetcode.com/problems/count-valid-substrings-ii/)
 *
 * You are given a string `s` consisting only of the characters 'a', 'b', and 'c'.
 *
 * A string is valid if it does not contain "abc" as a subsequence.
 *
 * Return the number of valid substrings of `s`.
 *
 * Substrings with different start or end indices are counted separately, even if they have the same content.
 *
 * Constraints:
 * - 1 <= s.length <= 10^5
 * - s consists only of 'a', 'b', and 'c'.
 */
typealias I4104 = (String) -> Int

class I4104countValidSubstrings {

    @Nested
    inner class Solution : ProblemTest<I4104> {

        override val cases = testCases<I4104>(
            "abac" expects 9,
            "abc" expects 5,
            "ac" expects 3,
            "a" expects 1,                        // single char, n = 1
            "cba" expects 6,                      // all three letters, wrong order -> nothing invalid
            "ccccbbbbaaaa" expects 78,            // strictly decreasing, all n(n+1)/2 valid
            "aabbcc" expects 17,                  // several invalid windows of different lengths
            "abcabc" expects 14,                  // repeated pattern, overlapping invalid windows
            "acbc" expects 9,                     // 'c' before 'b' must not complete the subsequence
            "abbbbcab" expects 33,                // long b-run between a and c, trailing valid tail
            "aaabbbccc" expects 36,               // runs of each letter, many windows invalid only at full span
            "abcc" expects 8,                     // invalid window at the very start, extended by a c-run
            "bbbbcaaaab" expects 55,              // no 'c' after any 'a' -> all valid
            "cabcabcbacbbacacbcabcabbcacbacbcaabcbbca" expects 174, // larger mixed input (n = 40)
        )

        @Test
        fun test() = check(
            ::countValidSubstrings,
            ::referenceSolution,
        )


        fun countValidSubstrings(s: String): Int {
            var result = 0L
            var lastA = -1
            var lastAB = -1
            var lastABC = -1
            s.indices.forEach { r ->
                when (s[r]) {
                    'a' -> lastA = r
                    'b' -> lastAB = lastA
                    'c' -> lastABC = lastAB
                }
                result += r - lastABC
            }
            return result.toInt()
        }

        /**
         * ## Reference (lt-ref) — count substrings by right endpoint, monotone left boundary
         *
         * **Restatement.** Over a string of 'a'/'b'/'c', count the substrings `s[l..r]` in which you can *not* pick
         * an 'a', then a later 'b', then a later 'c'. Every `(l, r)` pair counts separately.
         *
         * **Pattern.** "Count substrings by right endpoint": for each `r`, find how many starts `l` make `s[l..r]`
         * good, and sum. It works whenever the property is **monotone** — here, if `s[l..r]` contains "abc", so does
         * every wider window — because then the bad starts form a prefix `0..cut` and the good ones a suffix
         * `(cut, r]`. Only the cut has to be tracked, and it only moves right as `r` grows.
         *
         * ### Intuition
         * `s[l..r]` contains "abc" exactly when there is an 'a' at index `≥ l` followed by a 'b' followed by a 'c'
         * (all `≤ r`). So the cut is "the latest 'a' that already has a 'b' and then a 'c' after it". That is
         * built in three layers, each fed by the one below:
         * - `lastA` — latest 'a' seen;
         * - `lastAB` — latest 'a' with a 'b' after it (a new 'b' upgrades the current `lastA`);
         * - `lastABC` — latest 'a' with "bc" after it (a new 'c' upgrades the current `lastAB`).
         *
         * For right end `r`, starts `0..lastABC` are invalid and `lastABC+1..r` are valid: `r - lastABC` of them.
         *
         * ### Core concepts
         * - **Monotone (extension-closed) property** — if a window has it, every superset window has it too. It
         *   turns "test each window" into "find one boundary per endpoint".
         * - **Counting by right endpoint** — sum over `r` of the number of valid `l`; each substring is counted
         *   exactly once, by its own right end.
         * - **Subsequence automaton / layered "latest start" pointers** — a pattern of length `k` is tracked by `k`
         *   pointers, layer `i` = latest start from which the first `i` pattern chars are matched. Taking the
         *   *latest* start is greedy-optimal because a later start is valid for more windows.
         * - **Complement counting** — counting "valid" is the same as `total − invalid`; here counting valid
         *   directly per `r` is just as easy and needs no total.
         *
         * ### Approach
         * 1. `lastA = lastAB = lastABC = -1` (`-1` = "none yet", which makes `r - (-1) = r + 1` = all starts valid).
         * 2. For each `r`: on 'a' set `lastA = r`; on 'b' set `lastAB = lastA`; on 'c' set `lastABC = lastAB`.
         * 3. Add `r - lastABC` to the answer (a `Long`).
         * 4. Return the sum.
         *
         * Trace `"abac"`: r=0 'a' → +1; r=1 'b' (lastAB=0) → +2; r=2 'a' → +3; r=3 'c' (lastABC=0) → +3; total 9.
         *
         * ### Complexity
         * O(n) time / O(1) space.
         * - Time: one pass, constant work per character; Ω(n) is a lower bound since any unread char could be the
         *   'c' that breaks a window.
         * - Space: three indices and an accumulator.
         *
         * ### Common pitfalls
         * - **Overflow.** The answer can reach `n(n+1)/2 ≈ 5·10^9` (e.g. all 'a'), which exceeds `Int`. Accumulate in
         *   `Long`. The `I4104` typealias returns `Int`, so this function narrows at the end — correct for every
         *   test here, but for the full constraint range the real return type must be `Long`.
         * - **Update order of the layers.** Each char updates only its own layer from the layer below, so it can't
         *   cascade within one step — 'a' must not touch `lastAB`, 'c' before any 'b' must leave `lastABC` at `-1`
         *   (`"acbc"`).
         * - **Using the *first* 'a' instead of the latest.** The cut must be the *largest* invalid start; anchoring
         *   on an earlier 'a' under-counts invalid windows.
         * - **Off-by-one on the count.** Valid starts are `lastABC+1..r`, i.e. `r - lastABC`, not `r - lastABC + 1`;
         *   the `-1` sentinel makes the no-match case come out as `r + 1` automatically.
         * - **Re-scanning windows.** Enumerating all windows and testing each is O(n²)–O(n³) — far too slow at
         *   `n = 10^5`; monotonicity is what removes the inner loop.
         */
        fun referenceSolution(s: String): Int {
            var lastA = -1
            var lastAB = -1
            var lastABC = -1
            var count = 0L
            for (r in s.indices) {
                when (s[r]) {
                    'a' -> lastA = r
                    'b' -> lastAB = lastA
                    'c' -> lastABC = lastAB
                }
                count += r - lastABC
            }
            return count.toInt()
        }


    }
}
