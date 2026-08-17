package leetcode.heap

import leetcode.expects
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test

/**
 * 502. IPO  (https://leetcode.com/problems/ipo/)
 *
 * You start with initial capital `w` and may complete at most `k` distinct projects. The i-th project
 * has a pure profit `profits[i]` and requires a minimum capital of `capital[i]` to start. You can only
 * start a project whose required capital is <= your current capital; on finishing it you keep its profit,
 * adding it to your capital (so completing a project can unlock more expensive projects). Return the
 * maximum capital you can have after finishing at most `k` projects.
 *
 * Constraints:
 * - 1 <= k <= 10^5
 * - 0 <= w <= 10^9
 * - n == profits.length == capital.length, 1 <= n <= 10^5
 * - 0 <= profits[i] <= 10^4
 * - 0 <= capital[i] <= 10^9
 * - You never lose capital (profits are non-negative), so it's never worse to do a project you can afford.
 * - Final capital can exceed Int range only if profits summed carelessly — but with the given bounds
 *   (k * max profit + w) it fits in Int; still, watch how you accumulate.
 */
typealias I0502 = (Int, Int, IntArray, IntArray) -> Int

class I0502findMaximizedCapital {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0502> {

        override val cases = leetcode.testCases<I0502>(
            leetcode.args(2, 0, "[1,2,3]", "[0,1,1]") expects 4,
            leetcode.args(3, 0, "[1,2,3]", "[0,1,2]") expects 6,
            // Can't afford any project -> stay at initial capital.
            leetcode.args(1, 0, "[5]", "[1]") expects 0,
            // Single affordable project.
            leetcode.args(1, 1, "[5]", "[1]") expects 6,
        )

        @Test
        fun test() = check(::findMaximizedCapital, ::referenceSolution)

        fun findMaximizedCapital(k: Int, w: Int, profits: IntArray, capital: IntArray): Int {
            val byCapital = PriorityQueue<Int>(compareBy { capital[it] })
            profits.indices.forEach { byCapital.add(it) }
            val affordable = PriorityQueue<Int>(reverseOrder())

            var result = w
            repeat(k) {
                while (byCapital.isNotEmpty() && capital[byCapital.peek()] <= result) {
                    affordable.add(profits[byCapital.poll()])
                }
                if (affordable.isEmpty()) return result
                result += affordable.poll()
            }

            return result
        }

        /**
         * Pattern: greedy with two heaps ("unlock, then take the best").
         *
         * Intuition: at every step you want the single most profitable project you can currently
         * afford. Profits are non-negative, so doing a project never hurts and can only raise your
         * capital, which unlocks strictly more (never fewer) projects for the next step. So the
         * locally-greedy choice — take the max-profit affordable project each round — is globally
         * optimal. You never need to "save" a cheap project for later; anything affordable now stays
         * affordable later.
         *
         * The two-heap structure supports this efficiently:
         *  - `byCapital` — a MIN-heap keyed by required capital. It hands you the cheapest not-yet-
         *    unlocked project so you can cheaply test "is anything newly affordable?".
         *  - `affordable` — a MAX-heap keyed by profit, holding every project you can currently start.
         *
         * Approach:
         *  1. Push all projects into `byCapital` (cheapest capital on top).
         *  2. Repeat up to k times:
         *     a. Drain every project whose required capital <= current capital from `byCapital` into
         *        `affordable` (they are now "unlocked").
         *     b. If `affordable` is empty, no project is reachable — stop early (more rounds won't help).
         *     c. Otherwise pop the max profit and add it to capital.
         *  3. Return the accumulated capital.
         *
         * Complexity: O(n log n + k log n) time — each project is pushed/popped across the heaps at
         * most once, plus up to k max-extractions. O(n) space for the heaps.
         *
         * Common pitfalls:
         *  - THE MAX-HEAP. This is exactly what the attempt above gets wrong: `PriorityQueue<Int>()` is
         *    a MIN-heap, so it takes the *smallest* affordable profit each round. On case
         *    (k=2, w=0, profits=[1,2,3], capital=[0,1,1]) that yields 1 then 2 -> 3, but taking the max
         *    yields 1 then 3 -> 4. Fix: `PriorityQueue(reverseOrder())` (or compareByDescending).
         *  - Stopping early: if nothing is affordable this round, break — the loop's `?: 0` "silently
         *    do nothing" also works but wastes iterations up to k (10^5).
         *  - Re-scanning all projects every round is O(nk); the point of `byCapital` is to advance the
         *    capital frontier only forward, so each project is considered once.
         */
        fun referenceSolution(k: Int, w: Int, profits: IntArray, capital: IntArray): Int {
            val byCapital = PriorityQueue<Int>(compareBy { capital[it] })       // min-heap by required capital
            profits.indices.forEach { byCapital.add(it) }
            val affordable = PriorityQueue<Int>(reverseOrder())                 // max-heap by profit

            var result = w
            repeat(k) {
                while (byCapital.isNotEmpty() && capital[byCapital.peek()] <= result) {
                    affordable.add(profits[byCapital.poll()])
                }
                if (affordable.isEmpty()) return result                         // nothing reachable — done
                result += affordable.poll()
            }
            return result
        }

    }
}
