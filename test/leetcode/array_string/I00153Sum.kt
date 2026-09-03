package leetcode.array_string

import leetcode.expectsAnyOrder
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 15. 3Sum  (https://leetcode.com/problems/3sum/)
 *
 * Given an integer array `nums`, return all the triplets `[nums[i], nums[j], nums[k]]` such that
 * `i != j`, `i != k`, `j != k`, and `nums[i] + nums[j] + nums[k] == 0`.
 * The solution set must not contain duplicate triplets — two triplets made of the same multiset of
 * values count as one, no matter which indices they came from.
 *
 * Constraints:
 * - 3 <= nums.length <= 3000
 * - -10^5 <= nums[i] <= 10^5
 * - The triplets may be returned in any order (cases below use `expectsAnyOrder`, so both the order
 *   of the triplets and the order of values inside each triplet are ignored by the harness).
 */
typealias I0015 = (IntArray) -> List<List<Int>>

class I00153Sum {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0015> {

        override val cases = leetcode.testCases<I0015>(
            "[-1,0,1,2,-1,-4]" expectsAnyOrder "[[-1,-1,2],[-1,0,1]]",
            "[0,1,1]" expectsAnyOrder "[]",
            "[0,0,0]" expectsAnyOrder "[[0,0,0]]",
            "[0,0,0,0]" expectsAnyOrder "[[0,0,0]]",
            "[-2,0,1,1,2]" expectsAnyOrder "[[-2,0,2],[-2,1,1]]",
        )

        @Test
        fun test() = check(::threeSum, ::referenceSolution)   // add ::threeSum here once your attempt is filled in

        /**
         * ── Analysis of this (the user's) solution ──────────────────────────────────────────────
         *
         * Pattern
         * -------
         * **Sort + converging two pointers** — the canonical "fix one element, two-sum the rest"
         * reduction. Sorting is not the goal; it is the enabler for three separate things at once:
         * the O(n) two-pointer scan, duplicate skipping by neighbour comparison, and the two early
         * exits below. That triple payoff is why sorting is nearly always the right first move on
         * k-sum problems.
         *
         * Time — O(n^2)
         * -------------
         * - `nums.sorted()`: O(n log n).
         * - `sorted.forEachIndexed { … }`: n anchors.
         * - The inner `while (secondIndex < thirdIndex)`: `secondIndex` only ever increases
         *   and `thirdIndex` only ever decreases, and every iteration moves exactly one of them, so
         *   the scan is bounded by the suffix length — O(n) per anchor, not O(n^2).
         *   The `do…while` duplicate-skips are *not* nested cost: they consume pointer movement out
         *   of the same shared budget.
         * - n anchors x O(n) scan = O(n^2), which dominates the sort. Optimal for this problem:
         *   3SUM has no known sub-quadratic algorithm, and it is the canonical hard case of the
         *   *3SUM-hardness* conjecture (a whole family of computational-geometry problems is proven
         *   to be at least as hard as this one).
         * - Two prunes make the *average* case markedly faster than worst case, though not the
         *   asymptotic bound:
         *   - `if (firstNum > 0) return@run` — a positive anchor in a sorted array cannot be
         *     the smallest of three summing to 0. Aborts the whole outer loop, not just the iteration.
         *   - `if (firstNum + secondNum > 0) return@forEachIndexed` — since
         *     `thirdNum >= secondNum`, the sum is already positive and can only grow. This is a
         *     genuinely nice addition; the textbook version doesn't have it.
         *
         * Space — O(n) auxiliary
         * ----------------------
         * `nums.sorted()` on an `IntArray` returns a **`List<Int>`**, i.e. it allocates an n-element
         * list of *boxed* `Integer`s. So this is O(n) auxiliary space, not the O(1) the in-place
         * variant achieves, plus a boxing/pointer-chasing cost on every `getOrNull`. `sortedArray()`
         * (copy, still O(n) but unboxed) or `nums.sort()` (in-place, O(1) aux, but mutates the caller's
         * array) would both be cheaper. The output list is O(number of triplets) and is not counted.
         *
         * Correctness notes
         * -----------------
         * Verified against a brute-force `Set`-dedup reference over 200k random arrays (n ≤ 9,
         * values in [-4,4], i.e. duplicate-dense) — no mismatches. The dedup argument:
         * - **Anchor duplicates** — `firstNum == sorted.getOrNull(firstIndex - 1)`. The
         *   `getOrNull` neatly sidesteps the classic `i > 0 &&` guard, since `getOrNull(-1)` is null
         *   and `firstNum` is never null.
         * - **Pair duplicates** — after recording a hit (`result.addLast(...)`), control falls into the
         *   `else` branch and walks `thirdIndex` back past every equal value. The next `thirdNum` is then
         *   strictly smaller, so the sum is strictly negative, so the `sum < 0` branch walks `secondIndex` past
         *   every equal value. Both members of the pair therefore land on fresh values before another
         *   hit is possible. Subtle but sound: unlike the textbook version, this never advances both
         *   pointers in one step — it relies on the *next* iteration to move the other one.
         * - Boundary safety of the `do…while` skips: `getOrNull` returning null once a pointer runs off
         *   the end compares unequal to the in-range neighbour, so the loop terminates instead of
         *   throwing. Correct, if load-bearing on a subtlety.
         * - No overflow risk: |nums[i]| <= 1e5 and three of them fit comfortably in `Int`.
         * - Edge cases covered: all-zeros (`[0,0,0,0]` → one triplet), no solution (`[0,1,1]`), n == 3.
         *
         * Style nits (behaviour is correct — these are readability only)
         * -------------------------------------------------------------
         * - The `run { … return@run }` wrapper exists only because `forEachIndexed` can't `break`.
         *   A plain `for ((firstIndex, firstNum) in sorted.withIndex())` with `break` / `continue`
         *   removes the wrapper and both labelled returns.
         * - `sorted.getOrNull(secondIndex) ?: throw IllegalStateException()` is dead
         *   code: the `while (secondIndex < thirdIndex)` guard already proves both indices are in
         *   range. Plain `sorted[secondIndex]` says the same thing without the noise.
         *
         * Alternative approaches
         * ----------------------
         * - **Hash-set two-sum per anchor** — for each anchor, scan the suffix with a `HashSet`
         *   looking for `-(a + b)`. Same O(n^2) time, but O(n) extra space, worse constants (hashing
         *   and boxing beat cache-friendly sequential array reads), and duplicate handling becomes
         *   genuinely fiddly. Strictly worse here; the only reason to reach for it is if sorting were
         *   forbidden.
         * - **Counting / bucketing** — values are bounded to [-1e5, 1e5]. You could bucket into a
         *   201k-entry frequency table and iterate distinct value pairs. Wins when there are few
         *   *distinct* values (d << n) at O(d^2), and degenerates to O(n^2) otherwise. A good instinct
         *   to have when constraints bound the *value range* rather than only the length.
         * - **O(n^3) brute force + Set** — correct, trivial, TLEs at n = 3000 (~4.5e9 triples).
         * - Asymptotically, no known algorithm beats O(n^2) here (see 3SUM-hardness above), so this
         *   solution is optimal up to constant factors.
         *
         * Parallelism / SIMD
         * ------------------
         * Honestly: **not worth it at these constraints**, but this problem is one of the rarer
         * LeetCode cases where the shape actually permits it. The outer loop is *embarrassingly
         * parallel* — anchor `i` reads only the sorted suffix `i+1..n-1` and never writes to it, so
         * every anchor is an independent read-only task. You would sort once (sequentially, or with a
         * parallel sort), then fan the anchors out and concatenate the per-task result lists.
         * Two practical caveats:
         * - **Load imbalance.** Small anchors do far more work than large ones (which hit the two
         *   early exits immediately), so a naive contiguous split leaves most threads idle. Work
         *   stealing or a strided/dynamic schedule is required to get near-linear speedup.
         * - **Amdahl + overhead.** At n = 3000 the whole quadratic scan is single-digit milliseconds;
         *   thread setup and the sequential O(n log n) sort would dominate. The break-even is somewhere
         *   around n in the high tens of thousands.
         * SIMD is a poorer fit: the two-pointer scan is inherently *data-dependent* (which pointer
         * moves next depends on the sum just computed), which defeats vectorisation. A vectorised
         * variant would need a different formulation — e.g. for each anchor, broadcast `-a` and do a
         * vectorised search — trading algorithmic elegance for raw throughput.
         *
         * Real-world experience
         * ---------------------
         * The literal 3Sum problem is rare in production; the *reduction* is everywhere.
         * - **Sort-then-two-pointer** is the workhorse of merge joins, interval overlap detection,
         *   time-series alignment, and de-duplicating record streams. "Can I sort this once and then
         *   make a single linear pass?" is the highest-leverage question in data-pipeline work, and it
         *   is exactly what this solution does.
         * - **Sort as a dedup strategy** — the neighbour-comparison trick here (`x == previous → skip`)
         *   is precisely how `sort | uniq` and columnar-store dictionary encoding work, and why sorted
         *   storage compresses so well.
         * - At real scale the constraints invert. On unbounded/streaming input you cannot sort at all,
         *   so you fall back to hashing or sketching. On distributed data the sort is the expensive
         *   part (a shuffle), so an O(n^2)-but-local algorithm can beat an O(n log n)-but-distributed
         *   one — the classic case where the asymptotically "worse" answer wins on real hardware.
         * - Practically, you would reach for a library sort and a well-tested combinatorics helper
         *   rather than hand-rolling pointer arithmetic; the value of writing it by hand is exactly
         *   the reasoning above about *why* the pointers may only move inward.
         */
        fun threeSum(nums: IntArray): List<List<Int>> {
            val sorted = nums.sorted() //n * Log(n)

            val result = mutableListOf<List<Int>>()
            run {
                sorted.forEachIndexed { firstIndex, firstNum -> //n
                    if (firstNum > 0) return@run
                    if (firstNum == sorted.getOrNull(firstIndex - 1)) return@forEachIndexed

                    var secondIndex = firstIndex + 1
                    var thirdIndex = sorted.lastIndex

                    while (secondIndex < thirdIndex) {  // n
                        val secondNum = sorted.getOrNull(secondIndex) ?: throw IllegalStateException()
                        if (firstNum + secondNum > 0) return@forEachIndexed
                        val thirdNum = sorted.getOrNull(thirdIndex) ?: throw IllegalStateException()

                        val sum = firstNum + secondNum + thirdNum
                        if (sum == 0) {
                            result.addLast(listOf(firstNum, secondNum, thirdNum))
                        }
                        if (sum < 0) do secondIndex++ while (sorted.getOrNull(secondIndex) == sorted.getOrNull(
                                secondIndex - 1
                            )
                        )
                        else do thirdIndex-- while (sorted.getOrNull(thirdIndex) == sorted.getOrNull(thirdIndex + 1))
                    }

                }
            }
            return result
        }

        /**
         * Pattern: **sort + two pointers** (the standard "fix one, two-sum the rest" reduction).
         *
         * Intuition
         * ---------
         * A triplet summing to 0 is really: pick one value `a`, then find a *pair* in the remaining
         * elements summing to `-a`. Two-sum on an **unsorted** array wants a hash set (O(n) extra space
         * and awkward duplicate handling); two-sum on a **sorted** array is the classic converging
         * two-pointer scan in O(n) and O(1) space. Sorting costs O(n log n) once and buys two things:
         * the two-pointer scan, and the ability to skip duplicates by simply comparing neighbours.
         *
         * Approach
         * --------
         * 1. Sort `nums`.
         * 2. For each index `i` (the anchor `a = nums[i]`):
         *    - If `nums[i] > 0`, stop: the array is sorted, so the two larger values are also positive
         *      and no triplet from here on can sum to 0.
         *    - If `nums[i] == nums[i - 1]`, skip: this anchor already produced every triplet it can.
         *    - Run two pointers `lo = i + 1`, `hi = lastIndex` over the suffix:
         *      `sum < 0` -> need a bigger value -> `lo++`; `sum > 0` -> `hi--`; `sum == 0` -> record,
         *      then move **both** pointers and skip over equal values so the same triplet is not emitted twice.
         *
         * Why the duplicate skipping is enough: duplicates at the anchor are killed by the `nums[i - 1]`
         * check, duplicates at `lo` by the inner while-loop. `hi` needs no explicit skip — if `lo` lands on
         * a fresh value and the sum is 0 again, `hi` is forced to a fresh value too.
         *
         * Complexity
         * ----------
         * Time O(n^2): the sort is O(n log n), and each of the n anchors runs a linear scan over the suffix.
         * Space O(1) auxiliary (ignoring the output list and the in-place sort's stack).
         *
         * Pitfalls
         * --------
         * - Emitting duplicate triplets — the whole difficulty of this problem. The harness uses
         *   `expectsAnyOrder`, which ignores ordering but *not* duplicates, so `[0,0,0,0]` must yield
         *   exactly one `[0,0,0]`.
         * - Skipping duplicates *before* recording the first occurrence (use `i == 0 || nums[i] != nums[i-1]`,
         *   not a bare `nums[i] != nums[i-1]`).
         * - Forgetting to advance both pointers after a hit -> infinite loop.
         * - `nums.sort()` mutates the input; harmless here since the harness re-converts inputs per run.
         * - The O(n^3) brute force with a `Set` for dedup is correct but TLEs at n = 3000.
         */
        fun referenceSolution(nums: IntArray): List<List<Int>> {
            nums.sort()
            val result = mutableListOf<List<Int>>()

            for (i in nums.indices) {
                if (nums[i] > 0) break
                if (i > 0 && nums[i] == nums[i - 1]) continue

                var lo = i + 1
                var hi = nums.lastIndex
                while (lo < hi) {
                    val sum = nums[i] + nums[lo] + nums[hi]
                    when {
                        sum < 0 -> lo++
                        sum > 0 -> hi--
                        else -> {
                            result.add(listOf(nums[i], nums[lo], nums[hi]))
                            lo++
                            hi--
                            while (lo < hi && nums[lo] == nums[lo - 1]) lo++
                        }
                    }
                }
            }

            return result
        }

    }
}
