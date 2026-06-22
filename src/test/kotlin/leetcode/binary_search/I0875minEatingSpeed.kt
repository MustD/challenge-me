package leetcode.binary_search

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.math.ceil
import kotlin.test.Test

/**
 * 875. Koko Eating Bananas  (https://leetcode.com/problems/koko-eating-bananas/)
 *
 * There are n piles of bananas; the i-th pile has piles[i] bananas. The guards leave and return
 * in h hours. Koko picks an eating speed k (bananas/hour). Each hour she chooses a single pile and
 * eats k bananas from it; if that pile has fewer than k bananas she eats the whole pile and is done
 * for that hour (she does not move on to another pile within the same hour).
 *
 * Return the minimum integer k such that she can finish all the bananas within h hours.
 *
 * Constraints:
 * - 1 <= piles.length <= 10^4
 * - piles.length <= h <= 10^9
 * - 1 <= piles[i] <= 10^9   (note: ceil(pile / k) summed across piles can overflow Int — use Long)
 */
typealias I0875 = (IntArray, Int) -> Int

class I0875minEatingSpeed {

    @Nested
    inner class Solution : ProblemTest<I0875> {

        override val cases = testCases<I0875>(
            args("[3,6,7,11]", 8) expects 4,
            args("[3, 6, 9, 12]", 10) expects 3,
            args("[30,11,23,4,20]", 5) expects 30,
            args("[30,11,23,4,20]", 6) expects 23,
            // edge: single pile, exactly one hour -> must eat the whole pile in that hour
            args("[5]", 1) expects 5,
        )

        @Test
        fun test() = check(::minEatingSpeed, ::referenceSolution)

        /**
         * minEatingSpeed — linear scan over candidate speeds (VERIFIED CORRECT: passes all 5 cases).
         *
         * Pattern: "search the answer space" — but here the search is a *linear* scan, not yet a
         * binary search. We don't search the piles; we search over the possible values of k, the
         * monotone quantity being `hours(k) = sum(ceil(pile/k))`. hours(k) is monotonically
         * non-increasing in k: faster speed never costs more hours. So the smallest k with
         * hours(k) <= h is what we want, and the first k that satisfies it (scanning low→high) is
         * the minimum.
         *
         * Time: O(n * R) where n = piles.size and R = max - min is the width of the candidate range.
         *   - `min = sum(pile/h)` is a lower bound on the answer (a coarse average-rate estimate).
         *   - `max = max(piles)` is a valid upper bound: at k = max(piles) every pile takes exactly
         *     1 hour, total n hours, and n <= h is guaranteed by the early return, so max always works.
         *   - The loop body recomputes `hours` in O(n) for each of up to R candidate speeds.
         *   With piles[i] up to 1e9, R can be ~1e9, so worst case ≈ O(n * 1e9) — too slow for the
         *   real constraints (TLE on LeetCode), even though it's correct. This is the motivation for
         *   the binary-search variant below.
         *
         * Space: O(1) auxiliary (sumOf accumulates in a register; no recursion, no collections).
         *
         * Correctness / edge cases:
         *   - `piles.size > h` → return -1 (impossible: needs at least one hour per pile). LeetCode
         *     guarantees piles.length <= h so this never triggers there, but it's a sound guard.
         *   - Single pile, one hour ([5], 1): min=5, max=5, the loop runs once at k=5, hours=1<=1 → 5. ✓
         *   - ceil via Double: pile/k up to 1e9 is exactly representable in a Double (< 2^53), so the
         *     ceil is exact here. Summing n=1e4 such values stays well under 2^53 too, so the
         *     `.toInt()` on the sum is safe in practice — though Long ceilDiv `(pile + k - 1) / k`
         *     avoids the floating-point reasoning entirely and is the idiom to prefer in production.
         *
         * Parallelism: the inner `hours(k)` sum is an embarrassingly-parallel map-reduce over piles,
         *   and distinct candidate k values are independent — so this is parallelizable in principle.
         *   In practice it isn't worth it: n is tiny (<=1e4) and the real fix is algorithmic (binary
         *   search drops R from ~1e9 to ~30 iterations), which dwarfs any constant-factor SIMD/thread
         *   speedup. Replacing an O(R) scan with O(log R) beats parallelizing the O(R) scan.
         *
         * Alternative (the better one): binary-search the answer space on the monotone predicate
         *   `hours(k) <= h`. O(n log R) time, O(1) space — see minEatingSpeedBin (intended optimal).
         *
         * Real-world: "binary search on a monotone cost function" is a staple of capacity/SLA tuning —
         *   e.g. find the minimum number of machines/threads/shards so a workload finishes within a
         *   deadline, the minimum bandwidth to drain a queue in time, or rate-limit thresholds. The
         *   cost evaluation (here hours(k)) is often the expensive part and may itself be a simulation
         *   or a query, which is exactly why you want O(log R) evaluations, not O(R).
         */
        fun minEatingSpeed(piles: IntArray, h: Int): Int {
            if (piles.size > h) return -1

            val min = piles.sumOf { it.toLong() / h }.toInt() // O(n)
            val max = piles.max() //O(m)

            for (k in min..max) { //O(n * m)
                val hours = piles.sumOf { ceil(it.toDouble() / k) }.toInt() //O(n)
                if (hours <= h) return k
            }

            return -1
        }

        /**
         * referenceSolution — clean binary search on the answer (VERIFIED CORRECT).
         *
         * Problem in plain terms: Koko eats from exactly one pile per hour at speed k. A pile of
         * size p therefore takes ceil(p / k) hours (the last hour may be partial and wasted). Total
         * hours at speed k is hours(k) = sum over piles of ceil(pile / k). Find the smallest integer
         * k with hours(k) <= h.
         *
         * Pattern: BINARY SEARCH ON THE ANSWER. The key property is monotonicity — hours(k) is
         * non-increasing in k (eat faster, never need more hours). So the predicate `hours(k) <= h`
         * is FALSE for small k and flips to TRUE once and stays TRUE. We binary-search for that
         * first TRUE, i.e. the boundary. This is the same shape as "find the leftmost element
         * satisfying P" / lower_bound.
         *
         * Approach:
         *   - Search interval [lo, hi] = [1, max(piles)].
         *       lo = 1 is the only universally safe lower bound (k must be >= 1).
         *       hi = max(piles) is always feasible: every pile then takes exactly 1 hour, total
         *       n hours, and n <= h is guaranteed by the constraints, so hi satisfies the predicate.
         *   - Standard leftmost-true loop: while (lo < hi) { mid = lo + (hi - lo) / 2;
         *       if hours(mid) <= h shrink right (hi = mid) else lo = mid + 1 }. Return lo.
         *   - Because hi starts feasible and we only ever set hi = mid (a feasible point) or move lo
         *       up past infeasible points, the invariant "hi is always feasible" holds, so lo == hi
         *       at the end is the minimal feasible k. No special case for the single-element interval.
         *
         * Complexity: O(n log R) time (R = max(piles); ~30 iterations of an O(n) hours() probe),
         *   O(1) space. Optimal: must read all piles (Ω(n)) and search a range of size R (Ω(log R)).
         *
         * Pitfalls this avoids (the three that sink minEatingSpeedBin above):
         *   1. lo = 1, never the average estimate sum(pile/h) — the true answer can exceed the average
         *      when one pile dominates, so the average is an unsafe lower bound for binary search.
         *   2. Loop runs correctly even when lo == hi initially (e.g. [5], 1): the while is skipped and
         *      we `return lo` = the seeded feasible bound, instead of falling through to -1.
         *   3. Clean convergence: `lo = mid + 1` on the infeasible branch guarantees progress; no
         *      `k == min` stall, so no linear-scan fallback is needed.
         *   4. Overflow: hours() accumulates in Long via ceilDiv (pile + k - 1) / k — no Double, no
         *      Int overflow even though sum of ceils across 1e4 piles of 1e9 can exceed Int range.
         */
        fun referenceSolution(piles: IntArray, h: Int): Int {
            fun hours(k: Int): Long = piles.sumOf { (it.toLong() + k - 1) / k } // ceil(pile / k) in Long, no Double

            var lo = 1
            var hi = piles.max()
            while (lo < hi) {
                val mid = lo + (hi - lo) / 2
                if (hours(mid) <= h) hi = mid else lo = mid + 1
            }
            return lo
        }
    }
}
