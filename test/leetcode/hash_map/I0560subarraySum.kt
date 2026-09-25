package leetcode.hash_map

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 560. Subarray Sum Equals K  (https://leetcode.com/problems/subarray-sum-equals-k/)
 *
 * Given an array of integers nums and an integer k, return the total number of subarrays whose sum equals to k.
 *
 * A subarray is a contiguous non-empty sequence of elements within an array.
 *
 * Constraints:
 * - 1 <= nums.length <= 2 * 10^4
 * - -1000 <= nums[i] <= 1000
 * - -10^7 <= k <= 10^7
 */
typealias I0560 = (IntArray, Int) -> Int

class I0560subarraySum {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0560> {

        override val cases = leetcode.testCases<I0560>(
            leetcode.args("[1,1,1]", 2) expects 2,
            leetcode.args("[1,2,3]", 3) expects 2,
            leetcode.args("[1]", 0) expects 0,
            leetcode.args("[0,0,0]", 0) expects 6,
            leetcode.args("[1,-1,0]", 0) expects 3,
            leetcode.args("[3,4,7,2,-3,1,4,2]", 7) expects 4,
            leetcode.args("[-1,-1,1]", 0) expects 1,
        )

        @Test
        fun test() = check(::subarraySum, ::referenceSolution)

        fun subarraySum(nums: IntArray, k: Int): Int {
            val seen = mutableMapOf(0 to 1) //
            var result = 0

            var sum = 0
            for (x in nums) {
                sum += x
                result += seen[sum - k] ?: 0
                seen.merge(sum, 1, Int::plus)
                print(sum)
            }
            return result
        }

        /**
         * ## Reference solution — prefix sums + hash map of seen prefix counts
         *
         * **Restatement.** Count the pairs of positions (i, j), i <= j, such that
         * `nums[i] + nums[i+1] + ... + nums[j] == k`. Values may be negative, so the window sum is not monotonic.
         *
         * **Pattern.** *Prefix sum + hash map* ("two-sum on prefix sums"). Any contiguous-range sum is the
         * difference of two prefix sums, so "count subarrays with sum k" becomes "count pairs of prefixes whose
         * difference is k" — and counting pairs with a given difference in one pass is exactly Two Sum with a
         * frequency map instead of an index map.
         *
         * Why *not* sliding window: a sliding window needs "growing the window only increases the sum" (all
         * values non-negative) to know when to shrink. With negatives (and zeros), shrinking or growing can move
         * the sum either way, so the window has no valid shrink rule.
         *
         * ### Intuition
         * Let `P(j)` be the sum of the first `j` elements (`P(0) = 0`). The subarray `nums[i until j]` sums to
         * `P(j) - P(i)`. It equals `k` iff `P(i) == P(j) - k`. So while walking left to right with the running sum
         * `P(j)`, the number of good subarrays *ending here* is simply "how many earlier prefixes equalled
         * `P(j) - k`" — a single hash-map lookup.
         *
         * ### Core concepts
         * - **Prefix sum** — `P(j) = nums[0] + ... + nums[j-1]`; turns any range sum into a subtraction of two
         *   numbers. Here it converts a question about O(n²) subarrays into one about pairs of O(n) prefixes.
         * - **Complement lookup (Two Sum idea)** — for each new value `x`, ask a hash map how many previous values
         *   equal the complement `x - k`. Turns a nested pair loop into one pass.
         * - **Empty-prefix sentinel** — seeding the map with `{0: 1}` represents `P(0)`, the prefix of length 0,
         *   so subarrays that start at index 0 are counted.
         * - **Frequency map vs. index map** — we need *how many* earlier prefixes match (duplicates of a prefix sum
         *   are common with zeros/negatives), not *where* one is; store counts.
         * - **Monotonicity requirement of sliding window** — the window technique is only valid when the metric
         *   moves one direction as the window grows; negatives break that, which is the signal to reach for
         *   prefix sums instead.
         *
         * ### Approach
         * 1. `count = {0: 1}`, `sum = 0`, `result = 0`.
         * 2. For each `x` in `nums`:
         *    - `sum += x` (now `sum == P(j)`);
         *    - `result += count[sum - k] ?: 0` (subarrays ending at this element);
         *    - `count[sum] += 1` (make this prefix available to later elements).
         * 3. Return `result`.
         *
         * Order in step 2 matters: look up *before* inserting the current prefix, otherwise with `k == 0` the
         * current prefix would pair with itself and count an empty subarray.
         *
         * ### Complexity
         * - Time: **O(n)** — one pass, O(1) expected per hash-map op.
         * - Space: **O(n)** — up to n + 1 distinct prefix sums in the map.
         * (Brute force over all (i, j) with a running sum is O(n²) time / O(1) space — ~2·10⁸ ops at n = 2·10⁴,
         * which is borderline; the prefix-map is the intended solution.)
         *
         * ### Common pitfalls
         * - **Forgetting the `{0: 1}` seed** — misses every subarray that starts at index 0 (e.g. `[1,2,3], k=3`
         *   would return 1 instead of 2).
         * - **Inserting before looking up** — for `k = 0` each prefix matches itself, counting n empty subarrays.
         * - **Using a sliding window** — silently wrong once negatives appear (`[1,-1,0], k=0` → 3).
         * - **Storing indices / a set instead of counts** — undercounts when the same prefix sum repeats
         *   (`[0,0,0], k=0` → 6, because the prefix 0 occurs four times: C(4,2) = 6).
         * - **Overflow** — not an issue here: |sum| <= 2·10⁴ · 1000 = 2·10⁷, well within `Int`.
         */
        fun referenceSolution(nums: IntArray, k: Int): Int {
            val seen = HashMap<Int, Int>()
            seen[0] = 1
            var sum = 0
            var result = 0
            for (x in nums) {
                sum += x
                result += seen[sum - k] ?: 0
                seen.merge(sum, 1, Int::plus)
            }
            return result
        }

    }
}
