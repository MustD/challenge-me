package leetcode.array_string

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4030. Check ASCII Palindromic  (https://leetcode.com/problems/check-ascii-palindromic/)
 *
 * You are given a string s consisting of lowercase English letters.
 *
 * Construct a binary string by replacing each character in s with the 8-bit binary representation of its ASCII
 * value, including leading zeros, while preserving the original order of the characters.
 *
 * Return true if the resulting binary string is a palindrome. Otherwise, return false.
 *
 * Constraints:
 * - 1 <= s.length <= 100
 * - s consists of lowercase English letters.
 */
typealias I4030 = (String) -> Boolean

class I4030isPalindromic {

    @Nested
    inner class Solution : leetcode.ProblemTest<I4030> {

        override val cases = leetcode.testCases<I4030>(
            "ff" expects true,
            "leet" expects false,
        )

        @Test
        fun test() = check(::isPalindromic, ::referenceSolution)

        fun isPalindromic(s: String): Boolean {
            val arr = s.map { it.code.toString(2).padStart(8, '0') }.joinToString("")
            return arr.reversed() == arr
        }

        /**
         * ### Intuition
         * Every character becomes a fixed-width 8-bit block, in order, so the whole thing is really just one long
         * bit string of length `8 * s.length`. "Is it a palindrome" is the classic two-pointer symmetry check —
         * bit `i` from the front must equal bit `i` from the back, for every `i` up to the midpoint. The only
         * twist is that no bit string is ever materialized: bit index `p` is decomposed back into "which character"
         * and "which bit of that character" on the fly, and read directly out of the character's ASCII code.
         *
         * ### Core concepts
         * - **Two-pointer / mirrored-index palindrome check** — instead of reversing a sequence and comparing, walk
         *   one index `i` from the start and compare it against `total - 1 - i`; stop at the midpoint. Works on
         *   any indexable sequence, real or virtual.
         * - **Index decomposition (div/mod into a fixed-width record)** — when a logical stream is really `n`
         *   fixed-size chunks laid end to end (here: 8 bits per character), a flat position `p` maps back to
         *   `chunk = p / width`, `offset = p % width`, avoiding ever building the flattened stream.
         * - **Bit extraction via shift + mask** — `(value shr k) and 1` reads a single bit out of an integer
         *   without converting it to a string first; `k` counts from the least-significant bit, so reading
         *   "bit `offset` from the most-significant end of an 8-bit value" is `(value shr (7 - offset)) and 1`.
         *
         * ### Approach
         * 1. Let `totalBits = 8 * s.length`.
         * 2. For each `i` in `0 until totalBits / 2`, compute the bit at position `i` and at its mirror position
         *    `totalBits - 1 - i`.
         * 3. To read the bit at flat position `p`: `charIndex = p / 8` picks the character, `bitOffset = p % 8`
         *    picks which of its 8 bits (0 = most significant), then extract it from `s[charIndex].code`.
         * 4. If any mirrored pair differs, return `false` immediately; otherwise return `true` once every pair
         *    up to the midpoint has matched.
         *
         * ### Complexity
         * - Time: O(n) where n = s.length (each of the 8n bits is visited at most once, and 8 is a constant).
         * - Space: O(1) extra — only integer indices are kept, no binary string is ever built.
         *
         * ### Common pitfalls
         * - Off-by-one on which end is the "most significant" bit: `padStart(8, '0')` in the naive approach puts
         *   the MSB first (leftmost), so bit offset 0 within a character must map to shift amount `7`, not `0`.
         * - Forgetting that `totalBits` can be odd... it can't be here (always a multiple of 8), but the loop
         *   bound `totalBits / 2` still needs care for interview variants where the total bit count is odd — the
         *   middle bit never needs comparing against itself.
         * - Comparing `charIndex` bounds: for `p` up to `totalBits - 1`, `charIndex` is always `< s.length`, but
         *   it's easy to accidentally use `s.length` instead of `totalBits` as the loop driver and get a
         *   half-covered check.
         */
        fun referenceSolution(s: String): Boolean {
            val totalBits = 8 * s.length

            fun bitAt(p: Int): Int {
                val charIndex = p / 8
                val bitOffset = p % 8
                return (s[charIndex].code shr (7 - bitOffset)) and 1
            }

            for (i in 0 until totalBits / 2) {
                if (bitAt(i) != bitAt(totalBits - 1 - i)) return false
            }
            return true
        }

    }
}
