package leetcode.sliding_window

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 643. Maximum Average Subarray I  (https://leetcode.com/problems/maximum-average-subarray-i/)
 *
 * You are given an integer array nums consisting of n elements, and an integer k.
 *
 * Find a contiguous subarray whose length is equal to k that has the maximum average value and return this value.
 * Any answer with a calculation error less than 10^-5 will be accepted.
 *
 * Constraints:
 * - n == nums.length
 * - 1 <= k <= n <= 10^5
 * - -10^4 <= nums[i] <= 10^4
 */
typealias I0643 = (IntArray, Int) -> Double

class I0643findMaxAverage {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0643> {

        override val cases = leetcode.testCases<I0643>(
            leetcode.args("[1,12,-5,-6,50,3]", 4) expects 12.75,
            leetcode.args("[5]", 1) expects 5.0,
            // dip-then-rise: windows 11, 2, 101 — best must come from the true last window, not a stale running sum
            leetcode.args("[10,1,1,100]", 2) expects 50.5,
            // k == n: single window, all negative
            leetcode.args("[-1,-2,-3]", 3) expects -2.0,
            // all-equal elements
            leetcode.args("[5,5,5,5]", 2) expects 5.0,
            // k == 1: answer is just the max element
            leetcode.args("[1,-1,1,-1,1]", 1) expects 1.0,
            // k == 1, all negative: `best` must come from the data, not an implicit 0 init
            leetcode.args("[-5,-1,-8,-9]", 1) expects -1.0,
            // best window sits in the interior, not at either end
            leetcode.args("[1,2,10,10,2,1]", 2) expects 10.0,
        )

        @Test
        fun test() = check(::findMaxAverage, ::referenceSolution)

        /**
         * ### Complexity
         * - Time `O(n)`: `nums.take(k).sum()` is one `O(k)` pass to seed the window, then the loop runs
         *   `n - k` times doing `O(1)` work (one subtract, one add, one `maxOf`) per step. Total `O(k) + O(n-k) = O(n)`.
         * - Space: `nums.take(k)` allocates a new `List<Int>` of size `k` before summing it (unlike the reference,
         *   which sums the first `k` elements in place with a plain loop) — `O(k)` auxiliary, not `O(1)`. Everything
         *   after that (`sum`, `best`, the loop index) is `O(1)`. Worth noticing since `k` can be up to `10^5`.
         *
         * ### Correctness
         * Same fixed-size sliding-window update as the reference (`sum -= nums[i-k]; sum += nums[i]`), just spelled
         * with `take(k).sum()` instead of a manual accumulation loop for the seed window — behaviourally identical.
         * `best` is initialised from the real first-window sum (not `0`), so an all-negative array (e.g.
         * `[-5,-1,-8,-9]`, `k=1` → `-1.0`) is handled correctly; `k == n` degenerates to zero loop iterations, which
         * is also fine. Division is deferred to the very end (`best / k.toDouble()`), so no per-window floating-point
         * rounding accumulates and no intermediate integer truncation occurs.
         *
         * ### Pattern
         * Fixed-size sliding window with a monotone objective transform (`avg = sum / k`, constant `k` → maximizing
         * sum maximizes average). See `referenceSolution`'s KDoc below for the full pattern writeup, alternative
         * approaches (prefix sums), parallelism discussion, and real-world context — it applies equally to this
         * implementation since the algorithmic core is the same; the only difference is the `List` allocation noted
         * above.
         */
        fun findMaxAverage(nums: IntArray, k: Int): Double {
            var sum = nums.take(k).sum()
            var best = sum
            for (i in k..nums.lastIndex) {
                sum -= nums[i - k]
                sum += nums[i]
                best = maxOf(best, sum)
            }
            return best / k.toDouble()
        }

        /**
         * Reference solution — fixed-size sliding window.
         *
         * **Restatement.** Among all `n - k + 1` contiguous windows of exactly `k` elements, find the one with the
         * largest average. Since every window has the same length `k`, the largest average belongs to the window
         * with the largest *sum* — so the problem is really "maximum sum of a length-`k` window", divided by `k`
         * once at the end.
         *
         * **Pattern: fixed-size sliding window.** Adjacent windows overlap in `k - 1` elements. Recomputing each sum
         * from scratch costs `O(k)` per window (`O(n*k)` total); instead, slide the window one step right by adding
         * the element that enters and subtracting the element that leaves — `O(1)` per step.
         *
         * ### Intuition
         * `sum(i+1 .. i+k) = sum(i .. i+k-1) + nums[i+k] - nums[i]`. The work shared between neighbouring windows
         * is reused rather than redone. And because the length is fixed, comparing sums is equivalent to comparing
         * averages, which keeps all arithmetic in exact integers until the single final division.
         *
         * ### Core concepts
         * - **Fixed-size sliding window** — a window of constant width moved one index at a time, maintained by an
         *   "add incoming, remove outgoing" update. Here it turns `O(n*k)` into `O(n)`.
         * - **Incremental (rolling) aggregate** — any aggregate with an inverse operation (sum/subtract,
         *   count/uncount, xor/xor) can be updated in `O(1)` as the window moves. Sum is the simplest case.
         * - **Monotone transformation of the objective** — `avg = sum / k` with constant `k > 0` preserves order,
         *   so maximising the sum maximises the average. Optimise the simpler quantity, convert at the end.
         * - **Prefix sums (alternative view)** — `sum(i..j) = prefix[j+1] - prefix[i]` gives the same answer with
         *   `O(n)` extra space; the sliding window is the space-free version of that idea.
         *
         * ### Approach
         * 1. Sum the first `k` elements → `windowSum`; set `best = windowSum`.
         * 2. For `i` from `k` to `n - 1`: `windowSum += nums[i] - nums[i - k]`; `best = max(best, windowSum)`.
         * 3. Return `best.toDouble() / k`.
         *
         * ### Complexity
         * - Time `O(n)` — one pass to build the first window, one pass sliding it; each step is `O(1)`.
         * - Space `O(1)` — only the running sum and the best sum are stored.
         *
         * ### Common pitfalls
         * - **Initialising `best` to `0`** — all numbers may be negative (e.g. `[-1]`, `k = 1` → `-1.0`). Initialise
         *   from the first window, never from `0` or an arbitrary sentinel.
         * - **Integer division** — `best / k` on `Int`s truncates (`51 / 4 == 12`, not `12.75`). Convert to `Double`
         *   *before* dividing.
         * - **Averaging per window in floating point** — works, but accumulates rounding noise and is slower; compare
         *   integer sums instead.
         * - **Overflow** — max window sum is `10^5 * 10^4 = 10^9`, which fits in `Int` (< 2.147e9), but only barely;
         *   with larger constraints use `Long`.
         * - **Off-by-one on the outgoing index** — when `nums[i]` enters, the element leaving is `nums[i - k]`, not
         *   `nums[i - k + 1]`.
         * - **`k == n`** — there is exactly one window; the sliding loop simply doesn't execute.
         */
        fun referenceSolution(nums: IntArray, k: Int): Double {
            var windowSum = 0
            for (i in 0 until k) windowSum += nums[i]

            var best = windowSum
            for (i in k until nums.size) {
                windowSum += nums[i] - nums[i - k]
                if (windowSum > best) best = windowSum
            }
            return best.toDouble() / k
        }

    }
}
