package leetcode.binary_search

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 74. Search a 2D Matrix  (https://leetcode.com/problems/search-a-2d-matrix/)
 *
 * You are given an m x n integer matrix with two properties:
 *   1. Each row is sorted in non-decreasing (left-to-right) order.
 *   2. The first integer of each row is greater than the last integer of the previous row.
 * Given an integer `target`, return true if `target` is present in the matrix, and false otherwise.
 *
 * Constraints:
 * - m == matrix.length, n == matrix[i].length
 * - 1 <= m, n <= 100  (the matrix is never empty)
 * - -10^4 <= matrix[i][j], target <= 10^4
 * - Must run in O(log(m * n)) time. The two properties mean the whole matrix, read row by row,
 *   is one globally sorted sequence — so it can be treated as a virtual 1D sorted array of
 *   length m*n and binary-searched, mapping index k -> (k / n, k % n).
 */
typealias I0074 = (Array<IntArray>, Int) -> Boolean

class I0074searchMatrix {

    @Nested
    inner class Solution : ProblemTest<I0074> {

        override val cases = testCases<I0074>(
            args("[[1,3,5,7],[10,11,16,20],[23,30,34,60]]", 3) expects true,
            args("[[1,3,5,7],[10,11,16,20],[23,30,34,60]]", 13) expects false,
            // edge cases
            args("[[1]]", 1) expects true,
            args("[[1]]", 0) expects false,
            args("[[1,3]]", 3) expects true,
            args("[[1,3,5,7],[10,11,16,20],[23,30,34,60]]", 60) expects true,  // last element
            args("[[1,3,5,7],[10,11,16,20],[23,30,34,60]]", 1) expects true,   // first element
            args("[[1,3,5,7],[10,11,16,20],[23,30,34,60]]", 100) expects false, // above range
        )

        @Test
        fun test() = check(::searchMatrix, ::solutionLegacy)


        /**
         * ## Analysis — two-stage (row-then-column) binary search
         *
         * ### Pattern
         * "Nested binary search." Instead of flattening the matrix into one virtual 1D
         * array (the header docstring's approach), this narrows in two independent stages:
         * 1. Binary-search the *rows* for the one row whose `[first .. last]` range can
         *    contain `target` (lines 61–72).
         * 2. Binary-search *within* that row for the value (lines 75–84).
         *
         * ### Time — O(log m + log n) = O(log(m·n))  ✅ meets the required bound
         * - Stage 1 halves the row count each iteration → O(log m).
         * - Stage 2 halves the column count each iteration → O(log n).
         * - log m + log n = log(m·n), so the two stages together hit the problem's
         *   O(log(m·n)) requirement — asymptotically identical to the single flattened
         *   binary search, just expressed as two loops instead of one with index mapping
         *   `k -> (k / n, k % n)`.
         *
         * ### Space — O(1) auxiliary
         * Both stages are iterative (no recursion, so no call-stack growth). The `run {}`
         * block returns a *reference* to the candidate row (`return@run matrix[mid]`), not
         * a copy — no allocation. Only a handful of Int index variables are used.
         *
         * ### Correctness notes
         * - The row search relies on the *second* matrix property (each row's first element
         *   exceeds the previous row's last), which makes the row ranges disjoint and
         *   ordered — so at most one row can contain `target`, and comparing against a
         *   row's `first`/`last` is enough to decide direction.
         * - `mid = lo + (hi - lo) / 2` avoids `(lo + hi)` integer overflow. Not strictly
         *   needed here (indices ≤ 100) but a good reflex.
         * - Edge cases the tests exercise and the code handles cleanly: 1×1 matrix,
         *   single row, the very first / very last element, and targets below/above the
         *   whole range (stage 1 falls through to `null` → `return false`).
         * - The commented-out `firstOrNull { ... }` (lines 46–48) is the O(m) linear row
         *   scan; replacing it with the binary search on lines 50–61 is exactly what
         *   upgrades stage 1 from O(m) to O(log m). Good instinct to swap it.
         *
         * ### Alternatives & trade-offs
         * - **Single flattened binary search** (the header's idea): one loop over
         *   `0 .. m*n-1`, reading `matrix[mid / n][mid % n]`. Same O(log(m·n)) / O(1).
         *   Slightly less code; costs a div+mod per step instead of two-stage indexing.
         *   Pure preference — neither is faster asymptotically.
         * - **Linear row scan + binary search in row**: O(m + log n). Simpler to write,
         *   but strictly worse (this is the commented-out version).
         * - **Staircase from top-right corner**: O(m + n). Doesn't use the "globally
         *   sorted" property at all — it only needs rows and columns individually sorted,
         *   which is the *weaker* guarantee of LC 240 ("Search a 2D Matrix II"). More
         *   general, but slower here. Worth knowing because 74 and 240 look identical but
         *   demand different algorithms.
         * - This solution is **optimal**: comparison-based search over m·n sorted elements
         *   has an Ω(log(m·n)) lower bound, which O(log m + log n) matches.
         *
         * ### Parallelism
         * Not worth it, and the reason is the teaching point: binary search is inherently
         * sequential — each step's comparison decides the next bound (a hard data
         * dependency), so there's nothing to run concurrently. The input is also tiny
         * (≤ 100×100 = 10⁴ cells ⇒ ~14 comparisons total); thread/overhead would dwarf any
         * gain. Parallel search only pays off on *unsorted* data (parallel scan) or data
         * far too large for one machine.
         *
         * ### Real-world
         * This two-level "find the block, then find within the block" shape is exactly how
         * B-tree / database indexes work: descend to the correct leaf page, then search
         * inside that page. The "treat 2D storage as a 1D sorted range" trick mirrors
         * row-major memory layout and disk-page addressing. In production you'd reach for
         * a library routine (`Arrays.binarySearch`, `List.binarySearch`) rather than
         * hand-rolling; the interview value is understanding *why* the index math works and
         * how cache/page locality (touching one contiguous row) makes stage 2 cheap.
         */
        fun searchMatrix(matrix: Array<IntArray>, target: Int): Boolean {

            val searchTarget = run {
                var lo = 0
                var hi = matrix.lastIndex
                while (lo <= hi) {
                    val mid = lo + (hi - lo) / 2
                    if (matrix[mid].first() <= target && matrix[mid].last() >= target) return@run matrix[mid]

                    val goLeft = target < matrix[mid].first()
                    if (goLeft) hi = mid - 1
                    else lo = mid + 1
                }
                null
            } ?: return false


            var lo = 0
            var hi = searchTarget.lastIndex
            while (lo <= hi) {
                val mid = lo + (hi - lo) / 2
                if (target == searchTarget[mid]) return true

                val goLeft = target < searchTarget[mid]
                if (goLeft) hi = mid - 1
                else lo = mid + 1
            }
            return false
        }


        private fun solutionLegacy(matrix: Array<IntArray>, target: Int): Boolean {
            fun binS(s: Int, e: Int, t: Int, aIdx: Int): Boolean {
                if (s >= e) return matrix[aIdx][s] == t
                val mid = (s + e) / 2
                if (matrix[aIdx][mid] == t) return true
                return if (matrix[aIdx][mid] > t) {
                    binS(s, mid - 1, t, aIdx) //up part search
                } else {
                    binS(mid + 1, e, t, aIdx) //down part search
                }
            }
            matrix.forEachIndexed { aIdx, ints ->
                if (target < matrix[aIdx][0]) return@forEachIndexed
                if (binS(0, ints.lastIndex, target, aIdx)) return true
            }
            return false
        }

    }
}
