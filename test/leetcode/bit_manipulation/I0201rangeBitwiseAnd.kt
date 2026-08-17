package leetcode.bit_manipulation

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 201. Bitwise AND of Numbers Range  (https://leetcode.com/problems/bitwise-and-of-numbers-range/)
 *
 * Given two integers `left` and `right` that represent the inclusive range `[left, right]`, return the bitwise AND of
 * every number in that range — i.e. `left & (left + 1) & (left + 2) & ... & right`. Iterating the whole range is not
 * viable: the range can span more than two billion values.
 *
 * Constraints:
 * - 0 <= left <= right <= 2^31 - 1  (`right` can be exactly `Int.MAX_VALUE`, so beware of overflow if you try to
 *   compute `right + 1` or loop with an `Int` counter)
 * - `left == right` is allowed (answer is that number itself)
 * - `left` may be 0
 */
typealias I0201 = (Int, Int) -> Int

class I0201rangeBitwiseAnd {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0201> {

        override val cases = leetcode.testCases<I0201>(
            leetcode.args(5, 7) expects 4,                   // 101 & 110 & 111 = 100
            leetcode.args(0, 0) expects 0,
            leetcode.args(
                1,
                2147483647
            ) expects 0,          // Int.MAX_VALUE — a loop-based solution will not finish
            leetcode.args(9, 12) expects 8,                  // 1001 & 1010 & 1011 & 1100 = 1000
            leetcode.args(1, 1) expects 1,                   // single-element range
            leetcode.args(2147483647, 2147483647) expects 2147483647,
            leetcode.args(0, 2147483647) expects 0,
            leetcode.args(6, 7) expects 6,                   // 110 & 111 = 110
            leetcode.args(12, 15) expects 12,
        )

        // NOTE: `::rangeBitwiseAnd` (your attempt) is intentionally left out of `check` for now — it compiles but
        // fails several cases (see "Where your attempt diverges" below). Re-add it once fixed:
        //     fun test() = check(::rangeBitwiseAnd, ::referenceSolution, ::referenceSolution2)
        @Test
        fun test() = check(::rangeBitwiseAnd, ::referenceSolution, ::referenceSolution2)

        fun rangeBitwiseAnd(left: Int, right: Int): Int {
            var hi = right
            while (left < hi) hi = hi and (hi - 1)
            return hi
        }

        /**
         * ### Restatement
         * AND together *every* integer in `[left, right]`. The range can hold billions of values, so the answer must
         * come from the structure of the numbers, not from a loop over them.
         *
         * ### The one insight: AND over a range keeps only the common binary prefix
         * A bit survives an AND only if it is `1` in **every** operand. So ask, per bit position, "is this bit set in
         * all of `left..right`?"
         *
         * Write the range in binary. Because the numbers are *consecutive*, the low bits are exactly a counter running
         * through all its states — and any bit that changes at least once inside the range is `0` somewhere, hence `0`
         * in the answer. The high bits, on the other hand, are frozen: they cannot change without the counter below
         * them wrapping.
         *
         * So split the bits of `left` and `right` at the first position where they differ:
         *
         * ```
         * left  = 9  = 1 0 0 1
         * right = 12 = 1 1 0 0
         *              ^ common prefix = "1", then bits differ
         * answer     = 1 0 0 0  = 8
         * ```
         *
         * Everything from the first differing bit down must be zeroed, because somewhere in the range that bit is `0`.
         * Formally: if `left` and `right` differ at bit `k`, then `left <= x < 2^k * m <= right` for some multiple, so
         * the range contains a number ending in `k` zeros *and* its predecessor ending in `k` ones — their AND already
         * kills every one of those low bits. Hence:
         *
         *     answer = longest common binary prefix of left and right, zero-padded on the right
         *
         * This is the whole problem. No case analysis, no special handling of `left == 0`.
         *
         * ### Approach (this function)
         * Shift both endpoints right until they are equal — each shift discards one bit that may differ — counting the
         * shifts. What remains *is* the common prefix; shift it back into place.
         *
         * ### Time: `O(1)` — at most 32 iterations (`O(log(max))`, and the word size is a constant).
         * ### Space: `O(1)` — three `Int` locals.
         *
         * ### Common pitfalls
         * - **Looping the range.** `(left..right).reduce(Int::and)` is the definition, not a solution: `1..2^31-1` is
         *   ~2 billion iterations, and an `Int` loop counter that must pass `Int.MAX_VALUE` overflows to negative and
         *   never terminates. Any expression touching `right + 1` has the same bug.
         * - **`shr` vs `ushr`.** Both work here since the inputs are non-negative (`right <= 2^31 - 1`, so bit 31 is
         *   always clear). `ushr` is the safer habit for bit surgery — with a negative input `shr` sign-extends and the
         *   loop would spin on `-1 != -1`... actually never terminating for mixed signs.
         * - **`1 shl 31` is `Int.MIN_VALUE`,** not `2^31`. Any solution enumerating "powers of two up to 2^31" in `Int`
         *   silently produces a negative sentinel.
         * - **`left == right`** needs no special case (zero shifts), and neither does `left == 0` (the prefix is empty,
         *   so the answer is `0` — correct, since `0` is in the range).
         *
         * ### Where your attempt diverges
         * `rangeBitwiseAnd` is built on a real observation, just an incomplete one. "A power of two lies strictly above
         * `left` and at most `right`" does correctly detect *when the answer is 0*: if `p = 2^k` is in `(left, right]`
         * then both `p - 1 = 0111…1` and `p = 1000…0` are in the range and their AND is already `0`. Two gaps:
         * 1. In that case the answer is **`0`**, not the power of two `p` — you `return it`.
         * 2. When no boundary is crossed the answer is *not* `left`; it is the common prefix. `(9, 12)` crosses no
         *    power of two, so your code returns `9`, but the answer is `8` — `left`'s own low bits still have to go.
         *    Same for `(5, 7)`: it returns `5` instead of `4`.
         *
         * Also, `it in left..right` should be `it in (left + 1)..right`: for `(2, 3)` the power `2` equals `left`, no
         * boundary is crossed, and the answer is `2` — inclusive-on-`left` would wrongly flag it. And the
         * `val pow2 = …forEach { }` binding holds `Unit`; the `map` allocates a 32-element boxed list only to iterate
         * it. Once you fold "zero out everything below the first differing bit" into the idea, the power-of-two probe
         * dissolves into the two loops below.
         */
        fun referenceSolution(left: Int, right: Int): Int {
            var lo = left
            var hi = right
            var shift = 0
            while (lo != hi) {
                lo = lo ushr 1
                hi = hi ushr 1
                shift++
            }
            return lo shl shift
        }

        /**
         * ### Same answer, from the other end: Brian Kernighan's bit-clearing
         * `n and (n - 1)` clears the **lowest set bit** of `n` — the classic trick behind `popcount` and
         * "is this a power of two". Here it gives the common prefix without any shift bookkeeping.
         *
         * Read the loop as: *while `right` is still larger than `left`, its lowest set bit cannot survive the AND, so
         * drop it.* Each removal makes `right` smaller while keeping it a prefix-superset of the true answer; the moment
         * `right <= left`, no bit below the common prefix remains and `right` has been ground down to exactly that
         * prefix. Because `left <= right` throughout, `right == left` at the end iff the whole of `left` survives.
         *
         * ### Time: `O(1)` — one iteration per set bit of `right`, so ≤ 31, and often far fewer than the shift version.
         * ### Space: `O(1)`.
         *
         * ### Pitfalls
         * - **Guard the loop, not the arithmetic.** `right - 1` would underflow only at `right == 0`, which implies
         *   `left == 0` and the loop body never runs — so the `left < right` condition is what keeps it safe.
         * - **`left < right`, not `left != right`.** They are equivalent given the precondition, but `<` also makes the
         *   invariant ("stop as soon as `right` has dropped to or below `left`") explicit.
         *
         * ### Where this shows up for real
         * "AND over a contiguous range = common prefix" is exactly CIDR/subnet arithmetic: an IP range collapses to a
         * network prefix, and a routing table lookup is a longest-common-prefix match. The same reasoning powers
         * range-to-prefix decomposition in firewall rules, and prefix-trie (radix tree) key splitting.
         */
        fun referenceSolution2(left: Int, right: Int): Int {
            var hi = right
            while (left < hi) hi = hi and (hi - 1)
            return hi
        }

    }
}
