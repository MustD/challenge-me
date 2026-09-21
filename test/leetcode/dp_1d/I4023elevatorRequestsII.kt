package leetcode.dp_1d

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4023. Elevator Requests II  (https://leetcode.com/problems/elevator-requests-ii/)
 *
 * You are given an integer n denoting the number of floors in a building, where the floors are
 * numbered from 0 to n - 1. You are also given an integer start, representing the floor where the
 * elevator begins, and an integer array requests, where requests[i] is a floor that the elevator is
 * requested to reach.
 *
 * All floors in requests are distinct.
 *
 * At time 0, the elevator is on floor start, and all requests are made simultaneously. During each
 * second before all requests are fulfilled, the elevator moves exactly one floor, either up or down.
 * A request is fulfilled the instant the elevator reaches that floor.
 *
 * For each second that a request remains unfulfilled, you receive 1 penalty. Equivalently, a request
 * fulfilled at time t contributes t to the total penalty.
 *
 * If start appears in requests, that request is fulfilled at time 0.
 *
 * Return the minimum total penalty across all requests.
 *
 * Constraints:
 * - 1 <= n <= 10^9
 * - 1 <= requests.length <= 1500
 * - 0 <= start, requests[i] <= n - 1
 * - All values in requests are distinct
 */
typealias I4023 = (Int, Int, IntArray) -> Long

class I4023elevatorRequestsII {

    @Nested
    inner class Solution : leetcode.ProblemTest<I4023> {

        override val cases = leetcode.testCases<I4023>(
            leetcode.args(6, 4, "[1,5]") expects 6L,
            leetcode.args(8, 3, "[3,7,1]") expects 10L,
            leetcode.args(10, 5, "[0,2,9]") expects 22L,
        )

        @Test
        fun test() = check(::referenceSolution)

        fun elevatorRequestsII(n: Int, start: Int, requests: IntArray): Long {
            TODO()
        }

        /**
         * ### Restatement
         * All requests are issued at t = 0 and the elevator moves 1 floor per second. A request served at time t costs
         * t. Choose the visiting order (a path on a line) that minimises the sum of service times.
         *
         * ### Pattern
         * Interval DP on a line ("minimum latency" / travelling repairman on a path). Sort the floors; the set of
         * served floors is always a contiguous block around `start`, so the state is (block, which end I stand on).
         *
         * ### Intuition
         * Re-count the penalty by moves instead of by requests: while the elevator travels a distance d, every request
         * that is still unserved waits d more seconds. So a move of length d made while `rem` requests are unserved
         * costs d * rem. Passing over a floor serves it for free, so an optimal path never turns around inside
         * the visited block: it only ever extends the block by one floor to the left or to the right.
         *
         * ### Core concepts
         * - Interval DP: state = a contiguous range [l..r] already handled, grown one element at a time.
         * - Position flag: the value depends on whether we are at l or at r, so keep two values per range.
         * - Cost by "who is still waiting": a delay is charged to everything not yet served, i.e. d * remaining,
         *   which makes the cost of a step depend only on the state, not on history.
         * - Contiguity argument: on a line, passing through a point serves it, so the served set is an interval.
         *
         * ### Approach
         * 1. pts = sorted distinct floors of requests plus `start`; s = index of `start`. If `start` is not itself a
         *    request it is a weightless point (extra = 1).
         * 2. dp over range length. atL[l] / atR[l] = minimal penalty so far with [l..l+len-1] served and the
         *    elevator on the left / right end. Base: the range {s} costs 0.
         * 3. For range [l..r] with rem = m - (len - extra) unserved requests, extending to l-1 costs
         *    (pos - pts[l-1]) * rem, extending to r+1 costs (pts[r+1] - pos) * rem, where pos is the current end.
         * 4. Answer = min(atL[0], atR[0]) for the full range.
         *
         * ### Complexity
         * Time O(m^2) (m = requests.size <= 1500, about 2.25M states); space O(m) with the rolling array over length.
         *
         * ### Common pitfalls
         * - Overflow: the answer reaches ~1500 * 1500 * 1e9, so use Long everywhere and multiply as Long.
         * - `start` may or may not be a request: it must be in the point list either way, but only counts as waiting
         *   if it is a request (already served at t = 0 in that case).
         * - Charge each move with the number of requests unserved BEFORE it, including the one being reached.
         * - Requests are distinct, but `start` can coincide with one, so de-duplicate when adding it.
         */
        fun referenceSolution(n: Int, start: Int, requests: IntArray): Long {
            val pts = (requests.toList() + start).distinct().sorted()
            val p = pts.size
            val m = requests.size
            val s = pts.indexOf(start)
            val extra = if (start in requests) 0 else 1
            val inf = Long.MAX_VALUE / 4

            var atL = LongArray(p) { inf }
            var atR = LongArray(p) { inf }
            atL[s] = 0
            atR[s] = 0

            for (len in 1 until p) {
                val nextL = LongArray(p) { inf }
                val nextR = LongArray(p) { inf }
                val rem = (m - (len - extra)).toLong()
                for (l in 0..p - len) {
                    val r = l + len - 1
                    if (l > s || r < s) continue
                    val cl = atL[l]
                    val cr = atR[l]
                    if (l > 0) {
                        val cost = minOf(
                            cl + (pts[l] - pts[l - 1]) * rem,
                            cr + (pts[r] - pts[l - 1]) * rem,
                        )
                        if (cost < nextL[l - 1]) nextL[l - 1] = cost
                    }
                    if (r + 1 < p) {
                        val cost = minOf(
                            cl + (pts[r + 1] - pts[l]) * rem,
                            cr + (pts[r + 1] - pts[r]) * rem,
                        )
                        if (cost < nextR[l]) nextR[l] = cost
                    }
                }
                atL = nextL
                atR = nextR
            }
            return minOf(atL[0], atR[0])
        }

    }
}
