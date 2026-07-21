package leetcode.divide_conquer

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 1763. Longest Nice Substring  (https://leetcode.com/problems/longest-nice-substring/)
 *
 * A string is "nice" if, for every letter of the alphabet it contains, that letter appears in BOTH
 * uppercase and lowercase form. Given a string s, return the longest substring of s that is nice.
 * If there are several equally-long nice substrings, return the one that occurs earliest. If there
 * are none, return the empty string.
 *
 * Constraints:
 * - 1 <= s.length <= 100
 * - s consists of uppercase and lowercase English letters only.
 * - "Longest, earliest" tie-break: on equal length, prefer the smaller starting index.
 * - No nice substring => return "" (a single character can never be nice).
 */
typealias I1763 = (String) -> String

class I1763longestNiceSubstring {

    @Nested
    inner class Solution : leetcode.ProblemTest<I1763> {

        override val cases = leetcode.testCases<I1763>(
            "YazaAay" expects "aAa",
            "Bb" expects "Bb",
            "c" expects "",
            "dDzeE" expects "dD",   // "dD" and "eE" both length 2 -> earliest wins
            "cChH" expects "cChH",
        )

        @Test
        fun test() = check(::longestNiceSubstring, ::referenceSolution)

        /**
         * ## Verdict: correct. Verified by `./gradlew test --tests "leetcode.I1763longestNiceSubstring"`.
         *
         * This is the solution wired first into `check(...)`; `referenceSolution` below is the same
         * algorithm and its KDoc explains the *why* of the pattern. This block analyzes what YOUR
         * code actually does and what it costs.
         *
         * ## Pattern: Divide & Conquer on a "split point"
         * The insight you used: a character whose opposite-case partner is absent from the whole
         * string (line 42-43) can never lie inside a nice substring, so it partitions the problem —
         * the answer is entirely left of it or entirely right of it. Recurse on both halves, exclude
         * the split char, return the longer (line 44-46). If no split char exists, the string is
         * already nice and you return it whole (line 49). This is the canonical D&C shape:
         * find a separator, solve the independent sub-ranges, combine by picking the better.
         *
         * ## Correctness notes
         * - **Base case (line 39):** `s.length < 2` returns `""`. A single char has no partner and can
         *   never be nice, so this also covers the `"c" -> ""` case and terminates the recursion.
         * - **Set built from the WHOLE current substring (line 40)** before scanning — this is the load
         *   -bearing detail. Partner-membership is asked against every char in range, which is exactly
         *   what makes the split decision sound (checking against a running/partial set would be the
         *   sliding-window bug demonstrated by `wrongLongestNiceSubstring`).
         * - **Tie-break (line 46):** `if (left.length >= right.length) left else right` keeps `left` on
         *   equal lengths. Since `left` is the earlier-starting range, this encodes "longest, then
         *   earliest" and is why `"dDzeE" -> "dD"` passes. (Note: mathematically equivalent to the
         *   reference's `if (right.length > left.length) right else left` — both hand ties to the left.)
         *
         * ## Complexity — tied to your code
         * - **Time: O(n^2) worst case, O(n log n) best.** Per call you build the set (O(n), line 40) and
         *   scan (O(n), line 41), so each frame is O(n) plus the substring copies. A degenerate string
         *   (split char peeled one at a time, e.g. every char has a present partner except a lone one at
         *   the end) gives O(n) recursion depth × O(n) per level = O(n^2). A balanced split gives the
         *   classic T(n)=2T(n/2)+O(n) → O(n log n). With n <= 100 either is instant.
         * - **Space: O(n) auxiliary.** The char set is O(n) and lives once per active frame; recursion
         *   depth is O(n) worst case, so the live stack path is O(n). Additionally `substring` (lines
         *   44-45) allocates fresh copies at every level — total transient allocation trends toward
         *   O(n^2), which an index-range variant (passing lo/hi instead of copying) would avoid.
         *
         * ## Alternatives & trade-offs
         * - **Bitmask brute force:** for each of the O(n^2) substrings, track two 26-bit masks (seen
         *   lowercase / seen uppercase) while extending; a window is nice iff the masks are equal.
         *   O(n^2) time, O(1) space, no recursion — arguably simpler to reason about and strictly better
         *   on space than your D&C. Your D&C wins on *average* (O(n log n)) and reads more elegantly.
         * - **Sliding window: does NOT apply** — niceness is non-monotonic ("aA" nice, "aAb" not,
         *   "aAbB" nice again), so there's no lo/hi discipline that recovers the answer. That's the
         *   whole point of `wrongLongestNiceSubstring`.
         *
         * ## Parallelism
         * Not worth it here. The D&C is embarrassingly parallel in principle — the left and right
         * recursions are independent and could fork onto separate threads (parallel divide-and-conquer,
         * like a parallel merge sort). But n <= 100 means the entire computation is sub-microsecond;
         * thread-spawn and join overhead would dwarf the work by orders of magnitude. Parallelism only
         * pays once subproblems are large enough to amortize scheduling (typically thousands+ of
         * elements per task), which this constraint never reaches.
         *
         * ## Real-world
         * The "find a separator, recurse on independent sub-ranges" shape is everywhere: quicksort's
         * partition, interval/segment splitting, parsing (split on a delimiter token that can't appear
         * inside a valid sub-expression), and log/stream chunking on record boundaries. In production the
         * two changes you'd make: (1) pass index ranges instead of `substring` to kill the O(n^2)
         * copying and GC churn, and (2) for genuinely huge inputs, the bitmask linear-ish scan or a
         * streaming pass is preferred because it's cache-friendly and allocation-free — the "optimal"
         * asymptotics matter less than avoiding per-frame allocation and pointer-chasing.
         */
        fun longestNiceSubstring(s: String): String {
            if (s.length < 2) return ""
            val chars = s.toSet()
            s.forEachIndexed { i, c ->
                val partner = if (c.isUpperCase()) c.lowercaseChar() else c.uppercaseChar()
                if (chars.contains(partner).not()) {
                    val left = longestNiceSubstring(s.substring(0, i))
                    val right = longestNiceSubstring(s.substring(i + 1))
                    return if (left.length >= right.length) left else right
                }
            }
            return s
        }

        /**
         * ## Why the sliding window below can't work
         *
         * "Nice" is NOT a monotonic property, so the two-pointer / sliding-window template does
         * not apply. With sliding window you rely on: "if [lo, hi] is bad, growing it can't fix
         * it" (or the reverse). Here neither holds — a window can be not-nice, then become nice
         * again as you add more characters (e.g. "aA" is nice, "aAb" is not, "aAbB" is nice
         * again). There is no direction in which niceness changes only once, so no lo/hi
         * discipline recovers the answer. That is what "wrong by nature" is pointing at.
         *
         * ## Pattern: Divide & Conquer
         *
         * Key observation: if a character c appears in s but its opposite-case partner does NOT
         * appear ANYWHERE in s, then c can never sit inside a nice substring. So c is a hard
         * "split point": every nice substring must lie entirely to its left or entirely to its
         * right. That gives a clean recursion.
         *
         * ## Approach
         * 1. If s has fewer than 2 chars, it can't be nice -> return "".
         * 2. Put every char of s in a set. Scan left to right; find the first char whose
         *    swapped-case partner is absent from the set — a split point.
         * 3. If there is none, the whole string is already nice -> return s.
         * 4. Otherwise recurse on the left part and the right part (excluding the split char) and
         *    return the longer result. On a tie prefer the LEFT (earlier) — so the right side
         *    only wins when it is strictly longer. That encodes "longest, then earliest".
         *
         * ## Complexity
         * - Time: O(n^2) worst case — each level scans O(n), and a bad split (one char peeled off
         *   at a time) gives O(n) levels. n <= 100 so this is trivially fast.
         * - Space: O(n) for the char set + O(n) recursion depth.
         *
         * ## Pitfalls
         * - Tie-break direction: use `if (right.length > left.length) right else left` so equal
         *   lengths keep the earlier (left) substring. Flipping the comparison silently fails the
         *   "dDzeE" -> "dD" case.
         * - A single character is never nice (no partner) — the length < 2 base case handles it
         *   and covers the "c" -> "" case.
         * - Build the set from the WHOLE current substring before scanning; checking partners
         *   against a running/partial set reintroduces the sliding-window bug.
         */
        fun referenceSolution(s: String): String {
            if (s.length < 2) return ""
            val chars = s.toHashSet()
            for (i in s.indices) {
                val c = s[i]
                val partner = if (c.isUpperCase()) c.lowercaseChar() else c.uppercaseChar()
                if (partner !in chars) {
                    val left = referenceSolution(s.substring(0, i))
                    val right = referenceSolution(s.substring(i + 1))
                    return if (right.length > left.length) right else left
                }
            }
            return s
        }

        /**
         * Wrong by nature
         */
        fun wrongLongestNiceSubstring(s: String): String {
            if (s.isEmpty() || s.length == 1) return ""
            val string = s.toCharArray()

            var result = charArrayOf()
            val index = mutableMapOf<Char, Int>()

            fun isNice(): Boolean {
                ('a'..'z').forEach { c ->
                    val lChar = index.getOrDefault(c, 0)
                    val uChar = index.getOrDefault(c.uppercaseChar(), 0)
                    when {
                        (lChar == 0) and (uChar == 0) -> Unit
                        (lChar > 0) and (uChar > 0) -> Unit
                        else -> return false
                    }
                }
                return true
            }

            fun add(c: Char) {
                index[c] = index.getOrDefault(c, 0) + 1
            }

            fun remove(c: Char) {
                index[c] = index.getOrElse(c) { throw IllegalStateException() } - 1
            }

            var lo = 0; index[string[0]] = 1
            var hi = 1; index[string[1]] = 1

            while ((lo < hi) and (hi <= string.lastIndex)) {
                if (isNice()) {
                    if (hi - lo > result.lastIndex) result = string.sliceArray(lo..hi)
                    if (hi < string.lastIndex) {
                        hi++
                        add(string[hi])
                    } else break
                } else {
                    remove(string[lo]); lo++
                    if (hi < string.lastIndex) {
                        hi++
                        add(string[hi])
                    } else break
                }
            }
            return result.concatToString()

        }

    }
}
