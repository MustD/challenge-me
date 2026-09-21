package leetcode.sliding_window

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 424. Longest Repeating Character Replacement  (https://leetcode.com/problems/longest-repeating-character-replacement/)
 *
 * You are given a string s and an integer k. You can choose any character of the string and change it
 * to any other uppercase English character. You can perform this operation at most k times.
 *
 * Return the length of the longest substring containing the same letter you can get after performing
 * the above operations.
 *
 * Constraints:
 * - 1 <= s.length <= 10^5
 * - s consists of only uppercase English letters.
 * - 0 <= k <= s.length
 */
typealias I0424 = (String, Int) -> Int

class I0424characterReplacement {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0424> {

        override val cases = leetcode.testCases<I0424>(
            leetcode.args("ABAB", 2) expects 4,
            leetcode.args("AABABBA", 1) expects 4,
        )

        @Test
        fun test() = check(::referenceSolution)

        fun characterReplacement(s: String, k: Int): Int {
            TODO()
        }

        /**
         * ### Restatement
         * We can repaint up to `k` characters of `s` to any other uppercase letter. Find the length of the
         * longest contiguous substring we can turn into a single repeated letter.
         *
         * ### Pattern / technique
         * **Sliding window with a "budget" invariant** (variable-size two-pointer). This is the same family as
         * "longest substring with at most K distinct characters" or "minimum window substring": grow the window
         * greedily from the right, and only shrink from the left when the window becomes invalid. The trick here
         * is recognizing what "valid" means for *this* problem.
         *
         * ### Intuition
         * A window `[left, right]` of length `len` can be turned into one repeated letter using at most `k`
         * replacements if and only if `len - maxFreq <= k`, where `maxFreq` is the count of the *most frequent*
         * letter already inside the window — repaint everything else in the window to that majority letter, and
         * the number of repaints needed is exactly `len - maxFreq`. So we never need to actually know which
         * letter wins; we only need to track the best `maxFreq` seen for the current window's letters.
         *
         * A key simplification: `maxFreq` never needs to *decrease* as the window slides. If shrinking the window
         * would require a smaller `maxFreq` to stay valid, the resulting window can't be longer than the best one
         * already found, so tracking a stale (never-decreasing) `maxFreq` is safe — it can only ever let the
         * window grow again once a genuinely higher frequency reappears. That means `left` advances by at most one
         * step per invalid window, keeping the whole scan O(n).
         *
         * ### Core concepts
         * - **Sliding window invariant**: a boolean condition ("is this window achievable with <= k edits?") that
         *   the loop maintains by shrinking from the left whenever it's violated, instead of re-scanning from
         *   scratch.
         * - **Window validity via majority count**: `windowLength - maxFreqInWindow <= k` — the number of
         *   "non-majority" characters is exactly the number of replacements required.
         * - **Monotonic best-so-far (`maxFreq`)**: letting a tracked value go stale (only ever increasing, never
         *   recomputed downward) is a common trick to keep an O(n^2) sliding-window scan down to O(n), because it
         *   avoids ever having to shrink the window below its previous best length.
         *
         * ### Approach
         * 1. Keep a frequency array `count[26]` for letters currently inside `[left, right]`, and `maxFreq`, the
         *    highest count seen for any letter in any window so far.
         * 2. For each `right`, add `s[right]` to `count` and update `maxFreq = max(maxFreq, count[s[right]])`.
         * 3. If the current window is invalid (`(right - left + 1) - maxFreq > k`), shrink: decrement
         *    `count[s[left]]` and increment `left`. (At most one shrink per step, thanks to the stale-`maxFreq`
         *    argument above.)
         * 4. The answer is the largest window length seen, which — because the window only ever grows or holds
         *    its size steady — is simply `right - left + 1` at the end (or track a running max).
         *
         * ### Complexity
         * - Time: O(n) — `right` visits every index once; `left` advances at most n times total across the whole
         *   scan.
         * - Space: O(1) — a fixed 26-element frequency array regardless of input size.
         *
         * ### Common pitfalls
         * - Recomputing `maxFreq` by scanning `count[26]` on every shrink turns this into O(26n); the point of the
         *   trick is to let `maxFreq` go stale and never decrease.
         * - Off-by-one on window length: it's `right - left + 1`, not `right - left`.
         * - Forgetting that the window never actually needs to shrink below its previous best size — since
         *   `maxFreq` is monotonic non-decreasing, the loop only ever does `left++` once per `right++`, so the
         *   final window length equals the answer directly.
         */
        fun referenceSolution(s: String, k: Int): Int {
            val count = IntArray(26)
            var left = 0
            var maxFreq = 0

            for (right in s.indices) {
                val c = s[right] - 'A'
                count[c]++
                maxFreq = maxOf(maxFreq, count[c])

                if ((right - left + 1) - maxFreq > k) {
                    count[s[left] - 'A']--
                    left++
                }
            }

            return s.length - left
        }

    }
}
