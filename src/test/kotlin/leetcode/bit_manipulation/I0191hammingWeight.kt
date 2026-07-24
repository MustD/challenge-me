package leetcode.bit_manipulation

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 191. Number of 1 Bits  (https://leetcode.com/problems/number-of-1-bits/)
 *
 * Given a positive integer n, return the number of set bits (1s) in its binary
 * representation — also known as the Hamming weight.
 *
 * Constraints:
 * - 1 <= n <= 2^31 - 1  (fits in a positive 32-bit signed Int; no sign/overflow concerns here)
 *
 * Follow-up: if this function is called many times, how would you optimize it?
 */
typealias I0191 = (Int) -> Int

class I0191hammingWeight {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0191> {

        override val cases = leetcode.testCases<I0191>(
            11 expects 3,            // 1011  -> three set bits
            128 expects 1,           // 10000000 -> one set bit
            2147483645 expects 30,   // 0111...1101 -> thirty set bits
            1 expects 1,             // edge: smallest input
            2147483647 expects 31,   // edge: 2^31 - 1, all low 31 bits set
        )

        @Test
        fun test() = check(::hammingWeight, ::referenceSolution)

        /**
         * Analysis — verified correct (all 5 cases pass).
         *
         * Pattern: bit masking / fixed-width bit iteration. Walk every bit position
         * 0..31 and test whether that bit is set in `n` using a shifted single-bit mask.
         *
         * Correctness — the subtle part is operator precedence in the condition
         *   `1 shl it == 1 shl it and n`
         * In Kotlin, named infix functions (`shl`, `and`) bind *tighter* than `==`, so
         * this parses as `(1 shl it) == ((1 shl it) and n)`, NOT as `1 shl (it == ...)`.
         * `(1 shl it) and n` isolates just bit `it` of `n`: the result is `1 shl it`
         * when that bit is set and `0` otherwise, so the equality is a correct "is bit
         * set?" test. It reads like it could be broken; it isn't. (Adding explicit
         * parentheses would make the intent obvious to the next reader.)
         *   Range `0..31` covers the whole Int width, and the constraint `1 <= n <= 2^31-1`
         * keeps `n` non-negative, so there's no sign-bit / arithmetic-shift edge case.
         *
         * Time: O(1) — exactly 32 iterations regardless of input (Int is fixed 32-bit);
         *   constant work per iteration. (One caveat below.)
         * Space: O(1) — a single counter. The per-iteration `.toString(2)` allocates
         *   short-lived ~32-char strings, still O(1), but see the note.
         *
         * Note — leftover debug I/O: the `print`/`println` calls run 32 times per call.
         *   They don't affect correctness (tests pass), but console I/O is orders of
         *   magnitude slower than the arithmetic, so they dominate real runtime and
         *   should be removed before this counts as "done."
         *
         * Alternatives:
         *  - Brian Kernighan: `while (n != 0) { n = n and (n - 1); count++ }`. `n and (n-1)`
         *    clears the lowest set bit, so it loops once per set bit → O(popcount) instead
         *    of always 32. Same O(1) worst case, but fewer iterations on sparse inputs and
         *    no wasted string allocation.
         *  - Stdlib: `n.countOneBits()` (compiles to `Integer.bitCount`, which lowers to a
         *    hardware POPCNT instruction on modern CPUs) — the production answer.
         *  - Follow-up ("called many times"): precompute a 256-entry byte→count lookup
         *    table, then sum 4 table lookups per Int (SWAR). Answers the file's follow-up.
         *
         * Parallelism: not applicable. Counting bits in a single 32-bit word is a few
         *   instructions; thread/SIMD overhead dwarfs it. Vectorized popcount (AVX-512
         *   VPOPCNTDQ / SWAR) only pays off counting bits across large arrays/bitsets,
         *   not one scalar.
         *
         * Real world: popcount underlies compressed bitmaps (roaring bitmaps, DB bitmap
         *   indexes), Hamming distance for similarity search / LSH, chess bitboards, and
         *   error-correcting codes. At scale you always reach for the hardware intrinsic or
         *   a SIMD kernel over arrays — never a hand-rolled per-bit loop.
         */
        fun hammingWeight(n: Int): Int {
            var count = 0
            (0..31).forEach {
                if (1 shl it == 1 shl it and n) count++
            }
            return count
        }

        /**
         * Reference — Brian Kernighan's algorithm.
         *
         * Problem, plainly: count the 1-bits in n's binary form (its Hamming weight).
         *
         * Pattern: bit trick, not iteration over positions. Key identity:
         *   `n and (n - 1)` clears the *lowest* set bit of n and leaves every other
         *   bit unchanged. Subtracting 1 flips the lowest 1 to 0 and turns all the
         *   zeros below it into 1s; AND-ing with the original then wipes that whole
         *   low run, netting exactly one set bit removed per step.
         *
         * Approach:
         *   1. Start count at 0.
         *   2. While n != 0: do `n = n and (n - 1)` (drop one set bit), count++.
         *   3. When n hits 0 every set bit has been counted → return count.
         *
         * Why prefer it over the position loop: it iterates once per *set bit*, not a
         * fixed 32 times, so sparse inputs (e.g. 128 -> 1 iteration) finish fast. It
         * also sidesteps the shift/mask precedence subtlety in the version above.
         *
         * Complexity:
         *   Time  O(popcount(n)) — one iteration per set bit; O(1) since Int is 32-bit,
         *         but strictly fewer iterations than the always-32 approach.
         *   Space O(1) — a single counter, no allocation.
         *
         * Pitfalls:
         *   - Loop condition must be `n != 0`, not `n > 0`. It's moot here because the
         *     constraint keeps n positive, but on a general 32-bit input a set sign bit
         *     makes n negative, and `n > 0` would exit early and undercount. `!= 0` is
         *     the safe, general condition.
         *   - Don't reuse the incoming param as a hot mutable in languages where that's
         *     surprising; in Kotlin shadowing it into a local `var` is fine and clear.
         *
         * Production answer: `n.countOneBits()` (JVM `Integer.bitCount`, lowers to a
         * hardware POPCNT). Reach for that in real code; Kernighan is the interview-clean
         * manual version.
         */
        fun referenceSolution(n: Int): Int {
            var bits = n
            var count = 0
            while (bits != 0) {
                bits = bits and (bits - 1)
                count++
            }
            return count
        }

    }
}
