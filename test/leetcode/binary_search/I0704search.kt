package leetcode.binary_search

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 704. Binary Search  (https://leetcode.com/problems/binary-search/)
 *
 * Given an array of integers `nums` which is sorted in ascending order, and an integer `target`,
 * write a function to search `target` in `nums`. If `target` exists, then return its index.
 * Otherwise, return `-1`.
 *
 * You must write an algorithm with `O(log n)` runtime complexity.
 *
 * Constraints:
 * - 1 <= nums.length <= 10^4
 * - -10^4 < nums[i], target < 10^4
 * - All the integers in `nums` are unique.
 * - `nums` is sorted in ascending order.
 */
typealias I0704 = (IntArray, Int) -> Int

class I0704search {

    @Nested
    inner class Solution : ProblemTest<I0704> {

        override val cases = testCases<I0704>(
            args("[-1,0,3,5,9,12]", 9) expects 4,
            args("[-1,0,3,5,9,12]", 2) expects -1,
            args("[5]", 5) expects 0,                    // single element, hit
            args("[5]", -5) expects -1,                  // single element, miss
            args("[-1,0,3,5,9,12]", 12) expects 5,       // target at the last index
            args("[-1,0,3,5,9,12]", -1) expects 0,       // target at the first index
            args("[-1,0,3,5,9,12]", 13) expects -1,      // target above every element
            args("[-1,0,3,5,9,12]", -5) expects -1,      // target below every element
            args("[2,5]", 5) expects 1,                  // two elements, right one
            args("[-9999,9999]", -9999) expects 0,       // value extremes, two elements, left one
            args("[-1,0,3,5,9,12]", 4) expects -1,       // miss that falls in a gap between elements
            args("[2,5]", 3) expects -1,                 // two elements, miss between them (lo/hi cross)
            args("[-9999,9999]", 9999) expects 1,        // value extremes, two elements, right one
            args("[1,3,5,7,9]", 5) expects 2,            // odd length, hit on the very first mid
            args(
                """
                [-199,-197,-195,-193,-191,-189,-187,-185,-183,-181,-179,-177,-175,-173,-171,-169,-167,-165,-163,-161,
                 -159,-157,-155,-153,-151,-149,-147,-145,-143,-141,-139,-137,-135,-133,-131,-129,-127,-125,-123,-121,
                 -119,-117,-115,-113,-111,-109,-107,-105,-103,-101,-99,-97,-95,-93,-91,-89,-87,-85,-83,-81,
                 -79,-77,-75,-73,-71,-69,-67,-65,-63,-61,-59,-57,-55,-53,-51,-49,-47,-45,-43,-41,
                 -39,-37,-35,-33,-31,-29,-27,-25,-23,-21,-19,-17,-15,-13,-11,-9,-7,-5,-3,-1,
                 1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39,
                 41,43,45,47,49,51,53,55,57,59,61,63,65,67,69,71,73,75,77,79,
                 81,83,85,87,89,91,93,95,97,99,101,103,105,107,109,111,113,115,117,119,
                 121,123,125,127,129,131,133,135,137,139,141,143,145,147,149,151,153,155,157,159,
                 161,163,165,167,169,171,173,175,177,179,181,183,185,187,189,191,193,195,197,199]
            """, 77
            ) expects 138,                        // 200 odd numbers -199..199, hit deep in the right half
        )

        @Test
        fun test() = check(::search)

        /**
         * ## Analysis — iterative binary search, closed interval `[lo, hi]`
         *
         * **Pattern:** classic binary search on a sorted array, *closed-interval* variant. Invariant: if `target`
         * is in `nums`, its index lies in `[lo, hi]`. Every iteration either returns or strictly shrinks that
         * interval, discarding the half that provably cannot hold `target`.
         *
         * **Time — O(log n).** The `while (lo <= hi)` loop halves the live interval each pass (`lo = midI + 1` or
         * `hi = midI - 1` both drop `midI` *and* one whole side), so it runs at most ⌊log₂ n⌋ + 1 times — for
         * `n = 10^4` that is ≤ 14 iterations. Best case O(1): hit on the first `midI` (e.g. `[1,3,5,7,9]`, 5).
         * Each iteration is O(1): one array read, at most two comparisons.
         *
         * **Space — O(1).** Three `Int` locals (`lo`, `hi`, `midI`) plus `midN`; no recursion, no allocation.
         *
         * **Correctness notes**
         * - `lo <= hi` (not `<`) is the right guard for a closed interval: a one-element window `lo == hi` still
         *   has to be inspected — that is exactly the `[5]` and two-element cases.
         * - `midI ± 1` pairs with the closed interval. Since `nums[midI]` was already compared, excluding it
         *   guarantees progress; writing `hi = midI` here would loop forever on `[2,5]`, 3.
         * - `lo + (hi - lo) / 2` is the overflow-safe midpoint. With `n ≤ 10^4` plain `(lo + hi) / 2` could never
         *   overflow, but the habit matters: it was a real bug in the JDK's `Arrays.binarySearch` for ~9 years
         *   (Bloch, 2006) once arrays exceeded 2^30 elements. The JDK now uses `(lo + hi) ushr 1`.
         * - Comparing `target` to `midN` (rather than `midN` to `target`) reads naturally: "target is to the
         *   right → move `lo` right". `else` as the equality branch is safe because the two strict checks
         *   above it are exhaustive otherwise.
         * - Uniqueness of `nums` (constraint) means "return on first hit" is the full answer. With duplicates
         *   this returns *some* matching index, not the first — that is the lower-bound variant (below).
         * - Empty input is outside the constraints, but `hi = -1` makes the loop skip and return `-1` anyway.
         *
         * **Alternatives**
         * - *Half-open `[lo, hi)`* — `hi = nums.size`, `while (lo < hi)`, `hi = mid`. Same O(log n)/O(1); this is
         *   the form that generalises to **lower bound / upper bound** (first index with `nums[i] >= target`),
         *   which is what you need for duplicates, insertion position (LC 35), or range queries (LC 34). Worth
         *   learning both and never mixing their update rules.
         * - *Recursive* — same time, but O(log n) stack frames; no benefit here.
         * - *Library* — `nums.binarySearch(target)` returns the index, or `-(insertionPoint) - 1` on a miss, so
         *   the one-liner is `nums.binarySearch(target).let { if (it >= 0) it else -1 }`.
         * - *Linear scan* — O(n), violates the required O(log n), but on a few dozen elements it is often faster
         *   in practice (no branch mispredictions, sequential prefetch).
         * - *Interpolation search* — O(log log n) average on uniformly distributed keys, O(n) worst case. No
         *   asymptotically better comparison-based method exists: each comparison has 3 outcomes, so
         *   distinguishing n+1 results needs Ω(log n) comparisons. Theirs is optimal.
         *
         * **Parallelism.** Not worth it. Each step depends on the previous comparison — a strict data
         * dependency chain of ~14 steps; thread hand-off costs microseconds while the whole search costs tens of
         * nanoseconds. The "parallel" ideas that do exist are *k-ary search* (compare against k−1 pivots at once
         * with SIMD, cutting depth to log_k n) and *batching many independent queries* across threads — the
         * latter is how real systems parallelise lookups.
         *
         * **Real-world.** Binary search is everywhere sorted data lives: B-tree node lookup in databases,
         * SSTable/index blocks in LSM stores (RocksDB, Cassandra), `git bisect` (binary search over commit
         * history), sorted-array symbol tables, and finding a version/timestamp in an ordered log. At scale the
         * cost is cache misses, not comparisons — each probe jumps far away in memory — which is why production
         * code uses cache-friendly layouts (Eytzinger/B-tree order), branchless loops, or a linear scan once the
         * window is small. In everyday Kotlin you'd call `binarySearch` / `Collections.binarySearch` rather than
         * hand-roll it; the value of writing it yourself is owning the invariant for the many problems that
         * binary-search on an *answer space* instead of an array (LC 875, 1011, 410).
         */
        fun search(nums: IntArray, target: Int): Int {
            var lo = 0
            var hi = nums.lastIndex

            while (lo <= hi) {
                val midI = lo + (hi - lo) / 2
                val midN = nums[midI]
                when {
                    target > midN -> lo = midI + 1
                    target < midN -> hi = midI - 1
                    else -> return midI
                }
            }
            return -1
        }

    }
}
