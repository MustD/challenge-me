package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.math.abs
import kotlin.test.Test

/**
 * 4006. Count Valid Prefixes  (https://leetcode.com/problems/count-valid-prefixes/)
 *
 * Given a binary string `s`, determine how many of its prefixes are "valid." A prefix is valid if its characters can be
 * rearranged to form an alternating string (no two adjacent characters are equal).
 *
 * Constraints:
 * - 1 <= s.length <= 100
 * - s consists only of '0' and '1'
 */
typealias I4006 = (String) -> Int

class I4006countValidPrefixes {

    @Nested
    inner class Solution : ProblemTest<I4006> {

        override val cases = testCases<I4006>(
            "00101" expects 3,
            "101" expects 3,
            "0" expects 1,                                // single char, min length
            "1" expects 1,                                // single char, the other symbol
            "0000" expects 1,                             // all equal: only the first prefix is valid
            "1100" expects 3,                             // invalid prefix in the middle, then recovers
            "000111" expects 3,                           // imbalance of 3 must fully unwind before counting again
            "0101010101" expects 10,                      // every prefix valid
            "01".repeat(50) expects 100,                  // max length (100), all valid
            "1".repeat(100) expects 1,                    // max length, all equal
        )

        @Test
        fun test() = check(::countValidPrefixes, ::referenceSolution)

        /**
         * ## Help note (leetcode-help)
         *
         * Approach: *prefix aggregate* with two running counters. Correct and already optimal (O(n) time, O(1)
         * space), the same complexity as the reference. Nothing to fix. The only stylistic refinement is folding
         * the two counters into one signed balance (see `referenceSolution`), which removes `abs` and the array.
         *
         * ## Analysis (validated: 10/10 cases pass)
         *
         * **Key insight.** A multiset of 0s and 1s can be arranged into an alternating string iff
         * `|count0 - count1| <= 1`: the majority symbol needs a minority symbol between each pair of its copies,
         * so `major <= minor + 1`. Order inside the prefix doesn't matter, so each prefix is fully described by
         * its two counts. The check `abs(state[0] - state[1]) < 2` is exactly this condition.
         *
         * **Time — O(n).** One pass over `s.indices`; per character an O(1) array increment, one subtraction,
         * and `abs`. n <= 100, so this is trivially fast.
         *
         * **Space — O(1).** `state` is a fixed `IntArray(2)` plus `result`; no per-prefix storage.
         *
         * **Correctness notes.**
         * - The counts are cumulative, so each step's state *is* the state of prefix `s[0..i]` — no recount.
         * - Validity is not monotonic: a prefix can become invalid and later valid again (`"1100"` -> 3,
         *   `"000111"` -> 3). The code re-checks every prefix independently, so it handles this; a common bug
         *   is to `break` at the first invalid prefix.
         * - Map `char -> 0/1` relies on the constraint "only '0'/'1'"; any other char silently counts as '1'.
         * - No overflow: counts are bounded by n = 100.
         *
         * **Pattern.** *Prefix aggregate / running balance*: replace "a property of every prefix" with an
         * incrementally maintained summary (here, two counters). Same family as prefix sums and the
         * "+1 / -1 balance" trick used for balanced parentheses, "contiguous array" (LC 525), etc.
         *
         * **Alternatives.**
         * - *Single balance counter* `t += if (c == '1') 1 else -1; if (t in -1..1) result++` — same O(n)/O(1),
         *   one variable instead of two, and makes the "balance" pattern explicit. Purely a style trade-off.
         * - *Brute force* — for each prefix, recount 0s/1s: O(n^2). Fine at n = 100 but misses the point.
         * - Already optimal: every character must be read at least once (Omega(n)), and O(1) extra space is minimal.
         *
         * **Parallelism.** Not worth it here (n <= 100). In principle it's a *parallel prefix scan*: split `s`
         * into chunks, compute each chunk's net balance in parallel, exclusive-scan those offsets, then each
         * chunk counts positions where `offset + localBalance in -1..1` — O(n/p + log p). Only pays off for
         * strings in the tens of millions; below that, thread overhead dominates (Amdahl + spawn cost).
         *
         * **Real world.** Running-balance checks are everywhere: bracket/tag matching in parsers, stream
         * validation (e.g. "has the buy/sell imbalance exceeded k at any point?"), load-balancing between two
         * queues, and GPU stream compaction (which is the parallel-scan version above). In production the input
         * is often a stream, which this one-pass O(1)-state approach handles naturally — no need to buffer.
         */
        fun countValidPrefixes(s: String): Int {
            var result = 0
            val state = IntArray(2) { 0 }

            for (i in s.indices) {
                val char = s[i]
                val charI = if (char == '0') 0 else 1
                state[charI]++

                if (abs(state[0] - state[1]) < 2) result++
            }

            return result
        }

        /**
         * ## Reference solution
         *
         * **Restatement.** Walk the prefixes `s[0..0]`, `s[0..1]`, ..., `s[0..n-1]` of a binary string and count
         * how many of them could be shuffled into an alternating string like `0101...` or `1010...`.
         *
         * **Pattern: running balance (prefix aggregate).** "Can it be rearranged" means order is irrelevant, so a
         * prefix is fully described by how many 0s and 1s it has. Those counts change by exactly one per step,
         * so they can be maintained incrementally instead of recomputed for each prefix.
         *
         * ### Intuition
         * In an alternating string the two symbols interleave, so their counts differ by at most one. Conversely,
         * if `|zeros - ones| <= 1`, start with the majority symbol and alternate: it always works. So validity
         * collapses to a single number, `balance = ones - zeros`, and a prefix is valid iff `balance in -1..1`.
         *
         * ### Core concepts
         * - **Rearrangement => multiset.** When a question allows any permutation, only symbol counts matter;
         *   here that reduces each prefix to two integers.
         * - **Pigeonhole / interleaving bound.** k copies of the majority symbol need k-1 separators, so
         *   `major <= minor + 1`. This is the same bound as "reorganize string" (LC 767).
         * - **Signed balance (+1 / -1 trick).** Map one symbol to +1 and the other to -1; the running sum encodes
         *   the difference of counts. Used for parentheses matching, LC 525 "Contiguous Array", etc.
         * - **Prefix aggregate.** A property of every prefix is computed by updating a summary in O(1) per step.
         *
         * ### Approach
         * 1. `balance = 0`, `count = 0`.
         * 2. For each char: `balance += 1` for '1', `-= 1` for '0'.
         * 3. If `balance` is in `-1..1`, the current prefix is valid: `count++`.
         * 4. Return `count`.
         *
         * ### Complexity
         * - Time **O(n)**: one pass, O(1) work per character.
         * - Space **O(1)**: one integer of state.
         *
         * ### Common pitfalls
         * - **Stopping at the first invalid prefix.** Validity is not monotonic: `"1100"` has prefixes
         *   valid, invalid, valid, valid -> 3. Every prefix must be checked independently.
         * - **Checking real adjacency** (`s[i] != s[i-1]`) instead of counts: the question is about a
         *   rearrangement, so `"0011"` itself is fine as a prefix even though it is not alternating.
         * - **Using `< 1` / `== 0`** (even-length only) and forgetting odd-length prefixes where the counts
         *   differ by exactly one.
         */
        fun referenceSolution(s: String): Int {
            var balance = 0
            var count = 0
            for (c in s) {
                balance += if (c == '1') 1 else -1
                if (balance in -1..1) count++
            }
            return count
        }
    }
}
