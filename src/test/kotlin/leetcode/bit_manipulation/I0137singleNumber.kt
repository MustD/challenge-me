package leetcode.bit_manipulation

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 137. Single Number II  (https://leetcode.com/problems/single-number-ii/)
 *
 * Given an integer array `nums` where every element appears exactly three times except for one element,
 * which appears exactly once, find and return that single element.
 *
 * The problem explicitly asks for a solution with linear runtime complexity and only constant extra space —
 * so a HashMap of counts, while correct, does not satisfy the stated requirement.
 *
 * Constraints:
 * - 1 <= nums.length <= 3 * 10^4
 * - -2^31 <= nums[i] <= 2^31 - 1  (values can be negative, including Int.MIN_VALUE — watch the sign bit)
 * - Each element in nums appears exactly three times except for one element which appears once.
 */
typealias I0137 = (IntArray) -> Int

class I0137singleNumber {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0137> {

        override val cases = leetcode.testCases<I0137>(
            "[2,2,3,2]" expects 3,
            "[0,1,0,1,0,1,99]" expects 99,
            // single element — the loner is the whole array
            "[7]" expects 7,
            // negative answer: the sign bit must be reconstructed correctly
            "[-4,-4,-4,7,7,7,-9]" expects -9,
            // extreme value: Int.MIN_VALUE as the loner
            "[-2147483648]" expects -2147483648,
            "[1,1,1,-2147483648]" expects -2147483648,
        )

        @Test
        fun test() = check(::singleNumber, ::singleNumberBruteForce, ::referenceSolution, ::referenceSolution2)

        fun singleNumberBruteForce(nums: IntArray): Int {
            return nums.toTypedArray()
                .groupingBy { it }
                .eachCount()
                .map { (k, v) -> v to k }
                .toMap()[1] ?: throw IllegalStateException()
        }

        fun singleNumber(nums: IntArray): Int {
            var result = 0
            for (bit in 0..31) {
                var count = 0
                nums.forEach { num -> count += (num ushr bit) and 1 }
                if (count % 3 != 0) result = result or (1 shl bit)
            }

            return result
        }

        /**
         * ### Restatement
         * Every value occurs **exactly three times** except one, which occurs once. Return that one,
         * in `O(n)` time and `O(1)` extra space.
         *
         * ### Why the 136 trick dies here
         * In 136 the whole algorithm was `x xor x == 0`: pairs annihilate. With *triples* nothing
         * annihilates — `x xor x xor x == x` — so folding the array leaves a XOR of every distinct
         * value, which is garbage. The self-inverse property is a statement about counting **mod 2**,
         * and this problem counts **mod 3**. So build a mod-3 counter yourself.
         *
         * ### Pattern: per-bit counting modulo k (bit-slicing)
         * The key move is to stop thinking of `nums` as 32-bit integers and think of it as **32
         * independent columns of bits**. Look at bit position `i` across the whole array: every
         * thrice-repeated value contributes its bit `i` three times, so `(sum of column i) % 3` is
         * exactly the loner's bit `i` — the triples vanish mod 3 no matter how they're interleaved.
         * Solve 32 tiny independent problems, then reassemble the answer.
         *
         * This generalizes: "all appear `k` times except one appearing once" → sum each bit column
         * `% k`. And "except one appearing `m` times" (`m < k`) → the remainder is `m`, so divide it
         * out. 136 is just the `k = 2` case of this, where the column sum mod 2 *is* XOR.
         *
         * ### Approach (this function)
         * For `i` in `0..31`: count how many `nums[j]` have bit `i` set; if `count % 3 != 0`, set bit
         * `i` in the result. Two nested loops, no allocation.
         *
         * ### Time: `O(32n)` = `O(n)`
         * 32 passes over the array. Asymptotically optimal, but ~32x the memory traffic of the
         * one-pass state machine below — same complexity class, worse constant.
         *
         * ### Space: `O(1)`
         * Two `Int` locals. `1 shl i` and `and`/`or` are single-cycle ALU ops; nothing is boxed.
         *
         * ### Common pitfalls
         * - **The sign bit (`i == 31`).** `nums[i]` can be `Int.MIN_VALUE`, so bit 31 participates.
         *   Use `(n shr i) and 1` — or better `(n ushr i) and 1` — never a comparison like `n > 0`.
         *   Reassembling with `result = result or (1 shl 31)` is correct in Kotlin/Java precisely
         *   *because* `Int` is two's-complement and `1 shl 31 == Int.MIN_VALUE`; the negative answer
         *   falls out for free. In Python (unbounded ints) this same loop yields `2^31 - ...` and you
         *   must subtract `2^32` manually — a classic port bug.
         * - **`shr` vs `ushr` at bit 31.** For a *single* extracted bit, `and 1` masks the sign
         *   extension away, so both work here. If you ever drop the `and 1`, `shr` sign-extends and
         *   silently corrupts everything. Prefer `ushr` for bit extraction as a habit.
         * - **Accumulating into `count` then `% 3`** is safe: `n <= 3 * 10^4` so the column sum can't
         *   overflow. Adding a `% 3` inside the loop is equivalent and needed only for huge inputs.
         * - **Don't sum the values**, only the bits. `3 * sum(distinct) - sum(all)` is a valid formula
         *   but needs a `Set` (breaking `O(1)` space) and overflows `Int` — it demands `Long`.
         * - **Fragility, same as 136:** if the "exactly three times" invariant is violated, this
         *   returns a meaningless number rather than failing. Correct only under a guaranteed premise.
         *
         * ### On the existing brute force
         * `singleNumberBruteForce` is correct here but instructive in two ways. It is `O(n)` *space*,
         * which is what the problem forbids, and `nums.toTypedArray()` boxes all `n` values into
         * `Integer` objects. More subtly, `.map { (k, v) -> v to k }.toMap()[1]` **inverts** the map to
         * `count -> value`: that only works because exactly one value has count 1. If two values had
         * count 1, one would be silently overwritten and lost. Keying on the value and filtering on the
         * count — `nums.asSequence().groupingBy { it }.eachCount().entries.first { it.value == 1 }.key`
         * — says the same thing without the collision hazard. (The counting map's real virtue is that
         * it *can* detect a broken premise; the bit tricks cannot.)
         */
        fun referenceSolution(nums: IntArray): Int {
            var result = 0
            for (bit in 0 until Int.SIZE_BITS) {
                var count = 0
                for (n in nums) count += (n ushr bit) and 1
                if (count % 3 != 0) result = result or (1 shl bit)
            }
            return result
        }

        /**
         * ### Same idea, one pass: the two-mask state machine
         * The mod-3 counter per bit needs 2 bits of state (values 0, 1, 2). Instead of 32 separate
         * counters, hold those two state bits as **two whole `Int`s** and update all 32 columns at once
         * — `ones` is the low bit of every counter, `twos` the high bit. That is bit-slicing again, but
         * with the loop over bits replaced by SIMD-within-a-register.
         *
         * Per bit the state `(twos, ones)` walks `00 -> 01 -> 10 -> 00` as copies arrive:
         *
         * ```
         * ones = (ones xor n) and twos.inv()   // toggle "seen once", but clear it if already at 2
         * twos = (twos xor n) and ones.inv()   // uses the NEW ones: at count 3 both drop to 0
         * ```
         *
         * Trace one bit with `n`'s bit set: `(0,0) -> (0,1) -> (1,0) -> (0,0)`. Bits appearing three
         * times return to `00`; the loner ends at `01`, so `ones` is the answer. Negative inputs need
         * no special case — bit 31 is just another column.
         *
         * ### Time `O(n)` / Space `O(1)`
         * One pass, four ALU ops per element, two `Int` locals. This is the version to write in an
         * interview once you can *derive* it; the 32-column loop above is the version to write when
         * you need to explain *why* it's correct.
         *
         * ### Pitfall: the update order is load-bearing
         * `twos` must be computed **after** `ones` and read the updated `ones`. Swap the two lines and
         * the third occurrence no longer resets the counter. Also note `ones` is *not* "bits seen an odd
         * number of times" — the `and twos.inv()` mask is what makes it a mod-3 counter rather than the
         * mod-2 XOR of problem 136.
         *
         * ### Unlike 136, this does not parallelize for free
         * The state machine is associative only under a custom merge of `(ones, twos)` pairs, not plain
         * `xor`, so `reduce`-style parallel folding is no longer trivially correct. The 32-column
         * counting version *does* split cleanly (column sums are just integer addition), which is the
         * shape a distributed count-mod-k would actually use — the same trick underlies
         * generalized-parity / erasure-coding schemes over `GF(3)` and streaming "heavy hitter"
         * sketches, where per-position counters replace a per-key hash map.
         */
        fun referenceSolution2(nums: IntArray): Int {
            var ones = 0
            var twos = 0
            for (n in nums) {
                ones = (ones xor n) and twos.inv()
                twos = (twos xor n) and ones.inv()
            }
            return ones
        }
    }
}
