package leetcode.array_string

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 345. Reverse Vowels of a String  (https://leetcode.com/problems/reverse-vowels-of-a-string/)
 *
 * Given a string s, reverse only all the vowels in the string and return it. The vowels are
 * 'a', 'e', 'i', 'o', and 'u', and they can appear in both lower and upper cases, more than once.
 *
 * Constraints:
 * - 1 <= s.length <= 3 * 10^5
 * - s consists of printable ASCII characters.
 */
typealias I0345 = (String) -> String

class I0345reverseVowels {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0345> {

        override val cases = leetcode.testCases<I0345>(
            "IceCreAm" expects "AceCreIm",
            "leetcode" expects "leotcede",
            "a" expects "a",       // single vowel — unchanged
            "bcd" expects "bcd",   // no vowels — unchanged
            "aA" expects "Aa",     // case is preserved on swap
        )

        @Test
        fun test() = check(::reverseVowels)

        /**
         * Analysis (verified: passes all 5 cases).
         *
         * Pattern: two-pointer converging (opposite-ends) on a mutable char array.
         * `left` walks forward, `right` walks backward; when both land on vowels we
         * swap them. This is the same template used for in-place reversal / palindrome
         * checks — here filtered to only the vowel positions.
         *
         * Time:  O(n). Each iteration advances `left`, `right`, or both — at least one
         *        pointer moves every loop, and they only converge, so the total number
         *        of iterations is bounded by n. The `vowels` set gives O(1) membership
         *        checks (HashSet of 10 chars). `s.toCharArray()` and `String(result)`
         *        are each O(n). Overall O(n).
         * Space: O(n) auxiliary for the char-array copy (Strings are immutable in
         *        Kotlin/JVM, so an in-place swap requires a mutable copy) plus the
         *        returned String. The `vowels` set is O(1) (fixed 10 entries). No
         *        recursion, so no stack cost.
         *
         * Correctness / edge cases handled:
         * - Single char / no vowels: `left < right` plus the no-swap branches just walk
         *   the pointers inward without mutating ("a", "bcd" cases).
         * - Case preserved: swapping moves the actual chars, so 'A' and 'a' keep their
         *   case and only trade positions ("aA" -> "Aa").
         * - Both upper- and lower-case vowels are in the set, so mixed case works.
         *
         * Note on the loop shape: after a both-vowels swap you do `left++; right--`, then
         * the two independent `if`s skip any non-vowels. It's correct, but the more common
         * idiom is to skip non-vowels in two inner while-loops *first*, then swap once and
         * advance both — that makes the "advance vs. swap" intent more explicit and avoids
         * re-reading result[left]/result[right] after mutating them. Same O(n)/O(n).
         *
         * Alternatives:
         * - Collect vowel indices, reverse that list, write back: also O(n)/O(n) but two
         *   passes and an extra index list — strictly worse than the single-pass two-pointer.
         * - This is already optimal: every char must be inspected at least once, so O(n)
         *   time is a hard lower bound; O(n) space is forced by String immutability (a
         *   language built on mutable strings could do it truly in place at O(1) extra).
         *
         * Parallelism: not worth it. n <= 3*10^5 is tiny, and the swap pairing is a global
         * data dependency (the k-th vowel from the left pairs with the k-th from the right).
         * You *could* parallelize as: (1) parallel scan to find vowel positions, (2) pair
         * up symmetric indices and swap independently — the swaps themselves are embarrassingly
         * parallel once positions are known. But thread/SIMD setup overhead dwarfs the work at
         * this size; the sequential single pass wins. SIMD vowel-detection (compare-against-set)
         * is the only piece that'd realistically help, and only on much larger inputs.
         *
         * Real-world: this "reverse/transform only the elements matching a predicate, in place"
         * shape shows up in text processing, in-place buffer editing, and stream filtering. At
         * production scale you'd reach for a library (regex / vectorized string ops) and the
         * input is often streamed or chunked rather than a single bounded array, which breaks the
         * neat two-ends-meet trick and pushes you toward the index-collection or buffered approach.
         */
        fun reverseVowels(s: String): String {
            val result = s.toCharArray()
            var left = 0
            var right = result.lastIndex
            val vowels = setOf('a', 'e', 'i', 'o', 'u', 'A', 'E', 'I', 'O', 'U')

            while (left < right) {
                val leftChar = result[left]
                val rightChar = result[right]
                if (vowels.contains(leftChar) && vowels.contains(rightChar)) {
                    result[left] = rightChar
                    result[right] = leftChar
                    left++
                    right--
                }
                while (left < right && vowels.contains(result[left]).not()) left++
                while (left < right && vowels.contains(result[right]).not()) right--
            }

            return String(result)

        }

    }
}
