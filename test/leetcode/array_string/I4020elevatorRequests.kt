package leetcode.array_string

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.math.abs
import kotlin.test.Test

/**
 * 4020. Elevator Requests I  (https://leetcode.com/problems/elevator-requests-i/)
 *
 * You are given an integer n denoting the number of floors in a building, where the floors are
 * numbered from 0 to n - 1.
 *
 * You are also given an integer array requests, where requests represents the sequence of floor
 * requests.
 *
 * An elevator starts at floor 0 and follows these rules:
 * - Moves one floor per second
 * - Serves requests in the given order
 * - If already on the requested floor, no movement occurs
 * - Immediately starts moving toward the next request after serving one
 *
 * Return the total time in seconds to serve all requests.
 *
 * Constraints:
 * - 1 <= n <= 100
 * - 1 <= requests.length <= 100
 * - 0 <= requests[i] <= n - 1
 */
typealias I4020 = (Int, IntArray) -> Int

class I4020elevatorRequests {

    @Nested
    inner class Solution : leetcode.ProblemTest<I4020> {

        override val cases = leetcode.testCases<I4020>(
            leetcode.args(5, "[2,1,4,3]") expects 7,
            leetcode.args(3, "[2,0,0]") expects 4,
            leetcode.args(1, "[0]") expects 0, // n = 1: only floor 0 exists, no movement possible
            leetcode.args(5, "[0]") expects 0, // first request equals starting floor 0
            leetcode.args(100, "[99]") expects 99, // max floor at the constraint ceiling (n - 1)
            leetcode.args(
                100,
                "[99,0,99,0]"
            ) expects 396, // repeatedly bouncing between the two extreme floors
            leetcode.args(
                5,
                "[3,3,3]"
            ) expects 3, // consecutive duplicate requests cost nothing after the first
        )

        @Test
        fun test() = check(::elevatorRequests)

        /**
         * ## Analysis
         *
         * **Pattern:** single linear scan accumulating a running "distance traveled" — no state beyond
         * the current position is needed because each request only depends on where the elevator
         * currently is, not on any history before that. This is the same shape as summing
         * `abs(nums[i] - nums[i - 1])` over a path, e.g. "distance traveled by a robot" problems.
         *
         * **Time complexity:** O(requests.size). One pass over `requests`, O(1) work per element
         * (one subtraction, one `abs`, one addition). `n` itself is never used in the computation —
         * it only bounds the legal values of `requests[i]` per the constraints, so the elevator never
         * needs to know the building size to compute travel time.
         *
         * **Space complexity:** O(1) auxiliary — just `total` and `curFloor`; no recursion, no
         * extra collections. (`forEach` is iterative here, not recursive, so no stack growth.)
         *
         * **Correctness:** `total += abs(target - curFloor)` is exactly "one floor per second,
         * straight line, no overshoot," matching the problem's movement rule. The "if already on the
         * requested floor, no movement occurs" rule falls out for free: `abs(target - curFloor)` is 0
         * when `target == curFloor`, so no special-casing is needed (verified by the added
         * `[3,3,3]` and `[0]`-at-start cases above). The starting position `curFloor = 0` matches the
         * problem's fixed start floor. No overflow risk: with `n, requests.length <= 100`, the maximum
         * possible total is on the order of 100 * 99, far below `Int` range.
         *
         * **Alternatives:** none asymptotically better — every request must be read at least once
         * (Ω(requests.size) lower bound), so this is already optimal. A `fold`/`sumOf` version would be
         * more idiomatic Kotlin but identical complexity; e.g.
         * `requests.fold(0 to 0) { (total, cur), t -> total + abs(t - cur) to t }.first`. Not worth the
         * readability trade here since the imperative loop is already clear.
         *
         * **Parallelism:** not applicable. Each step's cost depends on `curFloor`, which is the *target*
         * of the previous step — a genuine sequential data dependency (a running "scan", not a
         * "reduce" over independent chunks), so there's no way to split the array and compute partial
         * sums independently without first knowing each chunk's entry floor. Even ignoring that, n and
         * requests.length are capped at 100, far below where thread/SIMD overhead would ever pay off.
         *
         * **Real-world angle:** this is a toy version of elevator dispatch/scheduling — real systems
         * (e.g. destination dispatch in modern elevator banks) additionally batch multiple riders,
         * reorder requests to minimize total wait/travel time (not just serve them in arrival order),
         * and account for capacity and up/down direction changes, turning it into a scheduling/
         * optimization problem (closer to TSP-with-constraints) rather than a simple linear replay.
         * The "sum of consecutive absolute differences" primitive itself shows up broadly, e.g.
         * total distance from a sequence of GPS waypoints, or total seek time of a disk head servicing
         * a sequence of track requests.
         */
        fun elevatorRequests(n: Int, requests: IntArray): Int {
            // iteration = abs(target - current)
            //10 | 1 2 3 4 -> 0->1->2->3->4 = 4
            //11 | 10 0 10 0  -> 0-10-0-10-0 = 10 + 10 + 10 + 10 = 40
            //11 | 10 0 0 0  -> 0-10-0-0-0 = 20
            //3 | 10 -> not possible

            var total = 0
            var curFloor = 0
            requests.forEach { target ->
                val path = abs(target - curFloor)
                total += path
                curFloor = target
            }
            return total
        }

    }
}
