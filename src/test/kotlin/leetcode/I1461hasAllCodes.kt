package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 1461. Check If a String Contains All Binary Codes of Size K
 * (https://leetcode.com/problems/check-if-a-string-contains-all-binary-codes-of-size-k/)
 *
 * Given a binary string `s` and an integer `k`, return `true` if every binary code of length `k`
 * (there are 2^k of them, from "00…0" to "11…1") appears as a substring of `s`. Otherwise return `false`.
 *
 * Constraints:
 * - 1 <= s.length <= 5 * 10^5
 * - s[i] is either '0' or '1'
 * - 1 <= k <= 20
 * - Note: if s.length < k, not even one code of length k fits, so the answer must be false.
 *   (There are exactly s.length - k + 1 substrings of length k; you need all 2^k distinct ones.)
 */
typealias I1461 = (String, Int) -> Boolean

class I1461hasAllCodes {

    @Nested
    inner class Solution : ProblemTest<I1461> {

        override val cases = testCases<I1461>(
            args("00110110", 2) expects true,   // codes "00","01","10","11" all present
            args("0110", 1) expects true,        // codes "0","1" both present
            args("0110", 2) expects false,       // missing "00" (and "11")
            args("0000000001011100", 4) expects false,
            args("0", 1) expects false,          // only "0" present, "1" missing
            args("1", 20) expects false,         // string shorter than k
        )

        @Test
        fun test() = check(::hasAllCodes, ::referenceSolution)

        /**
         * ## Analysis — `hasAllCodes` (verified against all 6 cases)
         *
         * ### Pattern
         * Fixed-size **sliding window** over the string, feeding every length-`k` window into a
         * **hash set** to count distinct substrings. Classic "distinct k-length substrings" template.
         * The trick that makes the check cheap: there are only `2^k` possible codes of length `k`,
         * so "all codes present" ⇔ "the number of *distinct* length-`k` windows equals `2^k`".
         *
         * ### Time — O(n·k)   (n = s.length)
         * The `while` loop runs `n - k + 1` times. Each iteration does two O(k) operations:
         * - `s.substring(lo..hi)` allocates a fresh k-char string, and
         * - `combinations.add(...)` hashes/compares that k-char string.
         *
         * So O((n − k + 1)·k) = O(n·k). With n ≤ 5·10^5 and k ≤ 20 that is ~10^7 char-ops — fine.
         *
         * ### Space — O(min(2^k, n)·k)
         * The set holds at most `min(2^k, n − k + 1)` distinct strings, each of length k. For k = 20
         * that is up to ~10^6 twenty-char strings plus per-String/HashSet object overhead (tens of MB).
         *
         * ### Correctness notes
         * - `k > s.length` guard returns false early — no window of length k exists (matches case `("1", 20)`).
         * - `combinations.size >= count` is safe: the set can never exceed `2^k` distinct codes, so `>=`
         *   behaves like `==` here. (`==` would read more directly as intent.)
         * - **Minor smell:** `Math.powExact(2L, k)` is computed *before* the `k > s.length` guard. Harmless
         *   under the given constraints (2^20 fits in a Long), but for k ≥ 63 `powExact` would throw
         *   `ArithmeticException` on overflow before you ever reach the length check. Cheap to reorder the
         *   two lines so the guard runs first.
         *
         * ### Alternative — rolling hash + bitset (the "optimal" answer)  → O(n) time, O(2^k) *bits*
         * Treat each length-k window as a k-bit integer and roll it in O(1):
         * `hash = ((hash shl 1) or bit) and mask`, where `mask = (1 shl k) - 1`. Mark
         * `seen[hash] = true` in a `BooleanArray(1 shl k)` (or a bitset) and keep a `found` counter;
         * short-circuit to true once `found == 2^k`. This removes the O(k) per-window substring
         * allocation/hashing (→ O(n) time) and replaces the heavyweight String hash set with ~`2^k`
         * bits (~1 Mbit ≈ 128 KB for k = 20 vs. tens of MB here). Same asymptotic space class, far
         * smaller constant. Your version is clearer to read; the rolling-hash version is what you'd
         * reach for at the upper end of the constraints.
         *
         * ### Parallelism — not worth it here
         * The window scan is *nearly* embarrassingly parallel (partition s into chunks, dedup each chunk
         * into a local set, union the sets — a map/reduce), and the bitset variant parallelizes even more
         * cleanly (each thread ORs into a shared/merged `BooleanArray`, and boolean-OR is associative). But
         * n·k ≈ 10^7 is far below the point where thread-spawn + merge overhead pays off — a single core
         * finishes in single-digit milliseconds. The teaching point: recognize the *shape* is parallelizable
         * (independent windows, associative merge) even when the input is too small to bother.
         *
         * ### Real-world
         * "Count distinct fixed-length substrings" is **k-mer counting** in bioinformatics — genome
         * assembly / De Bruijn graphs work over billions of bases, where nobody stores raw strings in a
         * HashSet; they use rolling (Rabin) hashes plus probabilistic structures (Bloom filters,
         * Count-Min sketch) to fit in memory and trade exactness for space. The same rolling-window +
         * fingerprint idea powers content-defined chunking in dedup/rsync. This problem is really asking
         * whether `s` covers a De Bruijn sequence B(2, k).
         */
        fun hasAllCodes(s: String, k: Int): Boolean {
            val count = Math.powExact(2L, k)
            if (k > s.length) return false
            val combinations = mutableSetOf<String>()

            var lo = 0
            var hi = k - 1

            while (hi <= s.lastIndex) {
                combinations.add(s.substring(lo..hi))
                lo++; hi++
            }

            return combinations.size >= count
        }

        /**
         * ## Reference — `referenceSolution`: rolling hash + bitset  → O(n) time, O(2^k) bits
         *
         * ### Restatement
         * Does `s` contain **every** one of the `2^k` binary strings of length `k` as a substring?
         * Equivalently: do the `n − k + 1` length-`k` windows of `s` cover all `2^k` distinct codes?
         *
         * ### The insight that unblocks the "optimal" version
         * A length-`k` binary window **is** a `k`-bit integer — so there is no need to build/hash a
         * `String` per window at all. Slide the window one char to the right by:
         * ```
         * hash = ((hash shl 1) or bit) and mask      // mask = (1 shl k) - 1 keeps the low k bits
         * ```
         * The `shl 1` shifts the old bits up, `or bit` appends the new char, and `and mask` drops the
         * bit that just fell off the left. That is **O(1) per window** instead of O(k) to cut+hash a
         * substring — this is the difference between your O(n·k) and the O(n) optimum.
         *
         * ### Approach
         * 1. `need = 1 shl k` (that's `2^k`; fits in an `Int` since `k ≤ 20`).
         * 2. Guard `s.length < k` → false (no full window exists — the `("1", 20)` case).
         * 3. Walk `i` over the string, rolling `hash`. A window is complete once `i >= k - 1`.
         * 4. Track seen codes in a flat `BooleanArray(need)` (a bitset by index — no hashing, no boxing)
         *    and a `found` counter; short-circuit to `true` the moment `found == need`.
         *
         * ### Complexity
         * - **Time O(n):** one pass, O(1) roll + O(1) array probe per char.
         * - **Space O(2^k) bits:** `BooleanArray(2^k)` — ~1 M booleans (~1 MB) at k = 20, versus the
         *   tens of MB of twenty-char `String`s the HashSet version holds. Same asymptotic class as
         *   `min(2^k, n)` here, but a far smaller constant and no per-object overhead.
         *
         * ### Pitfalls this version sidesteps / to watch for
         * - **Overflow:** `1 shl k` stays in Int for k ≤ 20; the HashSet version's `Math.powExact(2L, k)`
         *   would throw for k ≥ 63 — and note it runs *before* the length guard there. Here the guard is first.
         * - **Off-by-one on window start:** only record once `i >= k - 1`, i.e. after the first full window;
         *   recording earlier would count partial windows as codes.
         * - **`shl` vs arithmetic:** masking every step is what bounds the value to k bits — forget the
         *   `and mask` and stale high bits leak in and corrupt the code.
         */
        fun referenceSolution(s: String, k: Int): Boolean {
            if (s.length < k) return false          // not even one length-k window fits
            val need = 1 shl k                      // 2^k distinct codes to cover (Int-safe for k ≤ 20)
            val mask = need - 1                      // low k bits
            val seen = BooleanArray(need)
            var hash = 0
            var found = 0

            for (i in s.indices) {
                hash = ((hash shl 1) or (s[i] - '0')) and mask   // O(1) roll of the window
                if (i >= k - 1 && !seen[hash]) {                 // window is full and code is new
                    seen[hash] = true
                    if (++found == need) return true             // all codes covered — short-circuit
                }
            }
            return found == need
        }

    }
}
