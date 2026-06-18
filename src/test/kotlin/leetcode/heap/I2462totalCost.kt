package leetcode.heap

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test

/**
 * 2462. Total Cost to Hire K Workers  (https://leetcode.com/problems/total-cost-to-hire-k-workers/)
 *
 * You are given a 0-indexed integer array `costs` where `costs[i]` is the cost of hiring the i-th
 * worker, and two integers `k` and `candidates`. You run `k` hiring sessions, hiring exactly one
 * worker per session. In each session, choose the worker with the lowest cost among either the
 * first `candidates` workers or the last `candidates` workers (from the remaining, not-yet-hired
 * workers). Break ties by the smallest index. If fewer than `candidates` workers remain, choose the
 * lowest cost among all of them. A worker can only be chosen once. Return the total cost to hire
 * exactly `k` workers.
 *
 * Constraints:
 * - 1 <= costs.length <= 10^5
 * - 1 <= costs[i] <= 10^5
 * - 1 <= k, candidates <= costs.length
 * - Note: total cost can reach ~10^5 * 10^5 = 10^10, which overflows Int — return type is Long.
 */
typealias I2462 = (IntArray, Int, Int) -> Long

class I2462totalCost {

    @Nested
    inner class Solution : ProblemTest<I2462> {

        override val cases = testCases<I2462>(
            args("[17,12,10,2,7,2,11,20,8]", 3, 4) expects 11L,
            args("[1,2,4,1]", 3, 3) expects 4L,
            args("[10]", 1, 1) expects 10L,
            args(
                """[18,64,12,21,21,78,36,58,88,58,99,26,92,91,53,10,24,25,20,92,73,63,51,65,87,6,17,32,14,42,46,65,43,9,75]""",
                13,
                23
            ) expects 223L,
        )


        @Test
        fun test() = check(::totalCost, ::referenceSolution)

        /**
         * Pattern: TWO MIN-HEAPS with a converging two-pointer frontier (`minP` walks up from the
         * front, `maxP` walks down from the back). This is the standard — and asymptotically optimal
         * — approach for this problem.
         *
         * Complexity
         * ----------
         * Time  O((k + candidates) · log candidates) — seeding does up to O(candidates) pushes, then
         *       each of the k sessions does one poll plus at most one add. Neither heap ever exceeds
         *       `candidates` elements, so every heap op is O(log candidates).
         * Space O(candidates) — the two heaps; no recursion, so no extra stack term.
         *
         * The single most important correctness insight: the tie-break hinges on the STRICT `>` at
         * line 64. Because the two windows can never overlap, every element in `leftH` has a smaller
         * original index than anything in `rightH`. So when the two heap tops are equal, `>` is
         * false, you fall to the `else` branch and draw from the left heap — which is exactly
         * "smallest original index wins", the tie-break the problem demands. Switching that `>` to
         * `>=` would silently break the tie-break rule (you'd start preferring the higher-index
         * worker on ties).
         *
         * Why `Long`: the worst case is ~10^5 workers each costing up to 10^5, i.e. a sum near 1e10,
         * which overflows a 32-bit `Int`. Accumulating into `result: Long` is the right call.
         *
         * Implementation notes specific to your version:
         * - `meet = { minP > maxP }` (strict) is correct: the un-pooled middle is the *inclusive*
         *   range [minP, maxP], so the windows have only fully met once minP passes maxP.
         * - The seeding loops read-then-advance (`costs[minP++]`) and the refill branches advance
         *   *after* adding (`costs[maxP]; maxP--`), so the same middle element is never both pooled
         *   and skipped — the off-by-one that bit earlier drafts is gone here.
         * - The `?: Int.MAX_VALUE` sentinels let an empty side lose every comparison gracefully once
         *   the middle is exhausted, instead of NPE-ing on a null `peek()`.
         *
         * Parallelism doesn't help: the algorithm is inherently sequential — each hire mutates the
         * frontier the next hire reads from, so there's a true data dependency across the k sessions.
         * The work per session is already O(log candidates); there's nothing to fan out.
         *
         * Real-world framing: this is "online / streaming top-k from both ends of a shrinking pool",
         * the same shape as load-balancers picking the cheapest of a head/tail candidate set, or
         * distributed schedulers repeatedly committing the lowest-cost task from bounded front/back
         * windows of a queue.
         */
        fun totalCost(costs: IntArray, k: Int, candidates: Int): Long {
            var result = 0L
            var minP = 0
            var maxP = costs.lastIndex

            val leftH = PriorityQueue<Int>()
            val rightH = PriorityQueue<Int>()

            val meet = { minP > maxP }

            while (minP < candidates && meet().not()) leftH.add(costs[minP++])
            while (costs.lastIndex - maxP < candidates && meet().not()) rightH.add(costs[maxP--])

            repeat(k) {
                if ((leftH.peek() ?: Int.MAX_VALUE) > (rightH.peek() ?: Int.MAX_VALUE)) {
                    result += rightH.poll()
                    if (meet().not()) {
                        rightH.add(costs[maxP]); maxP--
                    }

                } else {
                    result += leftH.poll()
                    if (meet().not()) {
                        leftH.add(costs[minP]); minP++
                    }
                }
            }
            return result
        }

        /**
         * Pattern: TWO MIN-HEAPS with a converging two-pointer frontier.
         *
         * Intuition
         * ---------
         * Every session we may pick the cheapest worker from the first `candidates` OR the last
         * `candidates` of the *remaining* workers. "Repeatedly extract the minimum" screams a
         * min-heap. The twist is that the candidate window shrinks from both ends as workers get
         * hired, so we maintain TWO heaps:
         *   - `left`  holds the front window's costs,
         *   - `right` holds the back window's costs.
         * Two pointers `lo` and `hi` mark the next un-pooled element on each side. They walk toward
         * each other; when they cross, every remaining worker is already inside one of the heaps.
         *
         * Approach
         * --------
         * 1. Seed: push the first `candidates` items into `left` (advancing `lo`) and the last
         *    `candidates` items into `right` (advancing `hi` down), but never let the two windows
         *    overlap — `lo` must stay <= `hi`. This naturally handles `2*candidates > n`.
         * 2. For each of k sessions: peek both heap tops. Pick the smaller; on a tie pick `left`
         *    (front has the smaller original index, satisfying the tie-break rule). Add its cost.
         * 3. Refill the side we drew from: if `lo <= hi`, push `costs[lo]`/`costs[hi]` and move the
         *    pointer inward. This keeps each window topped up at `candidates` until the middle runs
         *    out. Heaps may be empty on one side near the end — guard with sentinels / size checks.
         *
         * Why the tie-break works: a worker pulled from `left` always had a lower original index
         * than anything in `right` (windows never overlap), so preferring `left` on equal cost is
         * exactly "smallest index wins".
         *
         * Complexity
         * ----------
         * Time  O((k + candidates) log candidates) — each heap holds <= candidates elements, and we
         *       do O(k) pops plus O(candidates) seeding pushes, each O(log candidates).
         * Space O(candidates) for the two heaps.
         *
         * Pitfalls
         * --------
         * - Overflow: sum can be ~1e10, so accumulate into a Long (return type is already Long).
         * - Double-counting / overlap when 2*candidates > n: guard every refill with `lo <= hi`.
         * - Tie-break by smallest index — prefer the left heap when tops are equal.
         * - Don't forget the `right` heap can be empty (or `left` can be) once the middle is
         *   exhausted; compare with a sentinel like Int.MAX_VALUE.
         */
        fun referenceSolution(costs: IntArray, k: Int, candidates: Int): Long {
            val n = costs.size
            val left = PriorityQueue<Int>()
            val right = PriorityQueue<Int>()

            var lo = 0
            var hi = n - 1
            // Seed both windows without overlapping.
            while (lo < candidates && lo <= hi) left.add(costs[lo++])
            while (n - 1 - hi < candidates && lo <= hi) right.add(costs[hi--])

            var total = 0L
            repeat(k) {
                val l = left.peek() ?: Int.MAX_VALUE
                val r = right.peek() ?: Int.MAX_VALUE
                if (l <= r) {
                    total += left.poll()
                    if (lo <= hi) left.add(costs[lo++])
                } else {
                    total += right.poll()
                    if (lo <= hi) right.add(costs[hi--])
                }
            }
            return total
        }

    }
}
