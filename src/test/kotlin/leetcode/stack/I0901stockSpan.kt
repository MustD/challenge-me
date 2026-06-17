package leetcode.stack

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 901. Online Stock Span  (https://leetcode.com/problems/online-stock-span/)
 *
 * Design a `StockSpanner` class that collects daily price quotes and returns the *span* of the
 * stock's price for the current day. The span is the maximum number of consecutive days (ending
 * with today, going backwards) for which the price was <= today's price.
 *
 * Original API (stateful, called day by day):
 *   StockSpanner()      // init
 *   int next(int price) // returns the span for `price`, given all prices seen so far
 *
 * NOTE — harness framing: this repo's test harness drives a single pure function once per case, so
 * the stateful `next`-per-day API doesn't map onto it directly. We model the problem as the
 * equivalent batch function: given the full sequence of daily prices, return the array of spans
 * (i.e. the result of replaying `next` over every price in order). The interesting part — the
 * monotonic stack — is identical; you just keep your own state across the loop instead of across
 * method calls.
 *
 * Example: prices [100,80,60,70,60,75,85] -> spans [1,1,1,2,1,4,6].
 *
 * Constraints:
 * - 1 <= price <= 1e5
 * - At most 1.5e4 calls to next per test on the original problem (so an O(n) amortized approach
 *   matters; a naive O(n^2) re-scan is the pitfall to avoid).
 */
typealias I0901 = (IntArray) -> IntArray

class I0901stockSpan {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0901> {

        override val cases = leetcode.testCases<I0901>(
            "[100,80,60,70,60,75,85]" expects "[1,1,1,2,1,4,6]",
            "[7,2,1,2]" expects "[1,1,1,3]",  // 4-day run where today=2 ends a [...,2,1,2] tail
            "[31,41,48,59,79]" expects "[1,2,3,4,5]",  // strictly increasing -> spans 1..n
            "[5]" expects "[1]",  // single day
            "[5,5,5]" expects "[1,2,3]",  // equal prices count (<= today)
            "[9,8,7,6]" expects "[1,1,1,1]",  // strictly decreasing -> all spans 1
        )

        @Test
        fun test() = check(
            ::stockSpanBF,
            ::stockSpan,
            ::referenceSolution
        )

        /**
         * Brute-force baseline — verified PASSING on all 6 cases.
         *
         * Pattern: naive backward scan ("expand-while-valid"). For each day `i`, walk backwards
         * (`prices[i - counter]`) counting consecutive days whose price is <= today's, stopping at the
         * first strictly-greater day or the start of the array. No auxiliary structure beyond the result.
         *
         * Correctness notes:
         *   - Uses `>=` (today's price `p` >= the earlier price), the mirror of the `<=` rule — equal
         *     prices are correctly counted, so [5,5,5] -> [1,2,3].
         *   - The `counter <= i` guard prevents underflowing past index 0, so the first day and any
         *     full-run-to-the-start case (e.g. strictly increasing [31,41,48,59,79]) are handled without
         *     a separate base case. Worst case `counter` reaches `i + 1`.
         *
         * Complexity:
         *   - Time: O(n^2) worst case. The inner `while` runs up to `i + 1` times per day; on a
         *     non-decreasing input (e.g. [1,2,3,...,n]) every day scans all the way back, giving
         *     1+2+...+n = O(n^2). On strictly decreasing input it's O(n) total (every inner loop exits
         *     after one comparison). This is exactly the pitfall the problem's 1.5e4-call constraint warns
         *     about.
         *   - Space: O(1) auxiliary (the `IntArray result` is output, not working space).
         *
         * Trade-off vs. the monotonic-stack `stockSpan`: this is simpler and allocation-free, and for tiny
         * inputs the constant factor can win — but it's asymptotically worse and degenerates precisely on
         * the rising-market inputs the stack version handles in amortized O(1)/day.
         */
        fun stockSpanBF(prices: IntArray): IntArray {
            val result = IntArray(prices.size)

            prices.forEachIndexed { i, p ->
                var counter = 0
                while (counter <= i) {
                    if (p >= prices[i - counter]) {
                        counter++
                    } else break
                }
                result[i] = counter
            }

            return result
        }

        /**
         * Monotonic (decreasing) stack — verified PASSING on all 6 cases. This is the intended optimal
         * solution and is logically identical to `referenceSolution` below; the only difference is the
         * stack element type (`Array<Int>` of [price, span] here vs. a `Pair<Int, Int>` there).
         *
         * Pattern: monotonic decreasing stack / "previous-greater-element" collapse. The stack holds
         * [price, span] for earlier days still strictly greater than every day after them. Today's span
         * starts at 1 and absorbs the span of every stacked day whose price is <= today's (`stack.last()[0]
         * <= price`), popping each as it folds in — accumulating the *popped spans*, not just the pop count,
         * so one pop can swallow a whole earlier run. A swallowed day can never be relevant again (any
         * future day clearing today's price already clears the smaller ones), which is why no re-scan is
         * needed.
         *
         * Correctness notes:
         *   - `<=` (not `<`) makes equal prices count: [5,5,5] -> [1,2,3].
         *   - Starts from an empty stack and never clears on a dip — a lower price just sits on top with
         *     span 1, waiting to be absorbed later: [7,2,1,2] -> [1,1,1,3].
         *   - Edge cases: single day [5] -> [1] (loop body never enters); strictly decreasing [9,8,7,6] ->
         *     all 1s (every `while` guard fails immediately, stack grows monotonically).
         *
         * Complexity:
         *   - Time: O(n) amortized. The `forEachIndexed` is n iterations; the inner `while` looks unbounded
         *     but each element is pushed exactly once (`addLast`) and popped at most once (`removeLast`)
         *     across the whole run, so total inner work is <= n. Worst-case single-step cost is O(n) (a tall
         *     run collapsing at once) but the amortized per-day cost is O(1).
         *   - Space: O(n) for the stack — degenerate on strictly decreasing input, where nothing is ever
         *     popped and all n entries pile up. (Plus O(n) output.)
         *
         * Minor note (not a bug): boxing. `Array<Int>` and `ArrayDeque<Array<Int>>` box every int into
         * `Integer` and allocate a 2-element object array per day. `referenceSolution`'s `Pair<Int,Int>`
         * has the same boxing cost. For the tightest constant factor you'd encode each (price, span) into a
         * single `Long` (price shl 32 or span) on a `LongArray`/`ArrayDeque<Long>`, avoiding all object
         * allocation. Irrelevant at n <= 1.5e4, but it's the real-world reflex.
         *
         * Parallelism: not applicable. The span computation is inherently sequential — each day's stack
         * state depends on the fully-resolved state after the previous day (a strict data dependency / scan
         * with a non-trivial carry). It can't be partitioned into independent chunks without a divide-and-
         * conquer "all-nearest-greater-element" formulation, which exists in theory (parallel ANSV) but is
         * far more complex and pointless at this scale. The honest answer: keep it single-threaded.
         *
         * Real-world: this is the "online" framing the LeetCode title hints at — in production the original
         * `StockSpanner.next(price)` is the natural shape: a streaming/unbounded feed where each tick must
         * answer in O(1) amortized and you keep the monotonic stack as long-lived state. The batch
         * IntArray form here is just the harness adaptation. The same previous-greater-element stack powers
         * "largest rectangle in histogram", "daily temperatures", and trailing-window analytics over price
         * ticks. At true scale the unbounded stack matters — for an infinite stream you'd cap memory and
         * accept approximate spans, since a never-ending strictly-decreasing run would grow the stack
         * forever.
         */
        fun stockSpan(prices: IntArray): IntArray {
            val result = IntArray(prices.size)

            // price to span
            val stack = ArrayDeque<Array<Int>>()


            prices.forEachIndexed { index, price ->
                var span = 1
                while (stack.isNotEmpty() && stack.last()[0] <= price) {
                    span += stack.removeLast()[1]
                }
                stack.addLast(arrayOf(price, span))
                result[index] = span
            }

            return result
        }

        /**
         * Pattern: monotonic (decreasing) stack — the canonical "previous greater element" tool.
         *
         * Intuition: today's span = 1 (for today) + the spans of all earlier days that are "swallowed"
         * because their price was <= today's. The key trick is that once a day is swallowed by a higher
         * later price, it can never matter again — any future day that clears today's price already
         * clears those smaller ones too. So instead of re-scanning, we *collapse* each consumed day into
         * the running span and pop it off the stack for good. That collapsing is what makes the work
         * amortized O(1) per day: every price is pushed once and popped at most once.
         *
         * Stack contents: pairs of (price, span) for days that are still strictly greater than every day
         * after them — i.e. a strictly decreasing stack of prices. The top is the most recent
         * still-relevant day.
         *
         * Approach (replaying `next` over each price):
         *   1. Start span = 1 (today alone).
         *   2. While the stack is non-empty and its top price <= today's price, pop it and add its span
         *      into today's span (absorbing that whole earlier run).
         *   3. Push (today's price, accumulated span); record span as the answer for this day.
         *
         * Complexity: O(n) time amortized (each element pushed/popped once), O(n) space for the stack.
         *
         * Pitfalls:
         *   - Use `<=` not `<`: equal prices count toward the span ([5,5,5] -> [1,2,3]).
         *   - Accumulate the *popped* spans, don't just count pops — popping one entry can fold in many
         *     days at once (that's the whole point).
         *   - Don't reset/clear the stack on a drop in price; a lower price just sits on top with span 1
         *     and waits to be absorbed later ([7,2,1,2] -> [1,1,1,3]).
         *
         */
        fun referenceSolution(prices: IntArray): IntArray {
            val result = IntArray(prices.size)
            // stack of (price, span) for still-relevant earlier days, strictly decreasing by price
            val stack = ArrayDeque<Pair<Int, Int>>()

            prices.forEachIndexed { index, price ->
                var span = 1
                while (stack.isNotEmpty() && stack.last().first <= price) {
                    span += stack.removeLast().second
                }
                stack.addLast(price to span)
                result[index] = span
            }

            return result
        }

    }
}
