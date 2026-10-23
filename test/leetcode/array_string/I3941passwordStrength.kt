package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 3941. Password Strength  (https://leetcode.com/problems/password-strength/)
 *
 * You are given a string `password`.
 *
 * The strength of the password is calculated based on the following rules:
 * - 1 point for each distinct lowercase letter ('a' to 'z').
 * - 2 points for each distinct uppercase letter ('A' to 'Z').
 * - 3 points for each distinct digit ('0' to '9').
 * - 5 points for each distinct special character from the set "!@#$".
 *
 * Each character contributes at most once, even if it appears multiple times.
 *
 * Return an integer denoting the strength of the password.
 *
 * Constraints:
 * - 1 <= password.length <= 10^5
 * - password consists of lowercase and uppercase English letters, digits, and special characters from "!@#$".
 */
typealias I3941 = (String) -> Int

class I3941passwordStrength {

    @Nested
    inner class Solution : ProblemTest<I3941> {

        override val cases = testCases<I3941>(
            "aA1!" expects 11,
            "bbB11#" expects 11,
            "a" expects 1,                                // single char, minimum length
            "$" expects 5,                                // single special char
            "zzzzzzzz" expects 1,                         // all equal -> counted once
            "aAbB" expects 6,                             // 'a' vs 'A' are distinct chars, different weights
            "!@#$$#@!" expects 20,                        // every special, each twice
            "0123456789" expects 30,                      // every digit (range ends '0'/'9')
            ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$".repeat(3)) expects 128, // max possible score
        )

        @Test
        fun test() = check(::passwordStrength)

        /**
         * ## Analysis (validated — all 9 cases pass)
         *
         * **Pattern:** dedupe-then-score — "count distinct elements by category" via a seen-set.
         * `Set.add` returning `false` for an already-present element is the idiomatic "first time
         * seen?" test, and it fuses the membership check and the insert into one hash lookup.
         *
         * ### Time — O(n)
         * - `password.forEach` visits each of the n chars once; `index.add(char)` is O(1) amortized
         *   (hashing a boxed `Char`).
         * - The `when` body runs only for *first* occurrences — at most 66 times (26 + 26 + 10 + 4),
         *   because the early `return@forEach` skips repeats. So `setOf('!', '@', '#', '$')`
         *   in the special-char branch is allocated at most ~66 times, not n times: harmless, but
         *   a hidden per-iteration allocation worth noticing. Hoisting it to a `val` (or writing
         *   `'!', '@', '#', '$' ->` as a multi-value `when` branch) removes it.
         *
         * ### Space — O(1)
         * - `index` holds at most 66 distinct chars regardless of n — bounded by the alphabet, so
         *   O(1) (or O(σ), σ = alphabet size, if you want to be precise). No recursion.
         *
         * ### Correctness notes
         * - Distinctness is per *character*, not per category: `'a'` and `'A'` are different chars,
         *   each scores once (`"aAbB"` → 6). The set keys on the raw `Char`, which is exactly right.
         * - Range checks `in 'a'..'z'` are inclusive at both ends — covered by the `"0123456789"` and
         *   full-alphabet cases.
         * - Max score is 26·1 + 26·2 + 10·3 + 4·5 = 128, so `Int` can never overflow.
         * - The `when` has no `else`; per the constraints every char hits a branch, and any stray
         *   char would just score 0 — a sensible default.
         *
         * ### Alternatives
         * - **Fixed-size boolean array / bitmask** (`BooleanArray(128)` indexed by `char.code`, or
         *   two `Long` bitmasks): same O(n)/O(1), but no boxing and no hashing — the classic choice
         *   when the alphabet is small and known. E.g. one bit per char, then sum weights over the
         *   set bits.
         * - **Collection pipeline:** `password.toSet().sumOf { weight(it) }` — the same algorithm in
         *   one line; clearer, slightly more allocation. For an easy problem, readability usually wins.
         * - Asymptotically optimal already: every char must be read at least once → Ω(n).
         *
         * ### Parallelism
         * Technically embarrassingly parallel — split the string into chunks, build a per-chunk
         * seen-bitmask, OR the masks together, score once (bitwise OR is associative, so it's a clean
         * map/reduce). In practice not worth it: n ≤ 10^5 is ~100 KB, a single core scans that in tens
         * of microseconds, and thread start-up costs more. The more realistic speedup is SIMD
         * (vectorized byte classification), which is what JVM intrinsics / native string libs do anyway.
         * A cute serial shortcut: once all 66 chars are seen you could stop early.
         *
         * ### Real world
         * Real password-strength meters (e.g. zxcvbn) moved *away* from character-class scoring: it
         * rewards `P@ssw0rd!` and punishes long passphrases. They estimate guessing entropy against
         * dictionaries, keyboard patterns and leaked-password lists instead; NIST SP 800-63B likewise
         * recommends length + breach-list checks over composition rules. The underlying technique
         * here — distinct-count by category with a bounded alphabet → bitset — is everywhere though:
         * character-class validation in parsers, `HyperLogLog` for distinct counts when the domain is
         * *not* bounded, and bitmap indexes in databases.
         */
        fun passwordStrength(password: String): Int {
            var result = 0
            val index = mutableSetOf<Char>()

            password.forEach { char ->
                if (index.add(char).not()) return@forEach
                when (char) {
                    in 'a'..'z' -> result += 1
                    in 'A'..'Z' -> result += 2
                    in '0'..'9' -> result += 3
                    in setOf('!', '@', '#', '$') -> result += 5
                }
            }

            return result
        }

    }
}
