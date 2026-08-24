package leetcode.math

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 172. Factorial Trailing Zeroes  (https://leetcode.com/problems/factorial-trailing-zeroes/)
 *
 * Given an integer `n`, return the number of trailing zeroes in `n!`,
 * where `n! = n * (n - 1) * (n - 2) * ... * 3 * 2 * 1`.
 *
 * Constraints:
 * - 0 <= n <= 10^4
 *
 * Follow-up: can you do it in logarithmic time (i.e. without computing n!)?
 */
typealias I0172 = (Int) -> Int

class I0172trailingZeroes {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0172> {

        override val cases = leetcode.testCases<I0172>(
            3 expects 0,      // 3! = 6
            5 expects 1,      // 5! = 120
            0 expects 0,
            4 expects 0,      // 4! = 24
            10 expects 2,     // 10! = 3628800
            25 expects 6,     // 25/5 = 5, 25/25 = 1
            30 expects 7,
            10000 expects 2499,
        )

        // NOTE: `trailingZeroes` is still a TODO stub, so it is left out of `check(...)`.
        // Add `::trailingZeroes` back once you implement it.
        @Test
        fun test() = check(::trailingZeroes, ::referenceSolution)

        fun trailingZeroes(n: Int): Int {
            var remaining = n
            var zCount = 0
            while (remaining > 0) {
                remaining /= 5
                zCount += remaining
            }
            return zCount
        }

        fun rlTest() {
            tailrec fun factorial(n: Int, run: Long = 1): Long =
                if (n <= 1) run else {
                    println(run)
                    factorial(n - 1, run * n)
                }
            factorial(15)
        }

        /**
         * ## Restatement
         * Count how many zeros `n!` ends with — without ever building `n!` (10000! has ~35660 digits,
         * so no primitive integer type can hold it).
         *
         * ## Pattern: prime factorization / counting factors (number theory, not iteration)
         * A trailing zero is produced by one factor of 10, and 10 = 2 * 5. So the answer is
         * `min(count of 2s, count of 5s)` in the prime factorization of `n!`.
         * In any run of consecutive integers, multiples of 2 are far denser than multiples of 5,
         * so 5s are always the bottleneck: **answer = number of factors of 5 in n!**.
         *
         * ## Approach (Legendre's formula)
         * Every 5th number contributes a 5 (5, 10, 15, ...) → `n / 5` of them.
         * Every 25th number contributes a *second* 5 (25, 50, 75, ...) → `n / 25` more.
         * Every 125th contributes a third → `n / 125`, and so on.
         *
         *     zeros(n) = n/5 + n/25 + n/125 + n/625 + ...   (integer division, until the term is 0)
         *
         * Check with n = 30: 30/5 = 6, 30/25 = 1, 30/125 = 0 → 7. Matches the test case.
         * Check with n = 10000: 2000 + 400 + 80 + 16 + 3 = 2499.
         *
         * ## Complexity
         * - Time: `O(log_5 n)` — the divisor multiplies by 5 each step, so at most ~6 iterations for n ≤ 10^4.
         * - Space: `O(1)`.
         *
         * ## Pitfalls
         * - Computing `n!` first (even in `Long`/`BigInteger`) — overflows or is needlessly slow; the whole
         *   point of the follow-up is to avoid it.
         * - Counting only `n / 5` and forgetting higher powers: 25 contributes **two** 5s, 125 three.
         *   This is the single most common wrong answer (fails at n = 25).
         * - Writing `divisor *= 5` in an `Int` loop variable: for large `n` the divisor eventually exceeds
         *   `Int.MAX_VALUE` and overflows to a negative number → infinite loop. Use `Long` for the divisor,
         *   or the `n /= 5` accumulate-in-place form below, which cannot overflow.
         * - `n = 0` → `0! = 1`, zero trailing zeros; the loop handles it naturally.
         */
        fun referenceSolution(n: Int): Int {
            var remaining = n
            var zeros = 0
            while (remaining > 0) {
                remaining /= 5
                zeros += remaining
            }
            return zeros
        }

    }
}
