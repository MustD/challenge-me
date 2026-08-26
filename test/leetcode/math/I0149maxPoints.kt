package leetcode.math

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.math.abs
import kotlin.test.Test

/**
 * 149. Max Points on a Line  (https://leetcode.com/problems/max-points-on-a-line/)
 *
 * Given an array of points where `points[i] = [xi, yi]` represents a point on the X-Y plane,
 * return the maximum number of points that lie on the same straight line.
 *
 * Constraints:
 * - 1 <= points.length <= 300
 * - points[i].length == 2
 * - -10^4 <= xi, yi <= 10^4
 * - All the points are unique (no duplicate coordinates to worry about).
 */
typealias I0149 = (Array<IntArray>) -> Int

class I0149maxPoints {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0149> {

        override val cases = leetcode.testCases<I0149>(
            "[[1,1],[2,2],[3,3]]" expects 3,
            "[[1,1],[3,2],[5,3],[4,1],[2,3],[1,4]]" expects 4,
            "[[0,0]]" expects 1,
            "[[0,0],[1,1]]" expects 2,
            // vertical line — watch out for division by zero when computing slope
            "[[1,0],[1,5],[1,-3],[2,7]]" expects 3,
            // slope equality via floating point can be lossy: prefer cross-product or a reduced fraction key
            "[[0,0],[94911151,94911150],[94911152,94911151]]" expects 2,
        )

        @Test
        // add `::maxPoints` back here once your own implementation is filled in
        fun test() = check(::referenceSolution)

        fun maxPoints(points: Array<IntArray>): Int {
            TODO("implement")
        }

        /**
         * ### Restatement
         * Given up to 300 distinct lattice points, find the largest subset that is collinear.
         *
         * ### Pattern: "anchor point + group by direction" (a hash-map counting pattern)
         * Any line that matters must pass through at least two of the given points. So instead of
         * inventing lines, *anchor* on each point `i` and ask: for every other point `j`, what is the
         * direction of the segment `i -> j`? All `j` sharing the same direction from `i` lie on one
         * line through `i`. The largest bucket + 1 (the anchor itself) is the best line through `i`;
         * the answer is the max over all anchors.
         *
         * This is the same shape as "group anagrams": the hard part is choosing a **canonical key**
         * so that equal things hash equal.
         *
         * ### Approach
         * 1. `n <= 2` -> answer is `n` (any 1 or 2 distinct points are trivially collinear).
         * 2. For each anchor `i`, build a fresh `HashMap<key, count>`.
         * 3. For each `j > i` compute `dx = xj - xi`, `dy = yj - yi` and canonicalize the direction:
         *    - divide both by `g = gcd(|dx|, |dy|)` (reduces the fraction `dy/dx` to lowest terms);
         *      `g > 0` always, because the points are distinct so `dx` and `dy` are never both 0.
         *    - fix the sign so opposite directions collapse into one: if `dx < 0`, or `dx == 0 && dy < 0`,
         *      negate both. Now `(0,5)` and `(0,-5)` both become `(0,1)`.
         *    - pack into a single primitive key: `dx * 40001L + dy` (after reduction each component is in
         *      `[-20000, 20000]`, so the encoding is collision-free).
         * 4. Track `max(count) + 1` over all buckets and all anchors.
         *
         * Only `j > i` is scanned: any line with `k` points is discovered from its *first* point, where
         * all `k - 1` others are still ahead. Halving the work costs nothing in correctness.
         *
         * ### Complexity
         * - Time `O(n^2 * log C)` — every ordered pair once, with a `gcd` (log of the coordinate range)
         *   per pair. With `n <= 300` that is ~45k pairs.
         * - Space `O(n)` — one map per anchor, cleared (re-created) each outer iteration.
         *
         * ### Pitfalls
         * - **Floating-point slopes.** `dy.toDouble() / dx` looks tempting but is lossy: the case
         *   `[[0,0],[94911151,94911150],[94911152,94911151]]` is the classic counterexample where two
         *   genuinely different slopes compare equal as `Double`. Use a reduced integer fraction (here)
         *   or a cross-product test `dx1 * dy2 == dy1 * dx2` (in `Long`).
         * - **Vertical lines** (`dx == 0`) divide by zero with the slope formula; the fraction key handles
         *   them naturally as `(0, 1)`.
         * - **Sign normalization.** Without it, `i -> j` and `j -> i` hash differently and a line gets split
         *   across two buckets. (Scanning only `j > i` does not by itself fix this: from one anchor, points
         *   can still lie on both sides.)
         * - **Off-by-one:** the bucket counts the *other* points, so remember the `+ 1` for the anchor.
         * - **Base case `n == 1`** must return 1, not 0 — the inner loop never runs, so `best` must start at 1.
         * - Duplicate points would break the "distinct" assumption (`g == 0`); this problem guarantees none,
         *   but a variant that allows them needs a separate duplicate counter.
         */
        fun referenceSolution(points: Array<IntArray>): Int {
            val n = points.size
            if (n <= 2) return n

            var best = 1
            for (i in 0 until n) {
                val directions = HashMap<Long, Int>()
                for (j in i + 1 until n) {
                    var dx = points[j][0] - points[i][0]
                    var dy = points[j][1] - points[i][1]

                    val g = gcd(abs(dx), abs(dy))
                    dx /= g
                    dy /= g
                    if (dx < 0 || (dx == 0 && dy < 0)) {
                        dx = -dx
                        dy = -dy
                    }

                    val key = dx * 40_001L + dy
                    val count = (directions[key] ?: 0) + 1
                    directions[key] = count
                    if (count + 1 > best) best = count + 1
                }
            }
            return best
        }

        private tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    }
}
