package leetcode.array_string

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 1768. Merge Strings Alternately  (https://leetcode.com/problems/merge-strings-alternately/)
 *
 * You are given two strings word1 and word2. Merge them by adding letters in
 * alternating order, starting with word1. If one string is longer than the
 * other, append the additional letters onto the end of the merged string.
 *
 * Constraints:
 * - 1 <= word1.length, word2.length <= 100
 * - word1 and word2 consist of lowercase English letters.
 */
typealias I1768 = (String, String) -> String

class I1768mergeAlternately {

    @Nested
    inner class Solution : ProblemTest<I1768> {

        override val cases = testCases<I1768>(
            args("abc", "pqr") expects "apbqcr",
            args("ab", "pqrs") expects "apbqrs",
            args("abcd", "pq") expects "apbqcd",
            args("a", "b") expects "ab",
        )

        @Test
        fun test() = check(::mergeAlternately)

        /**
         * Analysis of this solution (verified: all 4 cases pass).
         *
         * Pattern: parallel two-pointer merge — the merge step of merge sort. A single
         * shared index `i` walks both strings at once; `getOrNull(i)` returns null past
         * the end of the shorter word, so the longer word's tail is appended naturally.
         * That null-check is what implements the "append the remainder" rule for free —
         * no separate "drain the leftovers" loop is needed.
         *
         * Time: O(n + m), where n = word1.length, m = word2.length.
         *   The loop runs max(n,m) times (`0..maxIndx`), doing up to two `getOrNull` +
         *   `StringBuilder.append` per iteration. `append` is amortized O(1) (the builder
         *   doubles its backing array), so total work is linear in the combined length.
         * Space: O(n + m) for the output string. The StringBuilder's internal buffer is
         *   the only working storage, also O(n+m), and it becomes the result via
         *   `toString()`. No recursion -> no call-stack growth, and no per-character
         *   garbage (contrast the immutable `+=` version, which would be O((n+m)^2) and
         *   allocate a fresh string each step).
         *
         * Correctness: sound. Appending word1 then word2 every iteration gives the required
         *   "start with word1" alternation; `getOrNull` cleanly handles unequal lengths and
         *   the guaranteed-non-empty inputs (constraints say length >= 1). Looping to
         *   `maxOf(lastIndex, lastIndex)` rather than `minOf` is exactly what lets the
         *   longer word's tail come through.
         *
         * Alternative approach (also O(n+m) time): interleave only the common prefix of
         *   length min(n,m) with a tight loop, then `append` the remaining `substring`
         *   of whichever word is longer in one shot. Drops the per-char getOrNull branch
         *   in favor of one bulk copy — marginally faster constant factor, slightly more
         *   code. Your version is already asymptotically optimal; you must touch every
         *   character at least once, so O(n+m) is the floor.
         *
         * Parallelism: embarrassingly parallel in principle — every output index is a pure
         *   function of the inputs (word1[i] -> position 2i, word2[i] -> position 2i+1 in
         *   the interleaved prefix, tail after), so it maps onto a preallocated CharArray
         *   with no data dependencies. Not worth it here: at length <= 200, thread/SIMD
         *   setup overhead dwarfs the work. Honest verdict — parallelizable, not beneficial
         *   at this scale.
         *
         * Real-world: stream/data interleaving and building delimited output. The durable
         *   lesson is the one you already applied — reach for a builder/buffer
         *   (StringBuilder, byte buffer, rope) when assembling strings in a loop. Immutable-
         *   string languages (Kotlin/Java/Python/JS) all punish the naive `+=`-in-a-loop
         *   with quadratic copying; the StringBuilder here sidesteps exactly that footgun.
         */
        fun mergeAlternately(word1: String, word2: String): String {
            val maxIndx = maxOf(word1.lastIndex, word2.lastIndex)
            val result = StringBuilder()

            for (i in 0..maxIndx) {
                word1.getOrNull(i)?.let { result.append(it) }
                word2.getOrNull(i)?.let { result.append(it) }
            }
            return result.toString()
        }

    }
}
