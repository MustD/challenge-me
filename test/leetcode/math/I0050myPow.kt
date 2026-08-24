package leetcode.math

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 50. Pow(x, n)  (https://leetcode.com/problems/powx-n/)
 *
 * Implement pow(x, n), which calculates x raised to the power n (x^n).
 * n may be negative, in which case the answer is 1 / x^(-n).
 *
 * Constraints:
 * - -100.0 < x < 100.0
 * - -2^31 <= n <= 2^31 - 1  (n is an Int — note that -n overflows for n == Int.MIN_VALUE)
 * - either x is not zero or n > 0
 * - -10^4 <= x^n <= 10^4
 */
typealias I0050 = (Double, Int) -> Double

class I0050myPow {

    @Nested
    inner class Solution : ProblemTest<I0050> {

        // NOTE: Double equality in the harness is exact (==), no epsilon. Cases below are
        // deliberately chosen so the answer is exactly representable in binary floating point
        // (powers of 2), which keeps them association-independent. LeetCode's own example
        // x = 2.10000, n = 3 -> 9.26100 is omitted for that reason: every reasonable
        // implementation actually yields 9.261000000000001.
        override val cases = testCases<I0050>(
            args(2.0, 10) expects 1024.0,
            args(2.0, -2) expects 0.25,
            args(2.0, 0) expects 1.0,
            args(0.5, 2) expects 0.25,
            args(-2.0, 3) expects -8.0,
            args(-2.0, 4) expects 16.0,
            // Int.MIN_VALUE: negating n naively overflows back to itself.
            args(1.0, -2147483648) expects 1.0,
            args(2.0, -2147483648) expects 0.0,
        )

        @Test
        fun test() = check(::myPow, ::referenceSolution)

        fun myPow(x: Double, n: Int): Double {
            var e = n.toLong() // abs(Int.MIN_VALUE) = Int.MIN_VALUE
            val isNegative = n < 0
            if (isNegative) e = -e

            var result = 1.0
            var base = x
            while (e > 0) {
                if (e and 1L == 1L) result *= base
                base *= base
                e = (e shr 1)
            }
            return if (isNegative) 1 / result else result
        }


        /**
         * PATTERN: binary (fast) exponentiation — "exponentiation by squaring".
         *
         * INTUITION
         * The naive loop multiplies x by itself n-1 times: O(n). With n up to 2^31 that is
         * ~2 billion multiplications — a guaranteed TLE. The trick is to look at n in binary:
         *
         *     x^13 = x^(1101b) = x^8 * x^4 * x^1
         *
         * Every bit of n that is set contributes one factor, and those factors are just
         * repeated squarings of x: x, x^2, x^4, x^8, ... Squaring doubles the exponent for
         * the price of a single multiplication, so we only need ~log2(n) steps (<= 31 here).
         *
         * APPROACH (iterative, low bit first)
         * 1. Widen n to Long *before* negating. This is the crux of the Int.MIN_VALUE trap:
         *    -Int.MIN_VALUE overflows back to Int.MIN_VALUE, and abs() returns it unchanged
         *    (negative), so `power - 1` overflows the *other* way to Int.MAX_VALUE and
         *    `repeat` spins ~2.1 billion times. In Long there is room for 2147483648.
         * 2. Walk the bits of e: if the low bit is set, fold the current `base` into `result`;
         *    then square `base` and shift e right by one.
         * 3. If the original n was negative, return 1 / result.
         *
         * COMPLEXITY
         * Time  O(log n) — the loop halves e each iteration, so at most 32 iterations.
         * Space O(1) — iterative, no recursion stack. (The recursive divide-and-conquer
         *              variant is equally valid but costs O(log n) stack.)
         *
         * PITFALLS
         * - Int overflow on -n / abs(n) for n == Int.MIN_VALUE (see step 1) — the classic bug here.
         * - Do not compute 1/x up front and then power it: for x very close to 0 that loses
         *   precision faster than powering and inverting once at the end.
         * - Overflow/underflow to Infinity / 0.0 is *expected* and correct for the extreme
         *   cases (2.0^-2147483648 -> 1/Infinity -> 0.0); do not special-case it.
         * - x^0 == 1.0 for every x, including negative x.
         */
        fun referenceSolution(x: Double, n: Int): Double {
            var e = n.toLong()                 // widen BEFORE negating: -Int.MIN_VALUE overflows in Int
            val negative = e < 0
            if (negative) e = -e

            var result = 1.0
            var base = x
            while (e > 0) {
                if (e and 1L == 1L) result *= base
                base *= base
                e = e shr 1
            }
            return if (negative) 1 / result else result
        }


    }
}
