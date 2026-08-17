package leetcode.binary_search

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 704. Binary Search  (https://leetcode.com/problems/binary-search/)
 *
 * Given an array of integers `nums` sorted in ascending order and an integer `target`, return the
 * index of `target` in `nums`, or `-1` if it is not present. The required runtime is O(log n).
 *
 * Constraints:
 * - 1 <= nums.length <= 10^4
 * - -10^4 < nums[i], target < 10^4
 * - All integers in `nums` are unique and `nums` is sorted in ascending order.
 */
typealias I0704 = (IntArray, Int) -> Int

class I0704search {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0704> {

        override val cases = leetcode.testCases<I0704>(
            leetcode.args("[-1,0,3,5,9,12]", 9) expects 4,
            leetcode.args("[-1,0,3,5,9,12]", 2) expects -1,
            leetcode.args("[5]", 5) expects 0,       // single element, hit
            leetcode.args("[5]", -5) expects -1,     // single element, miss
            leetcode.args("[1,2,3,4,5]", 1) expects 0,  // target at left boundary
            leetcode.args("[1,2,3,4,5]", 5) expects 4,  // target at right boundary
        )

        @Test
        fun test() = check(::search)

        /**
         * Pattern: **classic binary search on a sorted array**, expressed with a *recursive*
         * divide-and-conquer helper (`search(l, r)`) over an inclusive window `[l, r]`. Each step
         * probes the midpoint and discards one half based on the comparison. This is the
         * "return-on-hit" flavour: it exits the instant it lands on `target`, rather than always
         * narrowing to a single index first.
         *
         * ### Time — O(log n)
         * Every recursive call throws away (roughly) half of the current window: the right branch
         * moves `l` to `midIdx + 1`, the left branch moves `r` to `midIdx` (and `midIdx < r`
         * whenever `r > l`, so the window strictly shrinks). Starting from `n` candidates it takes
         * ~⌊log2 n⌋ halvings to reach the size-1 base case — for the constraint `n ≤ 10^4`, ≤ ~14
         * comparisons. Work per frame is O(1).
         *
         * ### Space — O(log n)
         * This version is recursive and **not** `tailrec`, so the JVM keeps one stack frame per
         * level: depth = the number of halvings = O(log n) (≤ ~14 here). No other auxiliary
         * structures; output is a single `Int`. An iterative loop would bring this to O(1).
         *
         * ### Correctness notes
         * - Base case is the `r - l == 0` guard: a window of one element that isn't `target`
         *   returns `-1`. Termination is guaranteed because both branches shrink the window.
         * - The left branch recurses on `[l, midIdx]` — re-including `midIdx` even though `midVal`
         *   was just shown `!= target`. Harmless: `midIdx` is only re-tested in the terminal size-1
         *   frame, which then returns `-1`. It costs at most one extra comparison, never a wrong
         *   answer (the discarded half can't contain `target` since `target < midVal` there).
         * - `nums[midIdx]` on the first call is safe because the constraints guarantee
         *   `nums.length ≥ 1`; a truly empty array would throw here.
         * - **Overflow pitfall (fine here, worth knowing):** `(r + l) / 2` can overflow `Int` when
         *   `l + r > Int.MAX_VALUE`. Indices max out near `10^4` in this problem so it never does,
         *   but the robust idiom is `l + (r - l) / 2`.
         *
         * ### Alternatives / trade-offs
         * - **Iterative binary search** (`while (l <= r) { ... }`): same O(log n) time but O(1)
         *   space — no recursion stack. The usual production form.
         * - **Library call:** `Arrays.binarySearch(nums, target)` / Kotlin's `IntArray` has no
         *   direct one, but `nums.toList().binarySearch(target)` works. Note it returns
         *   `-(insertionPoint) - 1` on a miss rather than `-1`, so you'd map the result.
         * - **Lower-bound / half-open template** (`[lo, hi)`, `hi` exclusive, converge until
         *   `lo == hi`): doesn't early-exit but generalizes cleanly to "first index ≥ target",
         *   duplicates, and answer-space binary search. A more reusable template than return-on-hit.
         * - This is already asymptotically optimal: any comparison-based search on an arbitrary
         *   sorted array needs Ω(log n) comparisons in the worst case (decision-tree bound).
         *
         * ### Parallelism
         * Not applicable. Binary search is inherently sequential — each probe depends on the result
         * of the previous comparison (a hard data dependency), so there's nothing to fan out. Where
         * parallelism *would* help: running many independent queries against the same array (
         * embarrassingly parallel over the query set), or — for very small `n` — a branchless/SIMD
         * linear scan that beats log-n search because it dodges branch mispredictions and stays in
         * one cache line.
         *
         * ### Real-world
         * The idea is everywhere: `lower_bound`/`upper_bound` in the C++ STL, insertion-point
         * lookups, `git bisect`, timestamp-window queries in rate limiters, and "answer-space"
         * binary search (min feasible value). At scale the *shape* is kept but the branching factor
         * grows: database indexes and filesystems use B-trees (fan-out in the hundreds) instead of
         * binary, because minimizing disk/cache-line fetches matters more than minimizing
         * comparisons. And on real hardware, for small arrays a plain linear scan often wins on
         * wall-clock thanks to cache locality and the branch predictor — the asymptotically
         * "optimal" answer isn't always the fastest one.
         */
        fun search(nums: IntArray, target: Int): Int {

            fun search(l: Int = 0, r: Int = nums.lastIndex): Int {
                val midIdx = (r + l) / 2
                val midVal = nums[midIdx]

                if (midVal == target) return midIdx
                if (r - l == 0) return -1

                return if (target > midVal) search(midIdx + 1, r)
                else search(l, midIdx)
            }

            return search()
        }

    }
}
