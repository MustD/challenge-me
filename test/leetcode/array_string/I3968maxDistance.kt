package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.math.abs
import kotlin.test.Test

/**
 * 3968. Maximum Manhattan Distance After All Moves  (https://leetcode.com/problems/maximum-manhattan-distance-after-all-moves/)
 *
 * You are given a string `moves` consisting of the characters 'U', 'D', 'L', 'R', and '_'.
 *
 * Starting from the origin (0, 0), each character represents one move on a 2D plane:
 * - 'U': Move up by 1 unit.
 * - 'D': Move down by 1 unit.
 * - 'L': Move left by 1 unit.
 * - 'R': Move right by 1 unit.
 * - '_': Can be independently replaced with any one of 'U', 'D', 'L', or 'R'.
 *
 * Return the maximum Manhattan distance from the origin that can be achieved after all moves have been performed.
 *
 * Constraints:
 * - 1 <= moves.length <= 10^5
 * - moves consists of only 'U', 'D', 'L', 'R', and '_'.
 */
typealias I3968 = (String) -> Int

class I3968maxDistance {

    @Nested
    inner class Solution : ProblemTest<I3968> {

        override val cases = testCases<I3968>(
            "L_D_" expects 4,
            "U_R" expects 3,
            "_" expects 1,              // single wildcard, n = 1
            "R" expects 1,              // single fixed move
            "____" expects 4,           // all wildcards: every one goes the same way
            "UDLR" expects 0,           // fixed moves cancel out, no wildcards
            "UD__" expects 2,           // net zero position, wildcards alone decide the answer
            "DDD_" expects 4,           // negative vertical: wildcard must push further down
            "LLL_" expects 4,           // negative horizontal: wildcard must push further left
            "RRDD__L" expects 5,        // mixed signs (x = +1, y = -2) plus two wildcards
        )

        @Test
        fun test() = check(::maxDistance)

        /**
         * ## Analysis (leetcode-validate)
         *
         * **Verdict:** correct — passes the 2 official + 8 added cases.
         *
         * ### Key insight
         * Manhattan distance is separable: `|x| + |y|`. A wildcard adds +1 to the answer
         * whenever it moves *away* from the origin along any axis, and moving further along the
         * axis you are already heading in always does that. Every wildcard can be spent the same
         * way, so the closed form is `answer = |x| + |y| + wildcards`. Your code gets this by
         * pushing all `counter` moves onto one axis in the direction of its sign
         * (`if (p[0] >= 0) ... else ...`). That gives `|p[0]| + counter` there, and `|p[1]|` is unchanged.
         *
         * ### Time — O(n)
         * - `moves.forEach` with an O(1) `when`: n steps.
         * - `repeat(counter) { p[0]++ }`: another O(w) loop, where w = number of `'_'` ≤ n.
         *   Worst case (all wildcards) is 2n, which is still O(n). This loop only computes an
         *   addition, though: `p[0] += counter` (or `-=`) does the same work in O(1).
         *
         * ### Space — O(1)
         * The `IntArray(2)` plus a counter, with no recursion and no copy of the input.
         * Destructuring `val (x, y) = p` is two reads, not an allocation.
         *
         * ### Correctness notes
         * - **The sign branch matters.** If you always did `p[0]++` instead, it would undo distance when
         *   the axis is negative. `"DDD_"` (it would give 2, the right answer is 4) is the added case that
         *   would expose that bug.
         * - **`>= 0` at zero**: the axis is at the origin, so either direction works. `"UD__"` checks this.
         * - **Overflow**: |coord| ≤ n ≤ 10^5, so `Int` is fine.
         * - **Naming nit**: the comment says `//x,y`, but `p[0]` is changed by U/D, so it is really y.
         *   That does not affect the result because `|x| + |y|` is symmetric, but it would mislead a reader.
         * - `abs(0 - x)` is just `abs(x)`. The "distance from origin" form is clear, but the `0 -`
         *   adds nothing.
         * - `else -> throw` is good defensive style. The constraints rule it out, but it makes the
         *   `when` total and loud on bad input.
         *
         * ### Pattern
         * **Greedy / closed-form via separability of the metric.** If the objective splits into
         * independent per-axis terms (L1 norm), each free choice can be judged on its own, and
         * "always extend the dominant direction" is optimal. Related problems: LC 2833 *Furthest Point
         * From Origin* (the 1-D version of this one) and LC 3443 *Maximum Manhattan Distance After K
         * Changes*, where the max |x|+|y| is computed via the four sign combinations `±x ± y`.
         *
         * ### Alternatives
         * - **Pure arithmetic**: `abs(cnt('U') - cnt('D')) + abs(cnt('R') - cnt('L')) + cnt('_')`.
         *   It is the same O(n)/O(1) and needs no branch on the sign.
         * - **Four-direction max**: `max over (sx, sy) ∈ {±1}² of (sx·x + sy·y) + w`. This form
         *   generalizes when wildcards are restricted, or with a limit of k changes as in LC 3443.
         * - **Brute force / DP over choices**: 4^w assignments. It is only useful as a test oracle.
         * - O(n) is optimal: every character has to be read to know the counts.
         *
         * ### Parallelism
         * Technically it is embarrassingly parallel: the four counts are a map-reduce (split the string,
         * count per chunk, sum). With n ≤ 10^5 the whole scan takes microseconds, which is less than the
         * cost of starting a thread, so it is not worth it. At GB scale the realistic win comes from SIMD
         * byte counting (memchr-style), then multithreading, until memory bandwidth becomes the limit.
         *
         * ### Real world
         * Separable L1 metrics show up in grid/warehouse robot routing, taxicab distance in city
         * logistics, and PCB/VLSI wire-length estimates (half-perimeter wire length is a per-axis sum).
         * "Free moves go along the dominant axis" is the same reasoning as worst-case bounds in
         * uncertain positioning: with w unknown steps, the reachable region is the L1 ball of radius
         * w around the known position, so the worst-case distance is |p|₁ + w.
         */
        fun maxDistance(moves: String): Int {
            val p = IntArray(2) { 0 } //x,y
            var counter = 0
            moves.forEach { move ->
                when (move) {
                    'U' -> p[0]++
                    'D' -> p[0]--
                    'R' -> p[1]++
                    'L' -> p[1]--
                    '_' -> counter++
                    else -> throw IllegalStateException()
                }
            }

            if (p[0] >= 0) {
                repeat(counter) { p[0]++ }
            } else {
                repeat(counter) { p[0]-- }
            }

            val (x, y) = p
            return abs(0 - x) + abs(0 - y)
        }

    }
}
