package leetcode.intervals

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 3964. Minimum Lights to Illuminate a Road  (https://leetcode.com/problems/minimum-lights-to-illuminate-a-road/)
 *
 * You are given an integer array `lights` of length `n`, representing positions 0 through `n - 1` on a road.
 *
 * For each position `i`:
 * - If `lights[i] = v`, where `v > 0`, there is a working bulb at position `i` that illuminates every position from
 *   `max(0, i - v)` to `min(n - 1, i + v)`, inclusive.
 * - If `lights[i] = 0`, there is no working bulb at position `i`.
 *
 * A position is visible if it is illuminated by at least one working bulb.
 *
 * You may install additional bulbs at any positions. Each additional bulb installed at position `j` illuminates
 * positions from `max(0, j - 1)` to `min(n - 1, j + 1)`, inclusive.
 *
 * Return the minimum number of additional bulbs required to make every position on the road visible.
 *
 * Constraints:
 * - 1 <= n == lights.length <= 10^5
 * - 0 <= lights[i] <= n
 */
typealias I3964 = (IntArray) -> Int

class I3964minLights {

    @Nested
    inner class Solution : ProblemTest<I3964> {

        override val cases = testCases<I3964>(
            "[0,0,0,0]" expects 2,
            "[0,0,0,2,0]" expects 1,
            "[0]" expects 1,                                // n = 1, dark
            "[1]" expects 0,                                // n = 1, lit (range clamped to the road)
            "[3,3,3]" expects 0,                            // everything lit, ranges exceed the road
            "[0,1,0,0,1,0]" expects 0,                      // adjacent ranges 0..2 and 3..5 touch with no gap
            "[0,1,0,0,0,1,0]" expects 1,                    // two disjoint ranges, single dark cell 3 between them
            "[0,0,0,0,1,0,0,0,0]" expects 2,                // dark at both the start and the end
            "[0,0,0,3,1,0,0,0,0,0]" expects 1,              // range 3..5 nested inside 0..6 — merge must keep max end
            "[0,0,1,0,0,0,0,0,0,9]" expects 0,              // later bulb has an earlier start: ranges are not sorted by start
            "[0,0,0,0,0,0,0,0,0,0]" expects 4,              // no bulbs, length 10 -> ceil(10/3)
            "[0,0]" expects 1,                              // n = 2, one new bulb covers both
            "[1,0,0,0,0]" expects 1,                        // bulb at index 0, only a dark tail
            "[0,0,0,0,1]" expects 1,                        // bulb at last index, only a dark head
            "[0,0,0,0,0,0,0,1]" expects 2,                  // dark run of 6 -> exact multiple of 3
            "[0,0,0,0,0,0,0,0,1]" expects 3,                // dark run of 7 -> ceil rounds up by one
            "[0,1,2,0,0,0,0,0]" expects 1,                  // two ranges with the same start 0 (0..2 and 0..4)
            "[0,0,0,0,0,1,0,0,0,0,0]" expects 4,            // two runs of 4 each, flanking a centre bulb
        )

        @Test
        fun test() = check(
            ::minLights,
            ::referenceSolution,
        )

        /**
         * ## Analysis (lt-check) — verified: 2 official + 16 added cases
         *
         * **Approach as written.** Four stages: (1) turn every bulb into a clipped `IntRange`, (2) sort by start and
         * merge overlapping ranges, (3) take the complement of the merged ranges — the dark gaps, including the head
         * before the first range and the tail after the last — and (4) sum `ceil(len / 3)` over the gaps.
         *
         * **Time complexity.** `O(n log n)`, dominated by `sortedBy` over `k <= n` ranges. Everything else is linear:
         * building ranges `O(n)`, merging `O(k)`, building the complement `O(k)`, and summing `O(n)` — note that
         * `IntRange.count()` is `Iterable.count()` and *walks* the range rather than computing `last - first + 1`, but
         * the gaps are disjoint, so their total length is at most `n`. Best case (no bulbs) is `O(n)`.
         *
         * **Space complexity.** `O(k)` = `O(n)` auxiliary: `ranges`, the `sorted` copy, `merged` and `reversed` are each
         * up to `k` boxed `IntRange` objects (roughly 4 lists alive at once). No recursion. Output is a single `Int`.
         *
         * Optimal: O(n) time / O(n) space — this is O(n log n) time, so one log factor above it (space matches).
         *
         * **Correctness notes.**
         * - Merge keeps `maxOf(end, prev.last)`, so a range nested inside the previous one cannot shrink it.
         * - Ranges that merely touch (`prev.last + 1 == start`) are *not* merged; that is fine — the complement step
         *   then produces an empty gap `start..start-1`, whose `count()` is 0 and `(0 + 2) / 3 == 0`. The algorithm
         *   silently relies on integer division here; worth knowing it is load-bearing.
         * - Head gap comes from `prevLast = -1` for the first range; tail gap from the explicit post-loop check.
         * - `reversed.isEmpty()` can only be true when there were no bulbs at all (every merged range adds one entry),
         *   so that branch is really "`merged.isEmpty()` → the whole road is dark".
         * - Summing gaps independently is valid because two dark gaps are always separated by a lit block at least 3
         *   wide (an unclipped bulb lights `2v + 1 >= 3` cells, and clipping only happens at a road end, where there is
         *   no gap beyond it), so no single radius-1 bulb can serve two gaps.
         *
         * **Pattern.** *Interval merging* (sort by start, sweep, extend the last interval) followed by *interval
         * complement* (gaps between consecutive merged intervals) and *greedy fixed-width covering* (`ceil(L / w)`).
         * The merge + complement pair is the classic "free time between busy intervals" template (Employee Free Time,
         * Meeting Rooms).
         *
         * **Alternative approaches.**
         * - *Difference array* (see `referenceSolution`): `+1` at each start, `-1` past each end, prefix-sum sweep;
         *   cells with coverage 0 are dark. `O(n)` time, one `IntArray(n + 1)`, no sorting, no objects. It works because
         *   coordinates are bounded by `n`; the sort-based version is the one to keep when coordinates are huge or
         *   sparse (e.g. timestamps), where an array over the domain is unaffordable.
         * - *Bucket by start instead of sorting*: `maxEnd[s] = max end of any range starting at s` (array of size `n`),
         *   then one left-to-right sweep tracking the furthest reach — the Jump Game / Video Stitching trick. Also
         *   `O(n)`, and it keeps your "reach" mindset while dropping the `log n`.
         * - Nothing beats `O(n)`: every `lights[i]` must be read, since any single zero/non-zero flip can change the
         *   answer.
         *
         * **Parallelism.** At `n <= 10^5` the whole thing runs in well under a millisecond; thread startup would cost
         * more than the work. In principle: the sort parallelises (`parallelSort`), and the difference-array version
         * is a parallel prefix sum (scan) — each chunk counts its interior dark runs and reports its leading/trailing
         * dark lengths, which are stitched at the chunk boundaries. Memory-bandwidth bound, so the realistic ceiling
         * is a few x on very large inputs, never at these sizes.
         *
         * **Real-world.** Merge-then-complement is how calendars compute free/busy slots, how genomics tools find
         * zero-coverage regions in read alignments, how monitoring finds gaps in time-series data, and how coverage
         * planning (cell towers, sensors, CCTV) finds blind spots. There the intervals are usually timestamps or
         * genome coordinates — unbounded and sparse — which is exactly why the sort-based merge, not the difference
         * array, is the production default.
         */
        fun minLights(lights: IntArray): Int {
            //create light ranges
            val ranges = mutableListOf<IntRange>()
            lights.forEachIndexed { idx, slot ->
                if (slot == 0) return@forEachIndexed
                ranges.addLast(maxOf(0, idx - slot)..minOf(lights.lastIndex, idx + slot))
            }

            // sort+merge
            val sorted = ranges.sortedBy { it.first }
            val merged = mutableListOf<IntRange>()
            sorted.forEach { range ->
                val start = range.first
                val end = range.last
                if (merged.isNotEmpty() && merged.last().last >= start) {
                    val prev = merged.removeLast()
                    merged.addLast(prev.first..maxOf(end, prev.last))
                } else {
                    merged.addLast(start..end)
                }
            }

            //reverse
            val reversed = mutableListOf<IntRange>()

            merged.forEachIndexed { idx, range ->
                val prevLast = merged.getOrNull(idx - 1)?.last ?: -1 // 0 - 1
                val start = prevLast + 1
                val end = range.first - 1
                reversed.addLast(start..end)
            }

            if (merged.isNotEmpty() && merged.last().last < lights.lastIndex) {
                reversed.addLast(merged.last().last + 1..lights.lastIndex)
            }

            if (reversed.isEmpty()) {
                reversed.addLast(lights.indices)
            }

            // ceil range.size /3
            var result = 0
            reversed.forEach { darkArea ->
                result += (darkArea.count() + 2) / 3
            }

            return result
        }

        /**
         * ## Reference (lt-ref) — difference array (range coverage) + greedy over dark runs
         *
         * **Restatement.** A road has `n` cells. Some cells hold a bulb with radius `v` that lights `[i - v, i + v]`
         * (clipped to the road). Every new bulb we add has radius 1, i.e. lights at most 3 consecutive cells. Count the
         * fewest new bulbs so that every cell is lit.
         *
         * **Pattern.** Two classic pieces glued together:
         * 1. *Difference array / prefix-sum sweep* to mark which cells are already lit by many overlapping ranges in
         *    `O(n)` total — no sorting, no interval merging.
         * 2. *Greedy covering of a segment with fixed-length windows*: a maximal run of `L` dark cells needs exactly
         *    `ceil(L / 3)` radius-1 bulbs.
         *
         * It fits because ranges are bounded by the road (`0..n-1`), so an `O(n)` array is affordable, and the dark
         * runs are independent of each other — fixing one never helps another (beyond what the bound already counts).
         *
         * ### Intuition
         * Once you know which cells are dark, the problem collapses to: "cover each maximal dark run of length `L`
         * with windows of width 3". Every bulb covers at most 3 dark cells of a run, so you need at least `ceil(L/3)`;
         * placing bulbs at run positions 1, 4, 7, … (one cell inside the run's left edge) achieves it.
         *
         * Why can the runs be solved independently? A new bulb could only serve two runs at once if they were separated
         * by a *single* lit cell (`dark, lit, dark`). That never happens: an existing bulb with `v >= 1` lights at least
         * `2v + 1 >= 3` cells, and its range is only shortened by clipping at a road end — where there is no dark cell
         * on the far side. So every lit block *between* two dark runs is at least 3 wide, and no 3-wide bulb can reach
         * both runs.
         *
         * ### Core concepts
         * - **Difference array** — store `+1` at a range's start and `-1` just past its end; a running prefix sum then
         *   gives, for every cell, how many ranges cover it. Turns `k` range updates of total length up to `O(n²)` into
         *   `O(n + k)`. Here: "is cell `i` lit at all?" is just `prefix > 0`.
         * - **Clipping ranges to the domain** — `max(0, i - v)` and `min(n - 1, i + v)` keep indices in the array; since
         *   `v` can be as large as `n`, unclipped ends would overflow the array.
         * - **Maximal run** — a contiguous block of dark cells bounded by lit cells or the road ends. Scanning once and
         *   counting run length avoids building any interval lists.
         * - **Greedy window covering** — covering `L` consecutive cells with windows of width `w` needs exactly
         *   `ceil(L / w)`; compute it with integer math `(L + w - 1) / w`, no floating point.
         *
         * ### Approach
         * 1. Allocate `diff` of size `n + 1`. For every `i` with `lights[i] > 0`: `diff[max(0, i-v)]++`,
         *    `diff[min(n-1, i+v) + 1]--`.
         * 2. Sweep `i = 0..n-1`, keeping `cover += diff[i]`. If `cover == 0` the cell is dark: extend the current dark
         *    run. Otherwise close the run: add `(run + 2) / 3` to the answer and reset `run = 0`.
         * 3. After the loop, close the final run the same way (a dark tail at the road end).
         *
         * ### Complexity
         * `O(n) time / O(n) space` — one pass to fill the difference array and one sweep; the difference array holds
         * `n + 1` ints. (Sorting + merging the ranges instead would be `O(n log n)`.)
         *
         * ### Common pitfalls
         * - **Forgetting the last dark run** — the run only gets flushed when a lit cell follows; a dark tail at the end
         *   of the road must be flushed after the loop.
         * - **Off-by-one on run lengths** — a gap between lit cells `a` and `b` has `b - a - 1` dark cells, not `b - a`.
         * - **Not clipping** `i + v` / `i - v` — `v` may be up to `n`.
         * - **Interval-merge variant traps**: ranges sorted by start must merge by keeping `max(end)` (a nested range has
         *   a smaller end), and merged intervals that merely *touch* (`end + 1 == nextStart`) leave no gap.
         * - **Floating-point `ceil`** — fine at these sizes, but `(L + 2) / 3` is exact and cheaper.
         * - **No bulbs at all** — the whole road is one dark run of length `n`: answer `ceil(n / 3)`.
         */
        fun referenceSolution(lights: IntArray): Int {
            val n = lights.size
            val diff = IntArray(n + 1)
            for (i in 0 until n) {
                val v = lights[i]
                if (v == 0) continue
                diff[maxOf(0, i - v)]++
                diff[minOf(n - 1, i + v) + 1]--
            }

            var answer = 0
            var cover = 0
            var darkRun = 0
            for (i in 0 until n) {
                cover += diff[i]
                if (cover == 0) {
                    darkRun++
                } else {
                    answer += (darkRun + 2) / 3
                    darkRun = 0
                }
            }
            answer += (darkRun + 2) / 3
            return answer
        }
    }
}
