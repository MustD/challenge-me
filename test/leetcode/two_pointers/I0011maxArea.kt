package leetcode.two_pointers

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 11. Container With Most Water  (https://leetcode.com/problems/container-with-most-water/)
 *
 * You are given an integer array `height` of length `n`. There are `n` vertical lines drawn such that the two
 * endpoints of the `i`th line are `(i, 0)` and `(i, height[i])`.
 *
 * Find two lines that together with the x-axis form a container, such that the container contains the most water.
 *
 * Return the maximum amount of water a container can store.
 *
 * Notice that you may not slant the container.
 *
 * Constraints:
 * - n == height.length
 * - 2 <= n <= 10^5
 * - 0 <= height[i] <= 10^4
 */
typealias I0011 = (IntArray) -> Int

class I0011maxArea {

    @Nested
    inner class Solution : ProblemTest<I0011> {

        override val cases = testCases<I0011>(
            "[1,8,6,2,5,4,8,3,7]" expects 49,
            "[1,1]" expects 1,
            "[8,7,2,1]" expects 7,
            "[0,0]" expects 0,
            "[1,2,4,3]" expects 4,
            "[4,4,4,4]" expects 12, // all-equal plateau: widest pair wins, no height trade-off to make
            "[1,5,4,5,1]" expects 10, // best pair (idx1,idx3) is interior; pointers must pass over a taller outer wall
            "[10000,10000]" expects 10000, // max height bound (10^4) at the minimum size (n=2)
            "[9,1,1,1,1,1,1,1,1,9]" expects 81, // best answer is the two end walls despite a long flat interior
        )

        @Test
        fun test() = check(::maxArea, ::referenceSolution)

        /**
         * ## Analysis — opposite-ends two pointers
         *
         * **Correctness.** `lo`/`hi` start at the two ends (widest possible container) and close inward one step
         * per iteration; `max` is updated with `(hi - lo) * minOf(leftH, rightH)` before either pointer moves, so
         * every candidate width is measured at least once before being discarded. The move rule — advance the
         * pointer at the **shorter** wall — is the elimination argument that makes this greedy and not just a
         * heuristic: fixing `lo` (the shorter wall) and shrinking `hi` can only lose width without ever gaining
         * height above `height[lo]`, so every pair `(lo, hi')` with `hi' < hi` is provably no better than the pair
         * just measured. That pair is safe to skip, so `lo++`/`hi--` never steps over the true optimum. The
         * `[1,5,4,5,1]` case above stresses exactly this: the best pair (indices 1 and 3) lies strictly inside the
         * array, so the pointers must correctly walk *past* the taller outer bars without ever measuring the
         * interior pair, and still land on the right answer via elimination rather than by direct comparison.
         *
         * **Time complexity: O(n).** `lo` and `hi` each move strictly toward each other every iteration (exactly one
         * of them advances per loop), and the loop stops once they meet — at most `n - 1` iterations total, each
         * O(1) work.
         *
         * **Space complexity: O(1).** Only `lo`, `hi`, `max`, and the two locals `leftH`/`rightH` — no recursion,
         * no auxiliary structures.
         *
         * **Edge cases handled correctly by this implementation:**
         * - `n == 2` (`lo = 0`, `hi = 1`): loop body runs exactly once, no special-casing needed.
         * - All-equal heights (`[4,4,4,4]`): every comparison is a tie, so `leftH < rightH` is always false and
         *   `hi` is the one that always moves — still correct because on a tie either wall may retreat.
         * - All-zero heights (`[0,0]`): `max` starts at 0 and no positive area is ever found; returns 0 without a
         *   guard clause.
         * - Overflow: with `height[i] <= 10^4` and `n <= 10^5`, the largest possible product is
         *   `10^4 * (10^5 - 1) ≈ 10^9`, which fits comfortably in `Int` (max ≈ 2.147 × 10^9) — no `Long` needed here,
         *   but worth flagging since it would matter with larger bounds.
         *
         * **Pattern:** opposite-ends two pointers driven by an exchange/elimination argument on a bottleneck
         * (`min`) objective — the same shape as *Trapping Rain Water* (LC 42) and *Boats to Save People* (LC 881),
         * though each closes the gap for a different reason.
         *
         * **Alternatives considered:**
         * - **Brute force, O(n²)/O(1):** check every pair directly. Simpler to prove correct (no elimination
         *   argument needed) but far too slow at `n = 10^5` (~5 × 10^9 pair checks).
         * - No known approach beats O(n) here: every pair must at least be considered or provably eliminated, and
         *   this solution already achieves that in a single linear pass, so it is asymptotically optimal.
         *
         * **Parallelism:** not worth it. The pointers form a strict sequential dependency chain — each step's
         * move decision depends on the previous positions, so there's no independent work to fork. A
         * divide-and-conquer split (solve left half / right half / pairs crossing the midpoint) is possible in
         * principle but the crossing-pairs step still needs an O(n) two-pointer-style scan, so it wouldn't beat
         * the sequential O(n) algorithm — the coordination overhead would dominate at any realistic `n`.
         *
         * **Real-world angle:** this exact shape — two pointers closing inward on a bottleneck/min objective with
         * a provable elimination rule — shows up in interval-merging, scheduling problems bounded by the tighter of
         * two resources, and streaming max-window computations where you can prove a candidate is dominated without
         * fully re-examining it. In production, the "optimal" O(n) scan is also usually the practical choice: it's
         * branch-predictable, cache-friendly (single forward+backward sweep over one array), and needs no extra
         * memory — brute force would only be reached for if `n` were tiny and clarity mattered more than speed.
         */
        fun maxArea(height: IntArray): Int {
            var lo = 0
            var hi = height.lastIndex
            var max = 0
            while (lo < hi) {
                val leftH = height[lo]
                val rightH = height[hi]
                max = maxOf(max, (hi - lo) * minOf(leftH, rightH))
                if (leftH < rightH) lo++ else hi--
            }
            return max
        }

        /**
         * ## Reference solution — two pointers closing in from both ends (greedy elimination)
         *
         * **Restatement.** Pick two indices `i < j`. The water they hold is a rectangle of width `j - i` and height
         * `min(height[i], height[j])` (the shorter wall caps the water level; walls in between don't matter).
         * Return the largest such rectangle area.
         *
         * **Pattern.** *Opposite-ends two pointers.* Brute force checks all `n(n-1)/2` pairs (O(n²), too slow at
         * n = 10^5). Two pointers work here because one comparison lets you **prove** that a whole group of pairs
         * can't beat the best seen so far, so you can skip them without checking.
         *
         * ### Intuition
         * Start with the widest container, `l = 0` and `r = n - 1`. Say `height[l] <= height[r]`. Now look at every
         * other container that uses the left wall `l`, meaning `(l, r')` with `r' < r`:
         * - it is **narrower** (`r' - l < r - l`), and
         * - its height is `min(height[l], height[r']) <= height[l]`, so it is **no taller**.
         *
         * So none of them can beat `(l, r)`, which you just measured. Wall `l` has nothing left to offer, and you can
         * drop it with `l++`. Each step rules out one wall for good, so after `n - 1` steps every pair has been
         * either measured or proven no better.
         *
         * Moving the **taller** wall instead would be wrong: the shorter wall still caps the height, the width
         * shrinks, and you might skip past the real answer.
         *
         * ### Core concepts
         * - **Opposite-ends two pointers.** Start at both ends of the array and move them toward each other. It fits
         *   when each move is justified by a local comparison, so the O(n²) pair space shrinks by one row or column
         *   per step.
         * - **Exchange / elimination argument.** A proof that every candidate you skip is no better than one you
         *   already measured. It is what makes a greedy move *correct* and not just a heuristic. Here, "every pair
         *   using the shorter wall is dominated by the current pair".
         * - **Bottleneck (min) objective.** When a value is limited by its weakest part (`min` of two heights),
         *   improving the stronger part is pointless. Always work on the bottleneck.
         * - **Width vs. height trade-off.** Moving inward always costs width. The only way to come out ahead is a
         *   taller limiting wall, and only moving the shorter pointer can give you one.
         *
         * ### Approach
         * 1. `l = 0`, `r = lastIndex`, `best = 0`.
         * 2. While `l < r`: compute `(r - l) * min(height[l], height[r])` and update `best`.
         * 3. Move the pointer at the **shorter** wall inward. On a tie, move either one: every other pair using
         *    either tied wall is dominated too.
         * 4. Return `best`.
         *
         * ### Complexity
         * - **Time O(n):** each iteration moves one pointer one step, and they meet after `n - 1` steps.
         * - **Space O(1):** three integers.
         *
         * ### Common pitfalls
         * - **Moving the taller pointer**, or moving by "which side looks more promising". That breaks the
         *   elimination proof and gives wrong answers (e.g. `[1,2,4,3]` → 4).
         * - **Using the taller wall as the height.** Water spills over the shorter one, so it must be `min`.
         * - **Thinking the walls in between block the water.** They don't. This is not *Trapping Rain Water*
         *   (LC 42), which sums water above each bar.
         * - **Overflow worry:** the maximum is `10^4 * (10^5 - 1) ≈ 10^9`, which just fits in `Int`
         *   (`Int.MAX_VALUE ≈ 2.147 * 10^9`). With larger bounds, use `Long`.
         * - **Zero heights:** `[0,0]` → 0. The loop handles it without a special case.
         */
        fun referenceSolution(height: IntArray): Int {
            var l = 0
            var r = height.lastIndex
            var best = 0
            while (l < r) {
                val area = (r - l) * minOf(height[l], height[r])
                if (area > best) best = area
                if (height[l] <= height[r]) l++ else r--
            }
            return best
        }

    }
}
