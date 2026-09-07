package leetcode.array_string

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 164. Maximum Gap  (https://leetcode.com/problems/maximum-gap/)
 *
 * Given an integer array `nums`, return the maximum difference between two successive elements in
 * its sorted form. If the array contains fewer than two elements, return 0.
 *
 * You must write an algorithm that runs in linear time and uses linear extra space.
 *
 * Constraints:
 * - 1 <= nums.size <= 10^5
 * - 0 <= nums[i] <= 10^9  (fits in Int, but differences/sums are safe only because values are non-negative)
 * - Linear time + linear extra space required, so a plain comparison sort is off the table.
 * - Single-element (and all-equal) inputs must return 0.
 */
typealias I0164 = (IntArray) -> Int

class I0164maximumGap {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0164> {

        override val cases = leetcode.testCases<I0164>(
            "[3,6,9,1]" expects 3,
            "[10]" expects 0,
            "[1,1,1,1]" expects 0,
            "[1,10000000]" expects 9999999,
            "[100,3,2,1]" expects 97,
            "[0,0,0,1000000000]" expects 1000000000,
            "[15,3,7,1,9,4]" expects 6,
        )

        @Test
        fun test() = check(
            ::maximumGap,
            ::referenceSolution,
            ::referenceSolutionRadix
        )

        /**
         * check [referenceSolution]
         */
        fun maximumGap(nums: IntArray): Int {
            if (nums.size < 2) return 0

            val (min, max) = nums.fold(nums[0] to nums[0]) { (min, max), i ->
                minOf(min, i) to maxOf(max, i)
            }

            if (max - min == 0) return 0
            val diff = max - min
            val intervalCount = nums.size - 1

            // ceil((max - min) / intervalCount)
            val bucketSize = ((max - min + nums.size - 2) / intervalCount).coerceAtLeast(1)
            val bucketCount = (max - min) / bucketSize + 1

            val bucketMin = IntArray(bucketCount) { Int.MAX_VALUE }
            val bucketMax = IntArray(bucketCount) { Int.MIN_VALUE }
            nums.forEach {
                val bucketNum = (it - min) / bucketSize
                bucketMin[bucketNum] = minOf(bucketMin[bucketNum], it)
                bucketMax[bucketNum] = maxOf(bucketMax[bucketNum], it)
            }

            var result = 0
            var prevMax = max
            var seen = false

            (0 until bucketCount).forEach { bucketIndex ->
                if (bucketMin[bucketIndex] == Int.MAX_VALUE) return@forEach // empty bucket
                if (seen) result = maxOf(result, bucketMin[bucketIndex] - prevMax)
                prevMax = bucketMax[bucketIndex]
                seen = true
            }
            return result
        }

        /**
         * ## Restatement
         *
         * Sort `nums` mentally, then look at every adjacent pair in that sorted order and return the
         * largest difference. Fewer than two elements → 0. The catch: you may not actually sort with a
         * comparison sort — O(n log n) is disallowed, you need O(n) time and O(n) space.
         *
         * ## Pattern: bucketing / pigeonhole principle
         *
         * The trick is that you do **not** need the fully sorted array — you only need the *largest*
         * gap, so you can afford to lose all information about small gaps.
         *
         * Let `min` and `max` be the extremes and `n = nums.size`. The `n` values span the range
         * `max - min`, and there are `n - 1` adjacent pairs in sorted order. If the values were spread
         * perfectly evenly, every gap would be exactly `(max - min) / (n - 1)`. Since the average gap is
         * that value, **the maximum gap is at least that value** — it can never be smaller.
         *
         * Now cut `[min, max]` into buckets of width `bucketSize = ceil((max - min) / (n - 1))`. Two
         * numbers inside the *same* bucket differ by less than `bucketSize`, i.e. by less than (or equal
         * to) the guaranteed lower bound on the answer — so **an intra-bucket gap can never be the
         * answer**. That is the pigeonhole insight: the winning gap must span from the *max of one
         * non-empty bucket* to the *min of the next non-empty bucket*.
         *
         * So we only need to store two ints per bucket (its min and its max), not the elements
         * themselves. With `n` buckets that is O(n) space and one linear pass to fill them, one linear
         * pass to scan them.
         *
         * ## Approach
         *
         * 1. `n < 2` → return 0.
         * 2. One pass for `min` and `max`. If `min == max`, all elements are equal → return 0.
         * 3. `bucketSize = ceil((max - min) / (n - 1))`, clamped to at least 1 so it is never 0.
         *    `bucketCount = (max - min) / bucketSize + 1`.
         * 4. One pass: element `v` lands in bucket `(v - min) / bucketSize`; update that bucket's
         *    min/max. Track emptiness (e.g. min = Int.MAX_VALUE sentinel).
         * 5. One pass over buckets in order, keeping `prevMax` of the last non-empty bucket; for each
         *    non-empty bucket the candidate gap is `bucket.min - prevMax`. Take the maximum.
         *
         * Note `min` always lands in bucket 0 and `max` in the last bucket, so both ends are covered.
         *
         * ## Complexity
         *
         * - Time **O(n)** — three linear passes (min/max, fill, scan); `bucketCount <= n`.
         * - Space **O(n)** — two int arrays of size `bucketCount <= n`.
         *
         * ## Common pitfalls
         *
         * - **`bucketSize == 0`**: with `max - min < n - 1` integer division gives 0 and the bucket index
         *   divides by zero. Clamp with `coerceAtLeast(1)`.
         * - **All-equal / single-element input** must short-circuit to 0 (`min == max`).
         * - **Comparing adjacent buckets, not adjacent array slots** — the answer is
         *   `nextNonEmpty.min - prevNonEmpty.max`; skipping empty buckets is the whole point.
         * - **Ceiling division**: `(max - min + n - 2) / (n - 1)` is the branch-free form; here
         *   `max - min <= 10^9` and `n <= 10^5`, so no Int overflow — but on a problem with negative
         *   values or larger bounds, `max - min` itself overflows and you need `Long`.
         * - Do **not** reach for `nums.sort()`: it passes the tests but violates the stated
         *   linear-time requirement, which is the entire point of this problem.
         *
         * ## On your attempt
         *
         * The `n == 1` / `n == 2` base cases are right (and `abs` is a nice touch for the unsorted pair).
         * What is missing is the key realisation above: you don't need the sorted array at all. The
         * moment you frame it as "the answer is at least the average gap, so anything smaller than the
         * average gap is irrelevant", the bucket structure falls out. Also fold `n == 1` into a single
         * `if (nums.size < 2) return 0`, since LeetCode allows the empty array in the general statement.
         */
        fun referenceSolution(nums: IntArray): Int {
            val n = nums.size
            if (n < 2) return 0

            var min = nums[0]
            var max = nums[0]
            for (v in nums) {
                if (v < min) min = v
                if (v > max) max = v
            }
            if (min == max) return 0

            // ceil((max - min) / (n - 1)), never 0
            val bucketSize = ((max - min + n - 2) / (n - 1)).coerceAtLeast(1)
            val bucketCount = (max - min) / bucketSize + 1

            val bucketMin = IntArray(bucketCount) { Int.MAX_VALUE }
            val bucketMax = IntArray(bucketCount) { Int.MIN_VALUE }
            for (v in nums) {
                val i = (v - min) / bucketSize
                if (v < bucketMin[i]) bucketMin[i] = v
                if (v > bucketMax[i]) bucketMax[i] = v
            }

            var result = 0
            var prevMax = max // placeholder; overwritten by bucket 0, which is always non-empty
            var seen = false
            for (i in 0 until bucketCount) {
                if (bucketMin[i] == Int.MAX_VALUE) continue // empty bucket
                if (seen) result = maxOf(result, bucketMin[i] - prevMax)
                prevMax = bucketMax[i]
                seen = true
            }
            return result
        }

        /**
         * Alternative linear-time route: **LSD radix sort** (8 bits per pass, 4 passes for a 32-bit
         * non-negative Int), then a single scan of adjacent differences.
         *
         * Time O(4 * (n + 256)) = O(n); space O(n + 256). It genuinely sorts, so it is easier to trust
         * than the bucket argument, but it moves more memory and only works because the keys are
         * bounded-width integers. Worth knowing as the general "how do I sort in O(n)" answer, of which
         * counting sort / bucket sort are the other members.
         */
        fun referenceSolutionRadix(nums: IntArray): Int {
            val n = nums.size
            if (n < 2) return 0

            var src = nums.copyOf()
            var dst = IntArray(n)
            val count = IntArray(256)

            for (shift in 0 until 32 step 8) {
                count.fill(0)
                for (v in src) count[(v ushr shift) and 0xFF]++
                var sum = 0
                for (b in 0 until 256) {
                    val c = count[b]
                    count[b] = sum
                    sum += c
                }
                for (v in src) dst[count[(v ushr shift) and 0xFF]++] = v
                val tmp = src; src = dst; dst = tmp
            }

            var result = 0
            for (i in 1 until n) result = maxOf(result, src[i] - src[i - 1])
            return result
        }

    }
}
