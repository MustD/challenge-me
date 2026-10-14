package leetcode.array_string

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4014. Minimum Total Price After Applying Discounts  (https://leetcode.com/problems/minimum-total-price-after-applying-discounts/)
 *
 * You are given two integer arrays, `prices` and `discounts`. `prices[i]` is the price of the i-th item, and
 * `discounts[j]` is a discount percentage.
 *
 * Each discount can be applied to at most one item, and each item can receive at most one discount (or none).
 *
 * Applying a discount of `d` percent to an item with price `p` makes its final price `p * (100 - d) / 100`,
 * with no rounding.
 *
 * Return the minimum possible sum of the final prices of all items. Answers within 10^-5 of the actual answer
 * will be accepted.
 *
 * Constraints:
 * - 1 <= prices.length, discounts.length <= 10^5
 * - 1 <= prices[i] <= 10^5
 * - 1 <= discounts[j] <= 100
 */
typealias I4014 = (IntArray, IntArray) -> Double

class I4014minPrice {

    @Nested
    inner class Solution : ProblemTest<I4014> {

        override val cases = testCases<I4014>(
            args("[10,30,21]", "[50,60]") expects 32.5,
            args("[100,70]", "[10,40,50]") expects 92.0,
            args("[7,3,9]", "[100,100]") expects 3.0,
            args("[1]", "[1]") expects 0.99,                                     // single item, minimum values
            args("[1]", "[100]") expects 0.0,                                    // 100% discount -> item is free
            args(
                "[5,5,5]",
                "[20,20]"
            ) expects 13.0,                             // all-equal prices, one item undiscounted
            args(
                "[1,100]",
                "[90,10]"
            ) expects 10.9,                             // input order is a trap: index pairing gives 90.1
            args("[100000,100000]", "[100,100]") expects 0.0,                    // max price, every item free
            args("[100000,100000,100000]", "[1,1,1]") expects 297000.0,          // max prices, large total
            args(
                "[21,21,21]",
                "[1,1,1]"
            ) expects 62.37,                         // fractional result not exact in binary
        )

        @Test
        fun test() = check(::minPrice, ::referenceSolution)

        /**
         * ## Attempt assessment (leetcode-help)
         * Correct, and already the optimal approach: greedy "sort both descending, pair by rank", O(n log n + m log m)
         * time — the same as the reference. Nothing to fix for correctness. The only gap vs. the reference is
         * numeric: it accumulates `Double`s (`price / 100 * …`), so each item adds a rounding error. To close it,
         * change the loop body to accumulate `sum += it.toLong() * (100 - discount)` in a `Long` and return
         * `sum / 100.0` once at the end; `.coerceAtLeast(0)` can then be dropped.
         *
         * ## Analysis (validated: all 3 official + 7 added cases pass)
         *
         * ### Pattern
         * **Greedy pairing via sorting / rearrangement inequality.** The total is
         * `Σ p - Σ p·d/100`, so minimizing the total = maximizing the savings `Σ p_i·d_σ(i)`.
         * The rearrangement inequality says a sum of products is maximal when both sequences are sorted
         * the same way: largest discount on the most expensive item, 2nd-largest on the 2nd-most expensive, …
         * Exchange argument: if a bigger discount `D` sat on a cheaper item `q` while a smaller `d` sat on
         * a pricier `P`, swapping changes savings by `(P - q)(D - d) >= 0`, so a swap never hurts.
         * When there are more discounts than items, only the largest `n` are worth using; when there are more
         * items than discounts, the cheapest items go undiscounted — `getOrElse(index) { 0 }` handles both.
         *
         * ### Time — O(n log n + m log m)
         * - `prices.sortedDescending()` → O(n log n), `discounts.sortedDescending()` → O(m log m).
         * - The `forEachIndexed` loop is O(n) with O(1) `getOrElse` on a `List`.
         * - Only the first `min(n, m)` discounts matter, so `discounts` could be partially selected
         *   (or counting-sorted — values are only 1..100) but sorting dominates anyway.
         *
         * ### Space — O(n + m) auxiliary
         * `sortedDescending()` on an `IntArray` returns a new boxed `List<Int>` — two copies, and each
         * element is an `Integer` object (~16 B + pointer) rather than 4 B. Sorting in place
         * (`prices.sortDescending()`) would be O(1) extra beyond the sort, at the cost of mutating input.
         *
         * ### Correctness notes
         * - Order of the input is irrelevant (case `[1,100] / [90,10]`: index pairing gives 90.1, greedy 10.9).
         * - `.coerceAtLeast(0)` is dead code: constraints cap `d` at 100, so `100 - d >= 0` already.
         * - **Floating-point drift.** `price / 100` is not exact in binary (`21 / 100 = 0.21…`), and each `+=`
         *   rounds again. Examples: `[3,1,2] / [10..50]` yields `3.4000000000000004` instead of `3.4`;
         *   `[99999,1,50000] / [1,99,50]` yields `26000.980000000003`. LeetCode's 1e-5 tolerance absorbs
         *   this, but this repo's harness compares `Double` exactly, so those inputs were left out of `cases`.
         *   Worst case: 1e5 items, totals up to 1e10 where one ulp ≈ 1.9e-6; accumulated rounding over 1e5
         *   additions can in principle approach the 1e-5 tolerance.
         *   **Exact fix:** accumulate integer "cents" — `sum += p.toLong() * (100 - d)` (max 1e5 · 1e5 · 100
         *   = 1e12, needs `Long`, overflows `Int`) — and divide by `100.0` once at the end. One rounding total.
         *
         * ### Alternatives
         * - **Counting sort on discounts** (values 1..100): O(m + 100) for that side; prices still need
         *   O(n log n), or only the top `min(n, m)` prices via a size-k heap / quickselect → O(n log k).
         * - **Savings formulation:** `total = Σp - Σ(top-k p_i · top-k d_i)/100`, all in `Long`. Same
         *   complexity, but makes the "maximize savings" view explicit and is exact.
         * - Asymptotically, Ω(k log k) is needed in the comparison model to know the pairing order of the
         *   top `k` items, so sort-based is essentially optimal; the bounded discount range is the only lever.
         *
         * ### Parallelism
         * The two sorts are independent and could run concurrently; a parallel sort
         * (`Arrays.parallelSort`) helps for arrays of millions, and the final dot-product/sum is an
         * embarrassingly parallel reduction (SIMD-friendly in `Long`). At n = 1e5 the whole thing runs in a
         * few ms; thread start-up and coordination would cost more than the work. Not worth it here.
         *
         * ### Real world
         * This is the shape of "assign scarce resources to where they have most leverage": applying coupons
         * to the most expensive cart lines, giving the biggest compute boost to the heaviest jobs, matching
         * top bids to top slots (ad-auction "assortative matching"). In real money code you never use
         * `Double` — prices are integer minor units (`Long` cents) or `BigDecimal` with explicit rounding
         * rules, exactly because of the drift seen above. Real promo engines also add constraints
         * (eligibility, stacking rules, caps) that break pure greedy and push toward matching/ILP.
         */
        fun minPrice(prices: IntArray, discounts: IntArray): Double {
            var result = 0.0
            val sortedP = prices.sortedDescending()
            val sortedD = discounts.sortedDescending()
            sortedP.forEachIndexed { index, it ->
                val price = it.toDouble()
                val discount = sortedD.getOrElse(index) { 0 }
                val discountedPrice = (price / 100) * (100 - discount).coerceAtLeast(0)
                result += discountedPrice
            }

            return result
        }

        /**
         * ## Reference solution
         *
         * ### Restatement
         * There are `n` items with prices and `m` discount coupons (percentages). Each coupon may be used on at most
         * one item and each item takes at most one coupon. Choose the pairing that makes the total bill smallest.
         *
         * ### Pattern — greedy matching by sorting (rearrangement inequality)
         * Total = `Σ p` − `Σ p·d / 100`. The first term is fixed, so minimizing the bill means **maximizing the
         * savings** `Σ p·d` over the chosen pairs. A sum of pairwise products is largest when both sequences are
         * sorted the same way, so: biggest coupon on the most expensive item, second-biggest on the second-most
         * expensive, and so on.
         *
         * ### Intuition
         * A percentage discount is worth more on a bigger price — 50% off 100 saves 50, off 10 saves only 5. So the
         * strongest coupon should never be "wasted" on a cheap item while a pricier item gets a weaker one: swapping
         * them changes savings by `(P − q)(D − d) ≥ 0` for `P ≥ q`, `D ≥ d`. Repeating such swaps turns any pairing
         * into the sorted one without ever losing savings, so the sorted pairing is optimal.
         *
         * ### Core concepts
         * - **Rearrangement inequality** — for two sequences, `Σ aᵢ·b_σ(i)` is maximal when `a` and `b` are sorted in
         *   the same order. It is the formal reason "match largest with largest" works here.
         * - **Exchange argument** — the standard way to prove a greedy: show that swapping any out-of-order pair
         *   never makes the answer worse. Here the swap gain is `(P − q)(D − d)`.
         * - **Unbalanced matching** — with `k = min(n, m)` pairs, only the top `k` prices and top `k` coupons
         *   matter; extra coupons are dropped, extra (cheapest) items are paid in full.
         * - **Integer accumulation, one final division** — keep money in exact integer units (`p·(100 − d)` is
         *   "price in hundredths") and convert to floating point once, so there is a single rounding instead of `n`.
         *
         * ### Approach
         * 1. Sort copies of `prices` and `discounts` ascending (copies, so the caller's arrays are not mutated).
         * 2. Walk both from the largest end: the `i`-th largest price pairs with the `i`-th largest discount while
         *    discounts remain; afterwards the discount is `0`.
         * 3. Accumulate `p · (100 − d)` in a `Long` (up to `1e5 · 1e5 · 100 = 1e12`, which overflows `Int`).
         * 4. Return `sum / 100.0`.
         *
         * ### Complexity
         * - Time: O(n log n + m log m) — the two sorts dominate; the pairing pass is O(n).
         * - Space: O(n + m) for the sorted primitive copies (O(1) extra if mutating the input were allowed).
         *
         * ### Common pitfalls
         * - Pairing by input index instead of by rank (`[1,100]` / `[90,10]` → 90.1 instead of 10.9).
         * - Indexing past the end of the shorter array when `n ≠ m`.
         * - `Int` overflow of `p · (100 − d)` summed over 1e5 items — use `Long`.
         * - Summing per-item `Double`s: `21 / 100.0` is not exact in binary, and the error grows with every add.
         * - Using integer division (`p * (100 − d) / 100` in `Int`) — the answer is fractional, not floored.
         */
        fun referenceSolution(prices: IntArray, discounts: IntArray): Double {
            val p = prices.copyOf().apply { sort() }
            val d = discounts.copyOf().apply { sort() }
            var sum = 0L
            for (i in p.indices) {
                val price = p[p.lastIndex - i]                     // i-th most expensive
                val discount = if (i < d.size) d[d.lastIndex - i] else 0  // i-th largest coupon, or none
                sum += price.toLong() * (100 - discount)
            }
            return sum / 100.0
        }

    }
}
