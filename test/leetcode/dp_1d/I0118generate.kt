package leetcode.dp_1d

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 118. Pascal's Triangle  (https://leetcode.com/problems/pascals-triangle/)
 *
 * Given an integer `numRows`, return the first `numRows` rows of Pascal's triangle.
 * Row `i` has `i + 1` entries; the first and last are `1`, and every other entry is the
 * sum of the two entries directly above it in the previous row.
 *
 * Constraints:
 * - 1 <= numRows <= 30  (so the answer is never empty, and values stay well inside Int range)
 */
typealias I0118 = (Int) -> List<List<Int>>

class I0118generate {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0118> {

        // Rows and the values within a row are order-sensitive here — the triangle's shape
        // *is* the answer, so `expects` (positional comparison) is correct.
        override val cases = leetcode.testCases<I0118>(
            5 expects "[[1],[1,1],[1,2,1],[1,3,3,1],[1,4,6,4,1]]",
            1 expects "[[1]]",
            2 expects "[[1],[1,1]]",
            6 expects "[[1],[1,1],[1,2,1],[1,3,3,1],[1,4,6,4,1],[1,5,10,10,5,1]]",
        )

        @Test
        fun test() = check(::generate, ::referenceSolution)

        /**
         * ## Pattern
         *
         * **Iterative DP over rows, expressed as recursion.** Each row is derived only from its
         * immediate predecessor (`row[j] = prev[j-1] + prev[j]`), so this is a 1-D DP with a
         * rolling "previous row" state. The recursion here is *linear* (one self-call per level),
         * i.e. a loop in disguise — not a branching/divide-and-conquer recursion.
         *
         * ## Time — O(numRows²)
         *
         * `genRow` is entered once per row (depth = `numRows`, guarded by `prev.size == numRows`).
         * At depth `i` the `row.indices.forEach` loop does `i + 1` constant-time steps
         * (two `getOrElse` reads on an `ArrayList` are O(1)). Summing `1 + 2 + … + numRows`
         * gives Θ(numRows²) — which is optimal, since the output itself contains that many
         * entries and every one must be written.
         *
         * `result.addAll(next)` adds a second Θ(numRows²) term: at depth `i` it copies
         * `numRows - i` *references* into a fresh list. Same asymptotic class, but it means the
         * triangle's outer list is rebuilt ~`numRows` times instead of once. An accumulator
         * passed *down* (making the function `tailrec`) would avoid it.
         *
         * ## Space — O(numRows²) total, O(numRows²) auxiliary, O(numRows) stack
         *
         * - **Output:** Θ(numRows²) `Int` boxes — unavoidable, it is the answer.
         * - **Stack:** Θ(numRows) frames; the recursive call on line `val next = genRow(row)` is
         *   *not* in tail position (work follows it), so Kotlin cannot eliminate it even with
         *   `tailrec`. At the constraint ceiling of 30 that is harmless; on an unbounded
         *   `numRows` it would be the first thing to break.
         * - **Auxiliary:** each frame holds its own `result` list of `numRows - i` references,
         *   so the live reference overhead is Θ(numRows²) beyond the output — the cost of
         *   building the answer bottom-up instead of appending into one shared list.
         *
         * ## Correctness notes
         *
         * - The base case keys off `prev.size == numRows` rather than a counter, which works
         *   because row `i` has exactly `i + 1` entries — the row length *is* the loop index.
         *   Neat, but it couples the terminator to that invariant.
         * - `getOrElse(index - 1) { 0 }` and `getOrElse(index) { 0 }` handle both edges
         *   uniformly: negative index at the left edge, out-of-range at the right, both → 0.
         *   That is the cleanest way to avoid the classic off-by-one at row boundaries.
         * - `if (num == 0) 1 else num` is the only load-bearing subtlety, and it is a bit of a
         *   coincidence: it exists to turn the *first* row's `0 + 0` into `1`. It is safe only
         *   because no interior Pascal entry is ever 0. Since `row` is already initialised to
         *   all-`1`s by `MutableList(prev.size + 1) { 1 }`, an equivalent and more obviously
         *   correct guard is to skip the edges entirely — e.g. iterate `1 until row.lastIndex`,
         *   or `if (prev.isEmpty()) return@forEach`.
         * - `numRows >= 1` per constraints, so the empty case never arises; the code would still
         *   return `[]` correctly if it did.
         * - **Overflow:** max value at `numRows = 30` is C(29,14) = 77,558,760 — comfortably
         *   inside `Int`. Around row 34 it would silently overflow; the constraint is what makes
         *   `Int` safe here, not the algorithm.
         *
         * ## Alternatives
         *
         * 1. **Plain iterative loop** — same Θ(n²)/Θ(n²), but O(1) stack and no intermediate
         *    outer lists: build `result`, and for each new row read `result.last()`. Strictly
         *    better on constants; the recursion buys nothing here since there is no branching.
         * 2. **Multiplicative binomials** — `row[j] = row[j-1] * (i - j + 1) / j`. Generates a
         *    single row in O(n) *without* the previous row, so it wins if you only need row `k`
         *    (O(k) time, O(1) extra). For the full triangle it is still Θ(n²) and adds overflow
         *    risk in the intermediate product, so the additive recurrence is preferable.
         * 3. **In-place backwards update** — keep one array and update right-to-left
         *    (`a[j] += a[j-1]` for `j` descending) to reuse a single buffer. Relevant when you
         *    only need to *stream* rows rather than return them all.
         *
         * No approach beats Θ(n²) for the full triangle: the output size is the lower bound.
         *
         * ## Parallelism
         *
         * **Not applicable to this formulation, and that is the teaching point.** Row `i`
         * strictly depends on row `i - 1` — a serial data dependency chain of length `numRows`.
         * No amount of hardware shortens it.
         *
         * The dependency is an artifact of the *recurrence*, not the *problem*: via the
         * closed-form C(i, j), every entry is independent, making the triangle embarrassingly
         * parallel (a pure map over (i, j), trivially SIMD-able). With `numRows <= 30` the whole
         * answer is ~465 additions — several orders of magnitude below the cost of spawning a
         * single thread, so any parallel version loses outright. The general lesson: when a DP
         * has a closed form, you can trade a serial recurrence for a parallel map — worth it only
         * once the work per element dwarfs the coordination overhead.
         *
         * ## Real-world experience
         *
         * Pascal's triangle is a binomial-coefficient table, and the pattern shows up as:
         * - **Combinatorics caches** — precomputed C(n, k) tables for probability, hashing, and
         *   counting problems; production code usually memoises a triangle exactly like this, or
         *   works in log-space (`lgamma`) once `n` outgrows 64-bit integers, or does modular
         *   arithmetic with factorials + modular inverses when results are taken mod a prime.
         * - **Grid path counting / DP** — "unique paths in an m×n grid" (LeetCode 62) *is* this
         *   recurrence; the same rolling-row trick is the standard space optimisation for any
         *   2-D DP whose cell depends only on the row above.
         * - **Signal & image processing** — binomial kernels `[1,2,1]`, `[1,4,6,4,1]` (literally
         *   rows 2 and 4) are the cheap integer approximation of a Gaussian blur, used in
         *   mipmap/pyramid generation because they are separable and shift-only.
         * - **Bezier curves** — de Casteljau/Bernstein weights are binomial coefficients; graphics
         *   and font rasterisers hardcode the low-order rows.
         *
         * The real-world constraint that differs most from the interview version is **magnitude**:
         * beyond ~row 34 the values leave `Int`, ~row 67 they leave `Long`, and you are forced
         * into `BigInteger`, floating-point log-space, or modular arithmetic — at which point the
         * "optimal" additive recurrence often loses to a formula-based evaluation that never
         * materialises the triangle at all.
         */
        fun generate(numRows: Int): List<List<Int>> {
            // 1
            // 1 1
            // 1 2 1
            // 1 3 3 1
            fun genRow(prev: List<Int> = emptyList()): List<List<Int>> {
                if (prev.size == numRows) return emptyList()
                val row = MutableList(prev.size + 1) { 1 }
                row.indices.forEach { index ->
                    val num = prev.getOrElse(index - 1) { 0 } + prev.getOrElse(index) { 0 }
                    row[index] = if (num == 0) 1 else num
                }
                val next = genRow(row)
                val result = mutableListOf<List<Int>>(row)
                result.addAll(next)
                return result
            }

            return genRow()
        }


        /**
         * ## Reference solution — the plain rolling-row loop
         *
         * ### Restatement
         *
         * Build the first `numRows` rows of Pascal's triangle. Row `i` (0-based) has `i + 1`
         * entries, both ends are `1`, and every interior entry is the sum of the two entries
         * diagonally above it.
         *
         * ### Pattern: 1-D DP with a rolling previous row
         *
         * This is the simplest member of a family you will meet constantly: a DP whose state at
         * step `i` depends **only** on step `i - 1`. Whenever you spot that, you do not need the
         * whole 2-D table in memory — you keep one row and roll it forward. (Same trick that
         * turns Unique Paths / Edit Distance / 0-1 Knapsack from O(n*m) space into O(m).)
         *
         * Here we happen to *return* every row, so we keep them all anyway — but the recurrence
         * is still "current row from previous row", and the previous row is simply
         * `result.last()`. No recursion, no accumulator threading.
         *
         * ### Approach
         *
         * 1. Start with `result = [[1]]`.
         * 2. For each subsequent row `i` in `1 until numRows`:
         *    - `prev = result.last()`
         *    - allocate `row` of length `i + 1`, pre-filled with `1` (this *is* the base case for
         *      both edges — no special-casing needed);
         *    - fill only the interior, `j in 1 until i`, with `prev[j - 1] + prev[j]`.
         * 3. Return `result`.
         *
         * Building with `List(i + 1) { j -> ... }` makes the edges fall out of `getOrElse`
         * naturally, which is the variant written below: index `-1` and index `i` are both
         * out of range on `prev`, so both default to `0`, and `0 + 1` / `1 + 0` give the `1`s.
         * That removes the `if (num == 0) 1 else num` fixup in the attempt above — the only
         * genuinely fragile line there, since it relies on "no interior entry is ever 0".
         *
         * ### Complexity
         *
         * - **Time Θ(numRows²)** — Σ(i + 1) constant-time cells; optimal, since the output has
         *   that many entries and each must be written.
         * - **Space Θ(numRows²)** for the output, **O(1)** auxiliary, **O(1)** stack. The
         *   recursive version pays Θ(numRows) stack frames and rebuilds the outer list at every
         *   level; this one appends into a single list.
         *
         * ### Pitfalls
         *
         * - **Off-by-one at the edges** — the classic bug is looping `0..i` and indexing
         *   `prev[j - 1]` (negative at `j == 0`) or `prev[j]` (out of range at `j == i`).
         *   Either pre-fill with `1` and loop the interior only, or use `getOrElse { 0 }`.
         * - **Row length is `i + 1`, not `i`.** Easy to drop the last `1`.
         * - **`numRows` is ≥ 1 by constraint**, so no empty-input branch is needed — but note
         *   the seed row must exist before the loop starts.
         * - **Overflow** is a non-issue only because `numRows <= 30` (max C(29,14) ≈ 7.8e7).
         *   Past row 34 `Int` silently wraps.
         * - **Don't mutate `prev`.** Rows already emitted are part of the answer; write into a
         *   fresh `row` each iteration (in-place updates are only safe with the right-to-left
         *   single-buffer variant, which cannot return all rows).
         */
        fun referenceSolution(numRows: Int): List<List<Int>> {
            val result = ArrayList<List<Int>>(numRows)
            var prev = emptyList<Int>()
            repeat(numRows) { i ->
                val row = List(i + 1) { j ->
                    prev.getOrElse(j - 1) { 0 } + prev.getOrElse(j) { if (j == 0) 1 else 0 }
                }
                result.add(row)
                prev = row
            }
            return result
        }


    }
}
