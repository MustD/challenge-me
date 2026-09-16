package leetcode.array_string

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4045. Count Robot Groups  (https://leetcode.com/problems/count-robot-groups/)
 *
 * You are given a strictly increasing integer array position, where position[i] is the initial
 * position of the ith robot at time t = 0.
 *
 * You are also given an integer array speed, where speed[i] is the constant speed of the ith
 * robot in units per second, and an integer distance.
 *
 * Time is continuous and measured in seconds. A robot or group with speed v moves v * t units to
 * the right over any interval of t seconds.
 *
 * Whenever the distance between two robots or groups becomes at most distance, they merge into a
 * single group.
 *
 * If multiple robots or groups satisfy the merging condition at the same time, all merges happen
 * simultaneously. Specifically, every connected collection of robots or groups whose consecutive
 * positions differ by at most distance merges into one group.
 *
 * After a merge, the resulting group takes the current position and speed of the rightmost robot
 * in that group. Once merged, robots never separate.
 *
 * Return the number of groups remaining after all possible merges have occurred.
 *
 * Constraints:
 * - 1 <= position.length == speed.length <= 10^5
 * - 1 <= position[i], speed[i], distance <= 10^9
 * - position is strictly increasing.
 */
typealias I4045 = (IntArray, IntArray, Int) -> Int

class I4045countGroups {

    @Nested
    inner class Solution : leetcode.ProblemTest<I4045> {

        override val cases = leetcode.testCases<I4045>(
            leetcode.args("[1,5,6,20]", "[4,3,2,3]", 1) expects 2,
            leetcode.args("[1,5,9]", "[3,2,2]", 2) expects 2,
            leetcode.args("[9]", "[8]", 5) expects 1,
            leetcode.args("[0, 9, 20]", "[3, 10, 1]", 1) expects 1,
            // boundary: gap == distance still merges immediately
            leetcode.args("[0, 5]", "[1, 1]", 5) expects 1,
            // equal speeds never close the gap, no merge ever
            leetcode.args("[0, 100]", "[5, 5]", 1) expects 2,
            // chained immediate merge keeps the rightmost's (fast) speed, blocking a slower leader from ever catching up
            leetcode.args("[0, 50, 51, 52]", "[5, 1, 1, 100]", 1) expects 2,
            // huge speed is destroyed on first merge (adopts slow neighbor's speed), so it never reaches the far terminal robot
            leetcode.args("[10, 20, 30, 40]", "[1000000000, 1, 1, 100]", 1) expects 3,
        )

        @Test
        fun test() = check(::countGroups)

        /**
         * ## Pattern
         * Right-to-left monotonic sweep — the same family as *Car Fleet* (LC 853), extended with an
         * "instant merge" rule. `prevPos`/`prevSpd` track the (position, speed) of the rightmost
         * robot in the group that currently "owns" everything merged so far — per the problem statement,
         * a merged group always inherits the position/speed of its rightmost member, so that member's
         * data is the only thing future comparisons ever need.
         *
         * Two independent merge triggers are checked per step, in priority order:
         * 1. **Immediate merge** (`prevPos - pos <= distance`): the ORIGINAL t=0 gap between robot `i`
         *    and robot `i+1` is already within range, so they merge instantly regardless of speed. Note
         *    `prevPos` is reset to `pos` (robot i's own original position) on *every* branch, so this
         *    check always compares true original array-adjacent positions — never an accumulated/shifted
         *    position — which matches the spec's "connected collection of CONSECUTIVE robots" rule.
         * 2. **Catch-up merge** (`spd > prevSpd`): not close enough yet, but robot `i` is strictly faster
         *    than the group ahead of it, so the gap shrinks to 0 eventually. Equal speed is deliberately
         *    excluded — a constant gap never closes.
         * If neither fires, robot `i` starts a brand-new group and becomes the new rightmost reference
         * (`prevPos`/`prevSpd` both updated to its own values).
         *
         * Crucially, `prevSpd` is left untouched by *both* merge branches — a merge never changes the
         * group's effective speed, because the newly-absorbed robot is to the left of the existing
         * rightmost member, so the rightmost (and thus the group's speed) doesn't change. This is what
         * correctly handles the "speed destruction" trap: a very fast robot that merges into a much
         * slower group immediately inherits that slow speed and may never reach a fast robot further
         * ahead — see the `1000000000` test case above, adapted from a reported missing LeetCode test
         * case for exactly this scenario (a naive "biggest speed wins" shortcut gives the wrong answer;
         * this implementation never takes that shortcut, it always compares against the true rightmost's
         * current effective speed).
         *
         * ## Complexity
         * - Time: **O(n)** — single reverse pass, O(1) work per index (no popping/backtracking needed,
         *   because unlike Car Fleet, a merge here never needs to revisit or undo earlier decisions —
         *   `prevPos`/`prevSpd` are all the accumulated state a later index can ever need).
         * - Space: **O(1)** auxiliary (two scalars), ignoring the input arrays.
         *
         * ## Correctness notes
         * - Relies on `position` being strictly increasing (given by constraints) so `prevPos - pos` is
         *   always positive and gaps are well-defined without `abs`.
         * - `Int` arithmetic is safe here: positions are bounded by 1e9, so the difference fits well
         *   within `Int` range; there's no summation that could overflow (contrast with problems that
         *   accumulate distances/speeds).
         * - Single-robot input short-circuits correctly: the loop range `lastIndex - 1 downTo 0` is empty,
         *   returning the initial `groups = 1`.
         *
         * ## Alternative approaches
         * A stack-based formulation (push/pop explicit `(pos, speed)` pairs) is the more common way to
         * present this pattern and generalizes better if the merge rule ever needed to look more than one
         * level back, but it's asymptotically identical (O(n) time, O(n) worst-case space for the stack
         * vs. this solution's O(1) space) — here two scalars suffice because each index only ever needs
         * the single current rightmost reference, never a deeper history.
         *
         * ## Parallelism
         * Not applicable: this is an inherently sequential right-to-left scan where each step's decision
         * depends on the previous step's (possibly merge-updated) state — a genuine data dependency chain,
         * not just an implementation choice. A parallel prefix/scan variant is theoretically possible if
         * the merge relation could be expressed as an associative combine, but the "instant merge" rule
         * (rightmost's *original* position, independent of accumulated history) breaks simple
         * associativity, and at n <= 1e5 with O(1) work per step this single-threaded pass is already
         * far cheaper than any thread/task-spawning overhead would be.
         *
         * ## Real-world experience
         * This "fleet convergence" pattern shows up in traffic/platoon simulation (vehicles or convoy
         * segments merging when they catch up to a slower one ahead) and in job-scheduling contexts where
         * tasks "merge" into a shared resource once they catch up to it. The production twist is usually
         * that speed/position aren't fixed constants — they change over time (acceleration, throttling),
         * which breaks this closed-form "will it ever catch up" comparison and forces an actual
         * event-driven or discrete-time simulation instead of a single O(n) sweep.
         */
        fun countGroups(position: IntArray, speed: IntArray, distance: Int): Int {
            // 1|10, 2|5 -> 1gr|5
            // 1|5, 2|10 -> 2gr ->
            // 1|4, 2|3, 3|5, 4|4, 5|6, 6|5 ->  3
            // 1|2, 11|3, 21|2
            // 0|2, 10|1
            // 60/90, 70/90, 80/110, 95/100, 100/100 (9) ->  1

            var groups = 1
            var prevSpd = speed.last()
            var prevPos = position.last()
            for (i in position.lastIndex - 1 downTo 0) {
                val spd = speed[i]
                val pos = position[i]

                if ((prevPos - pos) <= distance) {
                    prevPos = pos
                    continue
                }
                // far enough to be not merged immediately

                if (spd > prevSpd) {
                    prevPos = pos
                    continue
                }
                // slow enough to be not merged

                groups++
                prevPos = pos
                prevSpd = spd
            }

            return groups
        }

    }
}
