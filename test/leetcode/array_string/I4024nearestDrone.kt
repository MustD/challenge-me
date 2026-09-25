package leetcode.array_string

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.math.abs
import kotlin.test.Test

/**
 * 4024. Nearest Available Drone  (https://leetcode.com/problems/nearest-available-drone/)
 *
 * You are given a 2D integer array drones, where drones[i] = [xi, yi, rangei] represents the x-coordinate,
 * y-coordinate, and travel range of the ith drone.
 *
 * You are also given an integer array target = [tx, ty], representing the coordinates of the target.
 *
 * A drone drones[i] can reach the target if the Manhattan distance between its coordinates and the target
 * coordinates is less than or equal to its rangei.
 *
 * Return the index of the reachable drone with the minimum Manhattan distance to the target. If there is a tie,
 * return the smallest index. If no drone can reach the target, return -1.
 *
 * Constraints:
 * - 1 <= drones.length <= 100
 * - drones[i] = [xi, yi, rangei]
 * - target = [tx, ty]
 * - -25 <= xi, yi, tx, ty <= 25
 * - 1 <= rangei <= 100
 */
typealias I4024 = (Array<IntArray>, IntArray) -> Int

class I4024nearestDrone {

    @Nested
    inner class Solution : leetcode.ProblemTest<I4024> {

        override val cases = leetcode.testCases<I4024>(
            leetcode.args("[[0,0,8],[2,2,9]]", "[3,4]") expects 1,
            leetcode.args("[[2,1,5],[4,4,5],[6,6,8]]", "[5,5]") expects 1,
            leetcode.args("[[4,4,5]]", "[8,6]") expects -1,
            leetcode.args("[[0,0,5]]", "[3,2]") expects 0,          // distance == range is reachable
            leetcode.args("[[1,0,5],[0,1,5]]", "[0,0]") expects 0,  // tie -> smallest index
            leetcode.args("[[5,5,1],[1,0,5],[0,1,5]]", "[0,0]") expects 1,// tie after an unreachable drone
            leetcode.args("[[3,0,2],[5,0,10]]", "[0,0]") expects 1,       // nearest drone is out of range
            leetcode.args("[[3,3,1],[0,0,1]]", "[0,0]") expects 1,        // drone sits on the target (distance 0)
            leetcode.args("[[10,10,1],[-10,-10,1]]", "[0,0]") expects -1, // several drones, none reachable
            leetcode.args("[[-25,-25,100]]", "[25,25]") expects 0,        // extreme coords, max distance == max range
            leetcode.args("[[-25,-25,100],[25,25,100]]", "[25,-25]") expects 0,   // tie at opposite corners
        )

        @Test
        fun test() = check(::nearestDrone)

        /**
         * ## Analysis (verified: all 11 cases pass)
         *
         * **Pattern:** linear scan with a filtered running minimum (argmin under a predicate), which is
         * the same template as "find the index of the smallest element that satisfies X".
         *
         * ### Time: O(n)
         * - `forEachIndexed` visits each of the n drones once. The body does a constant amount of work:
         *   two `abs`, one add, two comparisons. n ≤ 100, so this is trivial.
         * - Best case = worst case = Θ(n). You cannot stop early, because a later drone may be closer.
         *   The one exception would be `distance == 0`: nothing can beat it, and a later drone at 0
         *   would lose the tie anyway. You could `return idx` there. That is a micro-optimisation and
         *   not worth the extra branch.
         *
         * ### Space: O(1) auxiliary
         * - Only `tX, tY, result, min` plus the destructured `(dX, dY, range)` for each element.
         *   Destructuring `IntArray` compiles to `component1..3()` = `get(0..2)`, so no allocation.
         * - The `forEachIndexed` lambda is `inline`, so there is no closure object either.
         *
         * ### Correctness notes
         * - **Tie → smallest index** is handled by the *strict* `min > distance`. The first drone at a
         *   given distance wins, and later equal ones do not replace it. Writing `>=` would silently
         *   return the *largest* tied index. This is the key detail in this problem (probed by
         *   the `[[1,0,5],[0,1,5]]` and opposite-corners cases).
         * - **Inclusive range**: `range >= distance` matches "less than or equal to its range"
         *   (probed by `[[0,0,5]] / [3,2]`, where distance == range == 5).
         * - **Filter before compare**: an unreachable drone never touches `min`, so a close but
         *   out-of-range drone cannot hide a farther reachable one (`[[3,0,2],[5,0,10]]`).
         * - **Sentinel**: `result = -1` covers "no drone reachable" without a special case. Because
         *   `min = Int.MAX_VALUE` and every distance is ≤ 100, the first reachable drone always
         *   replaces it.
         * - **Overflow**: impossible here. Max distance is |25-(-25)| * 2 = 100. The general version
         *   has a subtle trap: `abs(tX - dX)` can overflow *before* `abs` when coords span the
         *   full Int range. Widen to `Long` if the constraints ever allow that.
         * - `val (tX, tY) = target` destructures an `IntArray`, which is correct and reads well.
         *
         * ### Alternatives
         * - **Functional one-liner**: `drones.indices.filter { reachable(it) }.minByOrNull { dist(it) } ?: -1`.
         *   `minByOrNull` returns the *first* minimum, so ties still resolve to the smallest index. It is the
         *   same O(n) time but allocates an O(n) list from `filter`. Using `asSequence()` avoids that.
         * - **Sort by (distance, index)** then take the first reachable: O(n log n). This is strictly worse
         *   for a single query. It only makes sense if you answer many queries against the *same* target.
         * - **Many targets / many queries**: precompute a spatial index. Manhattan distance becomes
         *   Chebyshev after rotating 45° (u = x+y, v = x−y), which makes a k-d tree or grid bucketing
         *   workable. On this tiny grid (51×51) you could even precompute the answer for every cell.
         *   None of that helps a single query: reading the input is already Ω(n), so O(n) is optimal.
         *
         * ### Parallelism
         * - In principle this is an embarrassingly parallel reduction: map each drone to
         *   `(distance, idx)` or ⊥, then reduce with `min` over the lexicographic pair. That reduction is
         *   associative, so chunks can be combined in any order and ties stay deterministic.
         * - In practice, with n ≤ 100, thread start-up costs far more than the whole scan (~100 ns of
         *   work). The inner loop is also SIMD-friendly: branchless abs/add/compare over structure-of-
         *   arrays `x[], y[], range[]`. The JIT may auto-vectorise it, but `Array<IntArray>`
         *   (array of pointers) blocks that because of the pointer chasing.
         *
         * ### Real world
         * - Dispatch / "nearest available unit": ride-hailing, delivery drones, warehouse robots,
         *   emergency services. In practice it is almost never a linear scan over all units. Units live
         *   in a geo index (geohash, H3 hexes, R-tree, PostGIS `<->` KNN) and you search nearby cells
         *   first, expanding outward.
         * - "Reachable" is rarely a static radius. It depends on battery, payload, wind, and road
         *   network distance (not Manhattan), so the filter is expensive and often evaluated lazily
         *   on the k nearest candidates only.
         * - Deterministic tie-breaking (smallest index here, often smallest ID or longest idle time in
         *   production) matters for fairness and reproducible tests. A distributed min-reduce needs
         *   the tie-breaker built into the comparison key, exactly as the `(distance, idx)` pair does.
         */
        fun nearestDrone(drones: Array<IntArray>, target: IntArray): Int {
            // val mDist = |Ax - Bx| + |Ay - By|
            // (X, Y, Fuel) = D[1]
            // (Tx Ty) = target


            val (tX, tY) = target
            var result = -1
            var min = Int.MAX_VALUE //conditions met for <Int

            drones.forEachIndexed { idx, (dX, dY, range) ->
                val distance = abs(tX - dX) + abs(tY - dY)
                if (range >= distance && min > distance) {
                    min = distance
                    result = idx
                }
            }

            return result
        }

    }
}
