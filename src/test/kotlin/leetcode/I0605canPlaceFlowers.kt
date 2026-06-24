package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 605. Can Place Flowers  (https://leetcode.com/problems/can-place-flowers/)
 *
 * You have a long flowerbed where some plots are planted and some are not, but flowers cannot be
 * planted in adjacent plots. Given an integer array `flowerbed` of 0's (empty) and 1's (planted)
 * and an integer `n`, return `true` if `n` new flowers can be planted without violating the
 * no-adjacent-flowers rule, and `false` otherwise.
 *
 * Constraints:
 * - 1 <= flowerbed.length <= 2 * 10^4
 * - flowerbed[i] is 0 or 1
 * - There are no two adjacent flowers in flowerbed (input is already valid).
 * - 0 <= n <= flowerbed.length
 */
typealias I0605 = (IntArray, Int) -> Boolean

class I0605canPlaceFlowers {

    @Nested
    inner class Solution : ProblemTest<I0605> {

        override val cases = testCases<I0605>(
            args("[1,0,0,0,1]", 1) expects true,
            args("[1,0,0,0,1]", 2) expects false,
            args("[0]", 1) expects true,
            args("[0,0,0]", 2) expects true,
            args("[1,0,0,0,0,1]", 1) expects true,
            args("[1,0,0,0,0,1]", 2) expects false,
        )

        @Test
        fun test() = check(::canPlaceFlowers)

        /**
         * Analysis of `canPlaceFlowers` (the solution wired into `check`).
         *
         * Pattern: greedy single-pass scan. Walk left-to-right; whenever a plot and both
         * neighbors are empty, plant immediately. Greedy is provably optimal here because
         * planting at the earliest legal spot never blocks a placement you couldn't make
         * otherwise — leaving an empty plot empty can only ever cost you, never help. The
         * `getOrNull(i - 1) ?: 0` / `getOrNull(i + 1) ?: 0` trick treats the off-array borders
         * as empty, which neatly handles the first/last plots without special-casing them.
         * After planting, the loop advances by 2 (`i++` inside the `if`, plus the trailing
         * `i++`) to skip the now-forbidden adjacent plot; on a non-plant it advances by 1.
         *
         * Time: O(L) where L = flowerbed.length. Single linear scan; each iteration does O(1)
         * work (two bounds-checked reads + a comparison). The early `return true` only makes it
         * finish sooner.
         *
         * Space: O(1) auxiliary. Just `count` and `i`; no recursion, no allocation. Note this
         * mutates nothing — many textbook versions write back into `flowerbed[i] = 1`; here the
         * skip-by-2 makes that unnecessary, which is cleaner and avoids touching the input.
         *
         * Correctness notes:
         * - Borders handled correctly via the null-coalescing reads.
         * - The "input is already valid (no two adjacent 1s)" precondition is what lets the
         *   simple `left/current/right` check suffice.
         * - The `left` read uses the live array, never a "I just planted here" flag, yet stays
         *   correct: after planting at `i` the loop jumps to `i + 2`, so the plot it just claimed
         *   (`i + 1`, left untouched in the array) is permanently skipped and never re-read as a
         *   neighbor. The skip-by-2 *is* the bookkeeping that a write-back would otherwise do.
         * - EDGE CASE — `n == 0`: handled correctly by the guard on the first line
         *   (`if (n == 0) return true`). Without it, a fully-planted bed like `[1]` with `n = 0`
         *   would find no plantable spot and fall through to `return false`, which would be wrong;
         *   the guard short-circuits that. (Worth a regression test: `args("[1]", 0) expects true`,
         *   which the current cases don't cover.)
         * - The `if (count >= n) return true` early-exit inside the planting branch is the only
         *   success path once `n > 0`; reaching the end of the loop means fewer than `n` legal
         *   spots existed, so `return false` is correct.
         *
         * Alternatives:
         * - Asymptotically this is already optimal: every plot must be inspected at least once
         *   to know if it's plantable, so O(L) time / O(1) space is the floor. No O(log L) or
         *   better exists for an unstructured array.
         * - A common variant counts the length of each run of consecutive 0s and adds
         *   `(gap - 1) / 2` (with virtual +1 padding on both ends) instead of stepping plot by
         *   plot. Same O(L)/O(1), arguably tidier math, but no asymptotic difference.
         *
         * Parallelism: not worth it. L tops out at 2*10^4 and the work per element is a couple
         * of comparisons — far below the threshold where thread/SIMD overhead pays off. The
         * greedy carries a left-to-right data dependency (whether plot i was planted affects
         * i+1), so a naive parallel split could double-count or miss placements at chunk
         * boundaries. It *is* parallelizable with care — split into chunks, plant within each
         * conservatively assuming the boundary plots are occupied, then do a reconciliation pass
         * on the seams — but that's a map-reduce over runs-of-zeros, and Amdahl's law plus the
         * tiny input make it strictly slower in practice.
         *
         * Real-world: this is the "place items respecting a minimum-gap constraint" pattern —
         * non-overlapping ad slots / scheduling jobs with cooldown / channel allocation in radio
         * spectrum (adjacent channels interfere) / seating with social-distancing gaps. At real
         * scale the input is usually a stream of intervals rather than a dense 0/1 array, so
         * you'd carry a "last placed position" cursor over an event stream rather than scan a
         * materialized array — but the greedy "place at the earliest legal slot" insight is
         * exactly the same, and it's still optimal.
         */
        fun canPlaceFlowers(flowerbed: IntArray, n: Int): Boolean {
            if (n == 0) return true
            var count = 0
            var i = 0
            while (i <= flowerbed.lastIndex) {
                val left = flowerbed.getOrNull(i - 1) ?: 0
                val current = flowerbed[i]
                val right = flowerbed.getOrNull(i + 1) ?: 0
                if (left == 0 && current == 0 && right == 0) {
                    count++
                    if (count >= n) return true
                    i++
                }
                i++
            }
            return false
        }

    }
}
