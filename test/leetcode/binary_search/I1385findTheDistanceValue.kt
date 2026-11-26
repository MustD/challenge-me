package leetcode.binary_search

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 1385. Find the Distance Value Between Two Arrays  (https://leetcode.com/problems/find-the-distance-value-between-two-arrays/)
 *
 * Given two integer arrays `arr1` and `arr2`, and the integer `d`, return the distance value between the two arrays.
 *
 * The distance value is defined as the number of elements `arr1[i]` such that there is not any element `arr2[j]`
 * where `|arr1[i] - arr2[j]| <= d`.
 *
 * Constraints:
 * - 1 <= arr1.length, arr2.length <= 500
 * - -1000 <= arr1[i], arr2[j] <= 1000
 * - 0 <= d <= 100
 */
typealias I1385 = (IntArray, IntArray, Int) -> Int

class I1385findTheDistanceValue {

    @Nested
    inner class Solution : ProblemTest<I1385> {

        override val cases = testCases<I1385>(
            args("[4,5,8]", "[10,9,1,8]", 2) expects 2,
            args("[1,4,2,3]", "[-4,-3,6,10,20,30]", 3) expects 2,
            args("[2,1,100,3]", "[-5,-2,10,-3,7]", 6) expects 1,
        )

        @Test
        fun test() = check(
            ::findTheDistanceValue,
            ::referenceSolution,
        )

        // Trace on example 1: arr1 = [4,5,8], arr2 = [10,9,1,8], d = 2  → expected 2
        fun findTheDistanceValue(arr1: IntArray, arr2: IntArray, d: Int): Int {
            // sorted = [1, 8, 9, 10]
            //   idx:    0  1  2   3      size = 4
            val sorted = arr2.sortedArray()

            // First index i with sorted[i] >= key; sorted.size if every element is < key.
            // Invariant: the answer is always in [lo, hi]; everything left of lo is < key,
            // everything from hi on is >= key.
            //
            // lowerBound(2) on [1, 8, 9, 10]:
            //   lo=0 hi=4 → mid=2 sorted[2]=9  >= 2 → hi=2
            //   lo=0 hi=2 → mid=1 sorted[1]=8  >= 2 → hi=1
            //   lo=0 hi=1 → mid=0 sorted[0]=1  <  2 → lo=1
            //   lo=1 hi=1 → stop, return 1   (8 is the first value >= 2)
            fun lowerBound(key: Int): Int {
                var lo = 0
                var hi = sorted.size

                while (lo < hi) {
                    val mid = lo + (hi - lo) / 2
                    if (sorted[mid] < key) lo = mid + 1 else hi = mid
                }

                return lo
            }

            // For each x the window is [x - d, x + d]; x counts iff no element of sorted is inside it.
            //
            //   x | window  | i = lowerBound(x-d) | sorted[i] | sorted[i] > x+d ? | counted
            //   --+---------+---------------------+-----------+-------------------+--------
            //   4 | [2, 6]  | 1                   | 8         | 8 > 6  → yes      | ✓
            //   5 | [3, 7]  | 1                   | 8         | 8 > 7  → yes      | ✓
            //   8 | [6, 10] | 1                   | 8         | 8 > 10 → no       | ✗ (8 is inside)
            //                                                                       result = 2
            //
            // The i == sorted.size branch: the whole array is below the window,
            // e.g. x = 50 → window [48, 52], lowerBound(48) = 4 == size → counted.
            return arr1.count { x ->
                val i = lowerBound(x - d)
                i == sorted.size || sorted[i] > x + d
            }
        }

        /**
         * ## Reference (lt-ref) — sort + binary search (lower bound)
         *
         * **Restatement.** Count the elements `x` of `arr1` that are *far* from all of `arr2`: no `y` in `arr2`
         * lies in the closed window `[x - d, x + d]`.
         *
         * **Pattern.** "Does any element of a set fall into a range?" is a range-existence query. Sort the set once,
         * and every query becomes one binary search: find the smallest element `>= x - d` (a *lower bound*) and
         * check whether it is also `<= x + d`. It fits because the condition `|x - y| <= d` is exactly a range
         * test on `y`, and a sorted array answers range tests in `O(log m)`.
         *
         * ### Intuition
         * You don't need to compare `x` with every `y` — only with its nearest neighbours in sorted order. If the
         * first `y` at or above the left edge `x - d` is already past the right edge `x + d`, nothing else can be
         * inside the window: everything before it is too small, everything after it is even larger.
         *
         * ### Core concepts
         * - **Abs-value to range rewrite** — `|x - y| <= d` ⇔ `x - d <= y <= x + d`. Turning a distance condition
         *   into an interval is what lets ordering (and thus binary search) apply.
         * - **Lower bound** — the first index `i` with `a[i] >= key` (or `a.size` if none). Unlike "find exact
         *   match", it always returns a well-defined insertion point; it is the building block for range queries.
         * - **Preprocess once, query many** — paying `O(m log m)` to sort one array amortizes across `n` queries
         *   from the other, the general trade-off behind indexing.
         * - **Two-pointer alternative** — if *both* arrays are sorted, the lower bound only moves right as `x`
         *   grows, so a single sweeping pointer replaces the binary searches (`O(n log n + m log m)` total, same
         *   bound, smaller constant).
         *
         * ### Approach
         * 1. Copy and sort `arr2` (copy, so the caller's array is not mutated).
         * 2. For each `x` in `arr1`, binary-search the lower bound `i` of `x - d` in the sorted copy.
         * 3. `x` counts iff `i == sorted.size` (all of `arr2` is below the window) or `sorted[i] > x + d`
         *    (the nearest candidate overshoots the window).
         * 4. Return the count.
         *
         * ### Complexity
         * `O((n + m) log m) time / O(m) space` — sorting `arr2` is `O(m log m)`, then `n` binary searches of
         * `O(log m)` each; the sorted copy takes `O(m)` extra space. (Brute force is `O(n·m)` time, `O(1)` space —
         * fine at these limits of 500×500, but the sorted version is the one that scales.)
         *
         * ### Common pitfalls
         * - **Inclusive boundaries.** The condition is `<= d`, so `y == x - d` and `y == x + d` both *disqualify*
         *   `x`. Search for `>= x - d` and reject on `<= x + d`; an off-by-one here silently miscounts edge cases.
         * - **`d = 0`** — the window collapses to the single point `x`; only exact matches disqualify.
         * - **Checking only one side.** Finding the element closest to `x` from below *or* above is not enough on
         *   its own; the lower bound of the *left edge* handles both sides in one probe.
         * - **`Arrays.binarySearch` semantics** — on a miss it returns `-(insertionPoint) - 1`, and with duplicates
         *   it may return any matching index, not the first. A hand-written lower bound avoids both surprises.
         * - **Mutating input** — sorting `arr2` in place changes the caller's data; sort a copy.
         */
        fun referenceSolution(arr1: IntArray, arr2: IntArray, d: Int): Int {
            val sorted = arr2.sortedArray()

            fun lowerBound(key: Int): Int {
                var lo = 0
                var hi = sorted.size
                while (lo < hi) {
                    val mid = (lo + hi) ushr 1
                    if (sorted[mid] < key) lo = mid + 1 else hi = mid
                }
                return lo
            }

            return arr1.count { x ->
                val i = lowerBound(x - d)
                i == sorted.size || sorted[i] > x + d
            }
        }

    }
}
