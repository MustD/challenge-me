package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 594. Longest Harmonious Subsequence  (https://leetcode.com/problems/longest-harmonious-subsequence/)
 *
 * We define a harmonious array as an array where the difference between its maximum value and its minimum value is
 * exactly 1.
 *
 * Given an integer array nums, return the length of its longest harmonious subsequence among all its possible
 * subsequences.
 *
 * Constraints:
 * - 1 <= nums.length <= 2 * 10^4
 * - -10^9 <= nums[i] <= 10^9
 */
typealias I0594 = (IntArray) -> Int

class I0594findLHS {

    @Nested
    inner class Solution : ProblemTest<I0594> {

        override val cases = testCases<I0594>(
            "[1,3,2,2,5,2,3,7]" expects 5,
            "[1,2,3,4]" expects 2,
            "[1,1,1,1]" expects 0,
            "[1]" expects 0,                                     // single element
            "[1,3,5,7]" expects 0,                               // distinct, no adjacent values at all
            "[5,5,5,5,5,1,2]" expects 2,                         // biggest group has no k±1 partner
            "[1,2,2,1]" expects 4,                               // whole array is harmonious
            "[-1,0,-1,0,0]" expects 5,                           // negatives and zero
            "[1000000000,999999999,1000000000]" expects 3,       // max-value elements
            "[3,2,1,0]" expects 2,                               // strictly decreasing
            "[1,1,1,2,2,0,0,0,0]" expects 7,                     // overlapping pairs (0,1) vs (1,2): pick the bigger
            "[-1000000000,-999999999,-999999999]" expects 3,     // min-value elements
            "[1,2]" expects 2,                                   // smallest harmonious input
            "[1,1,3,3,3,4]" expects 4,                           // diff 2 must not count, diff 1 must
        )

        @Test
        fun test() = check(::findLHS)

        /**
         * ## Analysis (lt-check) — verified: 3 official + 11 added cases
         *
         * **Time:** O(n) expected. `groupingBy { it }.eachCount()` is one pass building a `LinkedHashMap` (O(1)
         * average per insert); the `forEach` over the distinct keys `d ≤ n` does O(1) average lookups per key. Worst
         * case with adversarial hash collisions degrades toward O(n log n) (JVM HashMap treeifies buckets), never
         * worse in practice.
         *
         * **Space:** O(d) ⊆ O(n) for the count map (boxed `Int` keys and counts — around 50–80 bytes per entry on
         * the JVM, so the constant is not small). No recursion; `asSequence()` adds nothing beyond the map.
         *
         * Optimal: O(n) time / O(n) space — reached. (O(1) extra space is possible only by sorting in place, which
         * costs O(n log n) time.)
         *
         * **Correctness:** a harmonious subsequence uses exactly two values `k` and `k + 1` (max − min = 1 forbids
         * three distinct values and forbids a single value), and since it is a *subsequence*, not a subarray, order
         * is irrelevant — you can take every occurrence of both. So the answer is
         * `max over k with count[k+1] > 0 of count[k] + count[k+1]`, which is exactly what you compute. Checking only
         * `k + 1` (not `k − 1`) suffices: the pair `(k−1, k)` is visited when the loop is at key `k − 1`. The
         * `containsKey` guard handles the "all equal" trap (`[1,1,1,1]` → 0, not 4). Overflow is a non-issue:
         * `k + 1 ≤ 10^9 + 1` fits in `Int`, and the sum of counts is ≤ n.
         *
         * Small nits (no behaviour change): `index.getOrDefault(k, 0)` can be `index.getValue(k)` (k is a key), and
         * after the `containsKey` check `index[k + 1]` is known to be present — e.g.
         * `index[k + 1]?.let { max = maxOf(max, index.getValue(k) + it) }` folds the guard and the lookup into one.
         * The name `index` reads like "position"; `count`/`freq` says what it holds.
         *
         * **Pattern:** *frequency map + complement lookup* — the same shape as Two Sum (look up `target − x`) and
         * Longest Consecutive Sequence (look up `x ± 1`). Whenever a "subsequence" problem ignores order, collapse
         * the input into a multiset first.
         *
         * **Alternatives:**
         * - *Sort + two pointers / run-length:* sort, then walk runs of equal values; whenever the next run's value
         *   is exactly previous + 1, combine the two run lengths. O(n log n) time, O(1) extra (in-place
         *   `IntArray.sort()`, primitive dual-pivot quicksort — often faster than the boxed map for n = 2·10^4
         *   because of cache locality).
         * - *Single-pass map:* update the answer while counting (after `cnt[x]++`, check `cnt[x-1]` and `cnt[x+1]`).
         *   Same O(n), one pass instead of two — not asymptotically better, just fused.
         * Nothing beats O(n): every element must be read, since any one of them may change a count.
         *
         * **Parallelism:** counting is embarrassingly parallel (split the array, build per-chunk maps, merge — a
         * map-reduce), and the final scan over keys is too. But at n ≤ 2·10^4 the whole thing runs in microseconds;
         * thread start-up and map merging would dominate, so the realistic speedup is < 1. It only pays at
         * hundreds of millions of elements, where the merge step (O(d) per chunk) becomes the Amdahl bottleneck if
         * values are mostly distinct.
         *
         * **Real world:** "histogram, then look at adjacent buckets" is everyday analytics — e.g. a SQL
         * `GROUP BY value` followed by a self-join on `value + 1` (or `LEAD()` over sorted groups), or finding the
         * densest pair of neighbouring bins in a metrics histogram. At scale the counts come from a distributed
         * `countByKey`, and the memory concern (boxed map vs. primitive arrays) is what drives the choice between
         * the hashing and the sort-based variants.
         */
        fun findLHS(nums: IntArray): Int {
            val index = nums.asSequence().groupingBy { it }.eachCount()
            var max = 0
            index.keys.forEach { k ->
                if (index.containsKey(k + 1)) {
                    max = maxOf(max, index.getOrDefault(k, 0) + index.getOrDefault(k + 1, 0))
                }
            }
            return max
        }

    }
}
