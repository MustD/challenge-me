package leetcode.binary_search

import leetcode.ProblemTest
import leetcode.expects
import leetcode.expectsAnyOf
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 162. Find Peak Element  (https://leetcode.com/problems/find-peak-element/)
 *
 * A peak element is an element that is strictly greater than both of its neighbors. Given a 0-indexed
 * integer array `nums`, find any peak element and return its index. If the array contains multiple peaks,
 * return the index of any one of them.
 *
 * You may imagine that `nums[-1] = nums[n] = -∞` (out-of-bounds neighbors are negative infinity), so the
 * first/last element only needs to beat its single in-array neighbor to be a peak.
 *
 * You must write an algorithm that runs in O(log n) time.
 *
 * Constraints:
 * - 1 <= nums.length <= 1000
 * - -2^31 <= nums[i] <= 2^31 - 1
 * - nums[i] != nums[i + 1] for all valid i (no two adjacent elements are equal -> a peak always exists)
 *
 * NOTE: multiple valid answers may exist. Single-peak cases use plain `expects`; for arrays with
 * several peaks use `expectsAnyOf(...)`, which passes if your returned index matches *any* listed peak.
 */
typealias I0162 = (IntArray) -> Int

class I0162findPeakElement {

    @Nested
    inner class Solution : ProblemTest<I0162> {

        override val cases = testCases<I0162>(
            "[1,2,3,1]" expects 2,          // 3 is the only peak
            "[1]" expects 0,                // single element: trivially a peak
            "[1,2]" expects 1,              // strictly increasing -> last is the peak
            "[2,1]" expects 0,              // strictly decreasing -> first is the peak
            "[1,3,5,4,2]" expects 2,        // single peak: 5 at index 2
            "[1,2,1,3,5,6,4]".expectsAnyOf(1, 5),  // two peaks (idx 1 and 5): either index is correct
        )

        @Test
        fun test() = check(::findPeakElement, ::referenceSolution)

        /**
         * ANALYSIS of YOUR solution (`findPeakElement`). Verified: passes all 6 cases.
         *
         * PATTERN: Binary search on a condition (not on a sorted array). You never compare against a
         * search target; you compare each `mid` to its right neighbor to decide which half *must* still
         * contain a peak. This "binary search the slope toward a local maximum" idea is the transferable
         * insight — the array isn't sorted, yet you can still discard half the space each step.
         *
         * WHY IT'S CORRECT: invariant "a peak exists within [lo, hi]" holds at entry (nums[-1]=nums[n]=-inf
         * guarantee a peak somewhere) and is preserved each iteration:
         *   - current < next  -> uphill to the right, a peak lies in (mid, hi]  -> lo = mid + 1
         *   - current >= next -> mid itself or something left is a peak in [lo, mid] -> hi = mid (keeps mid)
         * The loop strictly shrinks (hi - lo) every step, so it terminates at lo == hi, which is a peak.
         *
         * TIME:  O(log n). The `while (lo < hi)` loop (line 49) halves (hi - lo) each iteration via
         *        `lo = mid + 1` or `hi = mid`; constant work inside -> log2(n) iterations. Matches the
         *        problem's required O(log n). For n <= 1000 that's <= 10 iterations.
         * SPACE: O(1) auxiliary. Three Int locals (lo, hi, mid) + the read values; iterative, no recursion,
         *        so no call-stack growth. Output is a single Int.
         *
         * EDGE CASES YOU HANDLE WELL:
         *   - n == 1: loop body never runs (lo == hi == 0), returns 0. Correct (single element is a peak).
         *   - Strictly increasing / decreasing arrays converge to the last / first index respectively.
         *   - `mid` is always < `hi` while lo < hi, so `mid + 1` is in-bounds — the `getOrNull` never
         *     actually returns null here (see note below).
         *
         * NOTE ON `nums.getOrNull(mid + 1) ?: Int.MIN_VALUE` (line 52) vs. the reference's plain
         * `nums[mid + 1]`: both are correct. Yours is the more *defensive* spelling — it makes the
         * "right neighbor of the last element is -inf" idea explicit in code, so it would still behave if
         * the bound ever loosened. The trade-off: `getOrNull` allocates nothing but does a bounds check +
         * null-coalesce on every iteration, marginally more work than the direct index; and it can mask an
         * off-by-one (an accidental out-of-bounds silently becomes MIN_VALUE instead of throwing). At log n
         * iterations the cost is irrelevant; it's a readability/safety-vs-strictness style choice, not a
         * correctness one. Using `current >= next` (the implicit `else`) rather than a separate `==` case is
         * fine because the constraint nums[i] != nums[i+1] means equality never occurs.
         *
         * ALTERNATIVES:
         *   - Linear scan O(n)/O(1): return the first i with nums[i] > nums[i+1] (treating nums[n] = -inf).
         *     Simpler, but violates the required O(log n) — though for n <= 1000 it would still pass here.
         *   - Recursive binary search: same asymptotics, but O(log n) stack frames instead of your O(1)
         *     iterative space. Your iterative form is strictly better on space.
         *   Your solution is already asymptotically optimal: any algorithm must read at least O(log n)
         *   elements to locate a peak in the worst case (this is a known lower bound for the comparison
         *   model on this problem), so no better than O(log n) is possible.
         *
         * PARALLELISM: not worth it. The algorithm is inherently sequential — each iteration's bounds depend
         * on the previous comparison (a data dependency chain), so there's nothing to fan out. A *parallel*
         * angle exists only for the O(n) linear variant (split the array into chunks, each thread finds a
         * local peak in its slice, reconcile boundaries) — but with n <= 1000 the thread-spawn overhead
         * dwarfs any gain (Amdahl: the sequential reconciliation + tiny input cap the speedup near 1x).
         *
         * REAL-WORLD: "binary search on a monotone predicate" is the genuinely useful takeaway. In
         * production you reach for it for `lower_bound`/`upper_bound`-style queries, finding the first
         * failing build in a regression range (`git bisect` is exactly this), capacity/threshold tuning
         * ("smallest N that keeps latency under X"), and rate-limit/quota boundaries. The literal "find a
         * peak" rarely appears; what transfers is the habit of asking "is there a monotone yes/no condition
         * I can bisect on?" — even when the underlying data is not sorted, as here.
         */
        fun findPeakElement(nums: IntArray): Int {
            var lo = 0
            var hi = nums.lastIndex

            while (lo < hi) {
                val mid = lo + (hi - lo) / 2
                val current = nums[mid]
                val next = nums.getOrNull(mid + 1) ?: Int.MIN_VALUE
                if (current < next) {
                    lo = mid + 1
                } else {
                    hi = mid
                }
            }
            return lo
        }

        /**
         * PATTERN: Binary search on an *unsorted* array (binary search on a condition / "find any local
         * maximum"). The trick is that we don't need a sorted array to binary-search — we only need a rule
         * that lets us discard half the search space at each step.
         *
         * INTUITION: Because adjacent elements are never equal, look at any mid and its right neighbor:
         *   - If nums[mid] < nums[mid+1], the slope is going UP to the right. Following an uphill slope, you
         *     must eventually hit a peak (worst case the last element, since nums[n] = -inf). So a peak
         *     exists somewhere in (mid, end]  -> search the RIGHT half: lo = mid + 1.
         *   - If nums[mid] > nums[mid+1], the slope is going DOWN to the right, so mid itself, or something to
         *     its left, is a peak (worst case index 0, since nums[-1] = -inf). So a peak exists in [start, mid]
         *     -> search the LEFT half (keep mid): hi = mid.
         * Each step halves the range and the invariant "a peak exists within [lo, hi]" is preserved, so when
         * lo == hi that single surviving index is a peak.
         *
         * APPROACH:
         *   1. lo = 0, hi = n - 1.
         *   2. While lo < hi: mid = lo + (hi - lo) / 2 (overflow-safe midpoint).
         *      - nums[mid] < nums[mid+1] -> lo = mid + 1   (uphill, peak on the right)
         *      - else                    -> hi = mid       (downhill/peak here, peak on the left incl. mid)
         *   3. Return lo (== hi).
         *
         * COMPLEXITY: O(log n) time — halve the range each iteration. O(1) extra space.
         *
         * PITFALLS:
         *   - Compare against the RIGHT neighbor (mid+1) and use `hi = mid` (not mid-1) so you never drop the
         *     candidate peak. Using mid-1 on the down branch can skip the actual peak.
         *   - `mid` never equals `hi` while lo < hi, so nums[mid+1] is always in bounds — no overflow read.
         *   - Use lo + (hi - lo) / 2, not (lo + hi) / 2, to avoid int overflow on large indices (defensive
         *     habit; here n <= 1000 so it can't actually overflow, but it's the right reflex).
         *   - This returns *a* peak, not necessarily the global max — that's exactly what the problem asks.
         */
        fun referenceSolution(nums: IntArray): Int {
            var lo = 0
            var hi = nums.size - 1
            while (lo < hi) {
                val mid = lo + (hi - lo) / 2
                if (nums[mid] < nums[mid + 1]) {
                    lo = mid + 1      // uphill to the right: a peak lies to the right
                } else {
                    hi = mid          // downhill (or mid is a peak): keep mid, search left
                }
            }
            return lo
        }

    }
}
