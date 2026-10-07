package leetcode.array_string

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4000. Largest Integer With Given Digit Sum  (https://leetcode.com/problems/largest-integer-with-given-digit-sum)
 *
 * You are given two non-negative integers `n` and `s`.
 *
 * Return the **largest** integer that has **at most** `n` digits and whose sum of digits is `s`. If no such integer
 * exists, return -1.
 *
 * Constraints:
 * - 1 <= n <= 5
 * - 0 <= s <= 100
 */
typealias I4000 = (Int, Int) -> Int

class I4000largestInteger {

    @Nested
    inner class Solution : ProblemTest<I4000> {

        override val cases = testCases<I4000>(
            args(2, 9) expects 90,
            args(2, 19) expects -1,
            args(5, 0) expects 0,
            args(1, 0) expects 0,          // smallest n, zero sum
            args(1, 9) expects 9,          // single digit at its ceiling
            args(1, 10) expects -1,        // single digit, just over 9*n
            args(5, 45) expects 99999,     // exactly 9*n at max n: largest possible answer
            args(5, 46) expects -1,        // one past 9*n at max n
            args(5, 100) expects -1,       // max s, far beyond reach
            args(
                5,
                1
            ) expects 10000,      // remainder in the top digit, trailing zeros fill the rest
            args(4, 20) expects 9920,      // 9s, then partial remainder digit, then zero
        )

        @Test
        fun test() = check(::largestInteger, ::referenceSolution)

        /**
         * ## Reference solution: greedy fill from the most significant digit
         *
         * **Restatement.** You have `n` digit slots and must distribute a total of `s` among them (each slot holds
         * 0..9). Make the resulting number as large as possible. If `s` cannot fit at all (`s > 9n`), return -1.
         *
         * **Pattern: greedy, most-significant-first.** For numbers with the same number of digits, the comparison
         * is decided by the first digit where they differ. A larger digit high up beats anything you could do
         * lower down. So spend the budget on the leftmost slot first, as much as it can hold, then move right.
         *
         * ### Intuition
         * "Largest number" means "front-load the digits". Put 9s at the front until the budget is under 9, then the
         * remainder, then zeros. Zeros at the tail are free: they keep the number at `n` digits, and an `n`-digit
         * number with a non-zero top digit is larger than any shorter number. So "at most n digits" really means
         * "exactly n slots, trailing zeros allowed".
         *
         * ### Core concepts
         * - **Lexicographic order of equal-length numbers.** Comparing digit by digit from the left is the same as
         *   comparing the values. This is why a greedy choice at the top position is safe.
         * - **Exchange argument.** To prove a greedy is optimal, show that any solution which deviates from it can
         *   be improved by swapping toward the greedy choice. Here, move one unit from a lower digit to a higher
         *   one that is below its cap.
         * - **Feasibility bound.** The capacity is `9 * n`. Check it first so the main loop only builds valid answers.
         * - **Horner's rule.** Build a number left to right with `acc = acc * 10 + digit`. No powers are needed,
         *   and it is the same loop you use to parse a digit string.
         *
         * ### Approach
         * 1. If `s > 9 * n`, return -1.
         * 2. Repeat `n` times: `digit = min(remaining, 9)`, `remaining -= digit`, `acc = acc * 10 + digit`.
         * 3. Return `acc`. `s = 0` gives 0 naturally.
         *
         * ### Complexity
         * - Time **O(n)**: one constant-time step per digit.
         * - Space **O(1)**: just the accumulator and the remaining budget.
         *
         * ### Common pitfalls
         * - **Building digits right to left** (putting 9s at the low end) gives the *smallest* valid number of
         *   that length, not the largest.
         * - **Special-casing `s = 0`.** No special case is needed: all zeros gives 0, which is a valid answer.
         * - **Off-by-one on the bound.** `s == 9n` is feasible (all 9s). Only `s > 9n` fails.
         * - **Using fewer than `n` digits.** Stopping once the budget is spent (dropping trailing zeros) turns
         *   `n=5, s=1` into 1 instead of 10000.
         * - **Overflow when generalised.** With large `n`, the answer does not fit in `Int` or `Long`, so build a
         *   string instead.
         */
        fun referenceSolution(n: Int, s: Int): Int {
            if (s > 9 * n) return -1
            var remaining = s
            var acc = 0
            repeat(n) {
                val digit = minOf(remaining, 9)
                remaining -= digit
                acc = acc * 10 + digit
            }
            return acc
        }

        // Attempt review: correct and already optimal (greedy most-significant-first, O(n) time, O(1) space).
        // There is no defect to fix. Two optional polish points: check `s > 9 * n` up front instead of
        // `sum != 0` at the end, and use Horner's rule (`result = result * 10 + digit`) in place of `Math.powExact`.
        /**
         * ## Analysis of this solution (validated: 11/11 cases pass)
         *
         * ### Pattern
         * **Greedy digit construction, most-significant digit first.** Comparing two numbers of equal length is
         * lexicographic on their digits, so a bigger top digit beats any choice made lower down. Each position
         * therefore takes the largest digit it can, `min(sum, 9)`. The same idea solves "largest/smallest number
         * with a digit-sum or digit-budget constraint" problems in general (see LC 1449, LC 2384, LC 670).
         *
         * ### Why it is correct
         * - **Use all n positions.** Any number with fewer than `n` digits is smaller than the best `n`-digit
         *   one with the same sum, as long as the top digit is non-zero. Filling every position and letting the
         *   tail be zeros covers "at most n digits" automatically. `s = 0` gives `00000 = 0`, which is the right
         *   answer, and no special case is needed.
         * - **Exchange argument.** If an optimal answer had a digit `d < min(remaining, 9)` at some position, moving
         *   one unit from a later digit to that position keeps the sum and makes the number bigger. So no
         *   optimal answer can differ from the greedy one.
         * - **Feasibility check.** `sum != 0` after the loop means `s > 9 * n`. The `-1` result is decided only
         *   after the digits are built, which is fine because building them has no side effects. An early
         *   `if (s > 9 * n) return -1` would say the same thing up front and states the invariant more clearly.
         * - **Overflow.** The largest result is `99999`, well inside `Int`. `Math.powExact` would throw on
         *   overflow, which is a nice defensive habit, though it can never fire here (`10^4` max).
         *
         * ### Complexity
         * - **Time: O(n).** There is one pass of `n` iterations (`for (i in 0..<n)`). Each iteration calls
         *   `Math.powExact(10, k)`, which costs O(log k) or O(k) depending on the implementation, so strictly
         *   it is O(n log n) or O(n²) digit operations. With `n <= 5` that does not matter. Horner's rule
         *   (`result = result * 10 + digit`) avoids the power call and is the usual idiom: O(n) with no `pow`.
         * - **Space: O(1).** Just `sum`, `result` and the loop index.
         *
         * ### Alternatives
         * - **Closed form.** With `q = s / 9` and `r = s % 9`, the answer is `q` nines, then `r` (if any), then
         *   zeros, padded to `n` digits. This is the same O(n), just written without the per-digit `min`.
         * - **String building.** `"9".repeat(q) + r + "0".repeat(...)` followed by `toInt()`. This is the natural
         *   form when `n` is large (LC 1449-style answers do not fit in a `Long`). The arithmetic version only
         *   works because `n <= 5`.
         * - **Brute force.** Scan from `10^n - 1` down and return the first number with digit sum `s`. That
         *   is O(10^n · n). It is fine for `n <= 5`, but it is the wrong instinct, and the greedy is already
         *   optimal: you must write `n` digits anyway, so Ω(n) is a lower bound.
         *
         * ### Parallelism
         * Not applicable. Each digit depends on the `sum` left over by the digits before it (a sequential
         * prefix dependency), and the input has at most 5 digits. Thread overhead would be millions of times the
         * work. The closed form does make each digit independent (`digit_i = clamp(s - 9*i, 0, 9)`), so it
         * *could* be computed data-parallel, which shows how removing a loop-carried dependency is what makes
         * parallelism possible. At this size it is still pointless.
         *
         * ### Real-world experience
         * The "max-first greedy fill" shape shows up in allocating a budget across ordered slots with a per-slot
         * cap: filling the highest-priority buckets first under rate limits, water-filling bandwidth allocation,
         * packing the most significant fields of a key. In production the cap and the slot count are usually
         * data rather than constants, and the answer is a list or string rather than a machine integer, so the
         * string/array form is what you would actually write.
         */
        fun largestInteger(n: Int, s: Int): Int {
            var sum = s
            var result = 0

            for (i in (0..<n)) {
                val digit = if (sum <= 9) sum else 9
                sum -= digit
                result += digit * (Math.powExact(10, n - 1 - i))
            }

            return if (sum != 0) -1 else result
        }

    }
}
