package leetcode.kadane

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0053 = (IntArray) -> Int

class I0053maximumSubarray {

    @Nested
    inner class Solution : ProblemTest<I0053> {
        override val cases = testCases<I0053>(
            "[-2,1,-3,4,-1,2,1,-5,4]" expects 6,
            "[1]" expects 1,
            "[5,4,-1,7,8]" expects 23,
        )

        @Test
        fun test() = check(::maxSubArray)

        /**
         * # Maximum Subarray — Kadane's Algorithm
         *
         * **Pattern:** Kadane's algorithm — a 1-D dynamic program collapsed into two
         * running scalars. `sum` is the best subarray sum *ending at* index `i`; `max`
         * is the best seen *anywhere so far*. The DP recurrence is
         * `dp[i] = max(nums[i], nums[i] + dp[i-1])` — at each element you decide whether
         * to extend the previous subarray or start fresh at `nums[i]`. Because `dp[i]`
         * only depends on `dp[i-1]`, the whole table folds into one variable.
         *
         * ## Complexity
         * - **Time: O(n)** — a single `forEach` over indices `1..lastIndex`, with O(1)
         *   `maxOf` work per element. No nested loops, no sorting.
         * - **Space: O(1)** — only the two scalars `sum` and `max`; no recursion, no
         *   auxiliary arrays. (Output is a single `Int`, so no output space either.)
         *
         * This is asymptotically optimal: every element must be inspected at least once
         * to know the answer, so O(n) time is a hard lower bound. O(1) space is also
         * optimal. No better asymptotic class exists.
         *
         * ## Correctness notes
         * - Seeding both `sum` and `max` with `nums[0]` (rather than `0`) is the key
         *   subtlety: it correctly handles **all-negative arrays** (e.g. `[-3,-1,-2]` → `-1`).
         *   A naive `var max = 0` would wrongly return `0` for the empty subarray, but the
         *   problem requires a **non-empty** subarray. This seeding avoids that pitfall.
         * - The `isEmpty()` guard returning `0` is defensive; LeetCode guarantees
         *   `n >= 1`, so it never fires on real input, but it prevents the `nums[0]` access
         *   from throwing.
         * - **Overflow:** sums stay within `Int` for LeetCode's constraints
         *   (`n <= 1e5`, `|nums[i]| <= 1e4` → max magnitude `1e9`, inside `Int.MAX`).
         *   At larger scale you'd widen `sum`/`max` to `Long`.
         *
         * ## Alternatives
         * - **Divide & conquer (O(n log n)):** split in half, the answer is the best of
         *   {left max, right max, max crossing the midpoint}. Slower and uses O(log n)
         *   stack, but it's the natural fit if you need a **segment-tree** answering range
         *   max-subarray queries — each node stores prefix/suffix/total/best, merged in
         *   O(1), giving O(log n) per query under updates. Kadane can't do incremental
         *   range queries.
         * - **Prefix sums:** `maxSubarray = max(prefix[j] - min(prefix[0..j-1]))`. Same
         *   O(n)/O(1) when the running min is tracked inline — essentially Kadane in
         *   disguise.
         *
         * ## Parallelism
         * Kadane is inherently sequential (`dp[i]` depends on `dp[i-1]`), so a naive
         * left-to-right scan doesn't parallelize. But it *is* parallelizable as a
         * **monoid scan**: associate each segment with a 4-tuple
         * (total, bestPrefix, bestSuffix, bestSubarray) that merges in O(1). That makes
         * it a divide-and-conquer / parallel-reduce — split into k chunks, each thread
         * reduces its chunk, then combine. Realistically the speedup ceiling is bounded
         * by Amdahl's law and memory bandwidth: for `n <= 1e5` the array fits in cache
         * and the single-pass scan is already memory-bound, so thread/coordination
         * overhead dwarfs any gain. SIMD doesn't help either due to the carried
         * dependency. Worth it only for very large, out-of-cache or distributed arrays.
         *
         * ## Real-world
         * The segment-tree form shows up in **time-series / financial analytics** —
         * "maximum gain over any contiguous window" with live updates, where Kadane's
         * static scan won't answer incremental queries. The monoid-merge variant is also
         * how you'd compute this over **distributed/streamed data** (each shard emits its
         * tuple, a coordinator merges). For a one-shot in-memory array, though, this exact
         * O(n)/O(1) scan is what you'd actually ship — simpler beats clever at this scale.
         */
        fun maxSubArray(nums: IntArray): Int {
            if (nums.isEmpty()) return 0
            var sum = nums[0]
            var max = nums[0]

            (1..nums.lastIndex).forEach { i ->
                sum = maxOf(nums[i], nums[i] + sum)
                max = maxOf(max, sum)
            }

            return max
        }


    }
}
