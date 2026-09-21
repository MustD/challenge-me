package leetcode.binary_search

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4. Median of Two Sorted Arrays  (https://leetcode.com/problems/median-of-two-sorted-arrays/)
 *
 * Given two sorted arrays nums1 and nums2 of size m and n respectively, return the median of the
 * two sorted arrays.
 *
 * The overall run time complexity should be O(log (m+n)).
 *
 * Constraints:
 * - nums1.length == m
 * - nums2.length == n
 * - 0 <= m <= 1000
 * - 0 <= n <= 1000
 * - 1 <= m + n <= 2000
 * - -10^6 <= nums1[i], nums2[i] <= 10^6
 */
typealias I0004 = (IntArray, IntArray) -> Double

class I0004findMedianSortedArrays {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0004> {

        override val cases = leetcode.testCases<I0004>(
            leetcode.args("[1,3]", "[2]") expects 2.00000,
            leetcode.args("[1,2]", "[3,4]") expects 2.50000,
            leetcode.args("[1,3,5]", "[2,4,6,8]") expects 4.00000,
        )

        @Test
        fun test() = check(::referenceSolution)

        fun findMedianSortedArrays(nums1: IntArray, nums2: IntArray): Double {
            TODO()
        }

        /**
         * ### Restatement
         * Two sorted arrays, sizes `m` and `n`, need to be treated as one combined sorted array
         * without actually merging them — return the median of that combined array in
         * `O(log(min(m, n)))` time.
         *
         * ### Pattern / technique
         * **Binary search on a partition**, not on a value. Instead of merging (`O(m+n)`) or
         * binary-searching for the median *value* directly, binary search for a **split point**
         * (an index) in the smaller array. Each candidate split point deterministically fixes the
         * split point in the other array, so one binary search over one array's indices is enough
         * to pin down "the correct dividing line" for both arrays simultaneously.
         *
         * ### Intuition
         * If you could merge the two arrays, the median is just "the boundary between the left
         * half and the right half" of that merged sequence. A valid boundary — cutting `nums1` into
         * a left part of size `i` and `nums2` into a left part of size `j` — is correct precisely
         * when:
         * 1. **Size balance**: `i + j` equals exactly half the total length (so everything left of
         *    the cut really is "the left half"), and
         * 2. **Value balance**: everything on the left of the cut is `<=` everything on the right of
         *    the cut, i.e. `nums1[i-1] <= nums2[j]` and `nums2[j-1] <= nums1[i]`.
         *
         * Fixing `i` fixes `j = half - i`, so condition 1 is automatic by construction — only
         * condition 2 needs searching for, and it is monotonic in `i` (increasing `i` only ever
         * makes `nums1`'s left edge bigger and `nums2`'s left edge smaller), which is exactly what
         * makes binary search applicable.
         *
         * ### Core concepts
         * - **Partition** — a way of cutting each array into a left prefix and a right suffix such
         *   that the two prefixes together form the left half of the merged array. The whole
         *   algorithm searches for the one correct partition, never materializing the merge.
         * - **Monotonic predicate** — "is `nums1`'s partition too far right?" (`nums1[i-1] >
         *   nums2[j]`) flips from true to false exactly once as `i` increases, which is the
         *   precondition for binary search to work on an index instead of a sorted value.
         * - **Sentinel boundary values** — using `Int.MIN_VALUE`/`Int.MAX_VALUE` for "the element
         *   just past the array edge" lets the same boundary comparisons work uniformly even when a
         *   partition sits at index `0` or at the array's length, without special-casing the edges.
         * - **Search on the smaller array** — binary searching over the smaller array bounds the
         *   work by `log(min(m, n))` and guarantees the derived `j` for the other array always
         *   stays in range `[0, n]`.
         *
         * ### Approach
         * 1. Let `a` be the smaller array (size `m`), `b` the larger (size `n`); swap if needed.
         * 2. Binary search `i` in `[0, m]` — the size of `a`'s left partition.
         * 3. Derive `j = (m + n + 1) / 2 - i` — the size of `b`'s left partition, chosen so the two
         *    partitions together hold the "left half" (using `+1` inside the division handles both
         *    even and odd totals uniformly).
         * 4. Read the four boundary values `aLeft, aRight, bLeft, bRight`, substituting
         *    `Int.MIN_VALUE`/`Int.MAX_VALUE` when a partition sits at an array edge.
         * 5. If `aLeft <= bRight && bLeft <= aRight`, the partition is correct: for odd totals the
         *    median is `max(aLeft, bLeft)`; for even totals it's the average of `max(aLeft, bLeft)`
         *    and `min(aRight, bRight)`.
         * 6. Otherwise, if `aLeft > bRight`, `i` is too large — search the left half (`hi = i - 1`);
         *    if `bLeft > aRight`, `i` is too small — search the right half (`lo = i + 1`).
         *
         * ### Complexity
         * - Time: `O(log(min(m, n)))` — binary search only over the smaller array's index range.
         * - Space: `O(1)` — a handful of scalars, no auxiliary arrays.
         *
         * ### Common pitfalls
         * - Binary searching over the **larger** array instead of the smaller one can push `j`
         *   negative or past `n` before any bounds check catches it.
         * - Forgetting the `+ 1` in `half = (m + n + 1) / 2` breaks the even/odd unification and
         *   silently mis-picks which side of an odd-length merge holds the true middle element.
         * - Off-by-one on empty partitions: `i == 0` or `i == m` (similarly for `j`) must map to
         *   `Int.MIN_VALUE`/`Int.MAX_VALUE`, not to an out-of-bounds array read.
         * - Integer overflow when averaging `aRight`/`bRight` — `Int.MAX_VALUE` sentinels are safe
         *   here in Kotlin since the average is computed as `Double`, but a naive `(a + b) / 2` on
         *   two real `Int.MAX_VALUE`-adjacent values in another language could overflow.
         */
        fun referenceSolution(nums1: IntArray, nums2: IntArray): Double {
            val (a, b) = if (nums1.size <= nums2.size) nums1 to nums2 else nums2 to nums1
            val m = a.size
            val n = b.size
            val half = (m + n + 1) / 2

            var lo = 0
            var hi = m
            while (lo <= hi) {
                val i = (lo + hi) / 2
                val j = half - i

                val aLeft = if (i == 0) Int.MIN_VALUE else a[i - 1]
                val aRight = if (i == m) Int.MAX_VALUE else a[i]
                val bLeft = if (j == 0) Int.MIN_VALUE else b[j - 1]
                val bRight = if (j == n) Int.MAX_VALUE else b[j]

                when {
                    aLeft > bRight -> hi = i - 1
                    bLeft > aRight -> lo = i + 1
                    (m + n) % 2 == 1 -> return maxOf(aLeft, bLeft).toDouble()
                    else -> return (maxOf(aLeft, bLeft) + minOf(aRight, bRight)) / 2.0
                }
            }
            error("Input arrays must be sorted")
        }

    }
}
