package leetcode.hash_map

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4007. Widest Possible Fence  (https://leetcode.com/problems/widest-possible-fence/)
 *
 * You are given an integer array `planks`, where `planks[i]` is the height of the i-th wooden plank. Each plank is
 * 1 unit wide. You want to build a fence made of planks that all have the same height. Each plank can either be
 * used as is, or two different original planks can be combined into one plank whose height is the sum of their
 * heights. Each original plank can be used at most once, and you do not have to use all of them.
 *
 * Return the maximum possible width of the fence, meaning the maximum number of planks of one common height you
 * can build.
 *
 * Constraints:
 * - 1 <= planks.length <= 1000
 * - 1 <= planks[i] <= 10^9
 */
typealias I4007 = (IntArray) -> Int

class I4007maximumWidth {

    @Nested
    inner class Solution : ProblemTest<I4007> {

        override val cases = testCases<I4007>(
            "[1,3,2,5,7,5,4,2,1]" expects 4,
            "[2,3,7]" expects 1,
            "[5]" expects 1, // single plank
            "[1,1]" expects 2, // using both as-is beats combining them into one
            "[3,3,3,6]" expects 3, // odd count of h/2: only floor(3/2) self-pairs at H=6, so H=3 wins
            "[3,3,6,1,5,2,4]" expects 4, // all three groups at once: as-is 6 + (3,3) + (1,5) + (2,4)
            "[1,1,2,2,3,3]" expects 4, // multiplicity matching: H=3 -> two 3s + min(2,2) (1,2) pairs
            "[1,2,10,11]" expects 2, // best height exists only as a sum, above max (12)
            "[100000,100000,1]" expects 2, // large values (range scan stays cheap at 1e5; 1e9 would not)
        )

        @Test
        fun test() = check(::maximumWidth, ::referenceSolution)

        /**
         * # Widest Possible Fence: reference solution
         *
         * **Restatement.** Pick one target height `H`. Every fence plank must be either one original plank of height
         * `H`, or two different original planks whose heights sum to `H`. No original plank may be used twice.
         * Maximize the number of fence planks over all choices of `H`.
         *
         * **Pattern: pair enumeration with "accumulate by key"**, a cousin of *two-sum / pair counting with a
         * frequency map*. The obvious approach fixes `H` and counts. Here we turn the loops around: enumerate every
         * pair of distinct heights once, and add what that pair can contribute into a bucket keyed by its sum. This
         * fits because the only heights worth trying are plank values and pairwise sums. There are O(d²) of those, so
         * any approach that loops over the *value range* (up to 2·10^9) is the wrong choice.
         *
         * ### Intuition
         * Fix `H`. A plank of height `x` can only ever pair with a plank of height `H - x`. So the heights split into
         * **independent groups** `{x, H - x}` that never compete for the same planks. Each group's best is
         * `min(cnt[x], cnt[H-x])`, or `cnt[x] / 2` when `x == H - x`. Planks already of height `H` stand alone. Because
         * the groups are independent, the answer for `H` is just a **sum of per-pair contributions**. Every unordered
         * pair of distinct heights `(x, y)` contributes to exactly one target, `x + y`. So one pass over all pairs fills
         * in the totals for every `H` at the same time.
         *
         * ### Core concepts
         * - **Frequency map (multiset by value).** Collapse equal values into `value -> count`. Here it turns n planks
         *   into d distinct heights, and the pair loop then runs over d², not n².
         * - **Independent sub-problems / decomposition.** When the parts of a choice do not share resources, the
         *   global optimum is the sum of the local optima. That is why greedy `min(...)` per group is exact here and
         *   no general matching algorithm is needed.
         * - **Inverting the enumeration (contribution technique).** Instead of "for each answer, scan the data", do
         *   "for each piece of data, add its contribution to the answer it affects". This often cuts a factor, or (as
         *   here) replaces a value-range loop with a data-size loop.
         * - **Pseudo-polynomial trap.** A loop bounded by an input *value* (e.g. `min..2*max`) is polynomial in the
         *   magnitude, not in the input size. Check the value constraints before you write one.
         *
         * ### Approach
         * 1. Build `cnt: height -> count` (Long keys, since sums reach 2·10^9 > `Int.MAX_VALUE`).
         * 2. Let `xs` be the distinct heights. For each `i`:
         *    - `acc[2·xs[i]] += cnt[xs[i]] / 2` (self-pairs of equal planks);
         *    - for each `j > i`: `acc[xs[i] + xs[j]] += min(cnt[xs[i]], cnt[xs[j]])`.
         * 3. The answer is the max of `cnt[h] + acc[h]` over every key `h` in `cnt` or `acc`.
         *    Both maps must be scanned: the best height can be a plank height that no pair hits (e.g. `[1,1]` → 2),
         *    or a sum that is no plank's height (e.g. `[1,2,10,11]` → `H = 12`).
         *
         * ### Complexity
         * - **Time O(n + d²)**, where d ≤ n ≤ 1000 is the number of distinct heights: about 5·10^5 pair visits, each
         *   an O(1) hash update. The values' magnitude does not matter.
         * - **Space O(d²)** for `acc`, since there can be up to d(d+1)/2 distinct sums.
         *
         * ### Common pitfalls
         * - **Looping over heights `min..2*max`**: correct but 2·10^9 iterations at the stated constraints.
         * - **Int overflow** on `x + y` or `2·x` when heights are near 10^9. Use `Long`.
         * - **Double counting a pair** by visiting both `(x, y)` and `(y, x)`. Only take `j > i`.
         * - **Self-pairs**: equal heights pair among themselves, giving `cnt / 2`, not `min(cnt, cnt) = cnt`.
         * - **Forgetting standalone planks**: `cnt[H]` adds to `acc[H]`, and `H` may appear in only one of the maps.
         * - **Thinking you must use every plank** or that combining always helps. `[1,1]` → 2 beats one plank of 2.
         */
        fun referenceSolution(planks: IntArray): Int {
            val cnt = HashMap<Long, Int>()
            for (p in planks) cnt.merge(p.toLong(), 1, Int::plus)

            val xs = cnt.keys.toLongArray()
            val acc = HashMap<Long, Int>()
            for (i in xs.indices) {
                val x = xs[i]
                val cx = cnt.getValue(x)
                if (cx >= 2) acc.merge(2 * x, cx / 2, Int::plus)
                for (j in i + 1 until xs.size) {
                    val y = xs[j]
                    acc.merge(x + y, minOf(cx, cnt.getValue(y)), Int::plus)
                }
            }

            var best = 0
            for ((h, c) in cnt) best = maxOf(best, c + (acc[h] ?: 0))
            for ((h, a) in acc) best = maxOf(best, a + (cnt[h] ?: 0))
            return best
        }

        /**
         * ## Attempt analysis (leetcode-help)
         * You reached for **enumerate the target height + per-target pair counting with a frequency map**, and the
         * counting logic is right. It passes every case. The weak point is the candidate set, not a local bug:
         * `min..2*max` makes it O((2·max − min) · d), pseudo-polynomial, which will not finish for values near 10^9.
         * To get to optimal (O(d²)), there are two steps:
         * - **Minimal fix (≈O(d³), value-independent):** keep your counting body, but replace the range
         *   `(min..(max * 2))` with a set of real candidates: `index.keys` plus every `a + b` over distinct keys
         *   `a <= b`. (Include `a == b` only when `index[a] >= 2`, though including it anyway is harmless.)
         * - **Optimal:** turn the loops around as in `referenceSolution`. Loop over pairs once and add each pair's
         *   `min` into a map keyed by its sum. Then you never re-scan all keys for every candidate.
         *
         * ## Validation summary
         * Correct on all 9 cases (2 official + 7 added). Asymptotically it is a **pseudo-polynomial** scan, so it
         * would time out at the stated constraints (`planks[i]` up to 10^9). Details below.
         *
         * ## Approach (as written)
         * Fix a target height `H`. For that `H`, count how many planks you can make:
         * - planks already of height `H` (`index[H]`),
         * - for each value `l < H/2`, `min(cnt[l], cnt[H - l])` pairs (`l` can only ever pair with `H - l`),
         * - when `H` is even, `cnt[H/2] / 2` self-pairs (the `possibleH - l == l` branch).
         * Try every `H` in `min..2*max` and keep the best.
         *
         * ## Correctness notes
         * - **Why greedy matching is optimal here:** for a fixed `H`, the values split into independent groups
         *   `{l, H-l}`. A plank of height `l` can only pair with a plank of height `H - l`, so the groups never
         *   compete for the same planks. Each group's optimum is `min(cnt_l, cnt_{H-l})` (or `cnt/2` when
         *   `l == H-l`), and the total is just their sum. There is no global matching problem to solve.
         * - Planks of height exactly `H` are counted once, via `index[H]`. They also stay in `localIndex`, but
         *   their partner `H - H = 0` never exists, so they add 0. This is harmless, though it is easy to miss when
         *   reading.
         * - Each pair is counted once: after `l` is processed, both `l` and `H-l` are decremented. When the loop
         *   later reaches `H-l`, the smaller side is already 0, so `min(...)` adds 0.
         * - `computeIfPresent` inside `localIndex.forEach` only *replaces values*. That is not a structural change,
         *   so `LinkedHashMap` does not throw `ConcurrentModificationException`. It works, but it depends on a
         *   subtle collection rule. Computing `min` on read-only counts and visiting only `l < H - l` would avoid
         *   the mutation entirely.
         * - Keys are `Long` (`it.toLong()`), so `max * 2` (up to 2·10^9 > `Int.MAX_VALUE`) does not overflow.
         *   This is a good catch.
         * - The candidate range `min..2*max` is complete: every useful height is a plank value (>= min) or a sum
         *   of two (<= 2*max).
         *
         * ## Time complexity: O((2·max − min) · d), where d is the number of distinct heights (d <= n <= 1000)
         * - The outer `(min..(max * 2)).forEach` runs once per *integer* in the value range, not once per
         *   plank. Its cost depends on the size of the values, not the input length (pseudo-polynomial).
         * - Each iteration does `filterKeys` (O(d), plus a new map allocation) and `localIndex.forEach` with
         *   O(1) hash lookups (O(d)).
         * - Worst case `[1, 1000000000]`: about 2·10^9 iterations, each allocating a map. That never finishes in
         *   practice. This is why the added "large values" case stops at 10^5. A 10^9 case would hang the
         *   suite. Almost all of those heights are dead: no plank and no pair of planks can reach them.
         *
         * ## Space complexity: O(d) auxiliary
         * - `index` holds d entries. `localIndex` is an O(d) copy, rebuilt and thrown away on *every* iteration,
         *   so peak memory is O(d) but the allocation churn (and GC pressure) is proportional to the value range.
         *
         * ## Pattern
         * **Enumerate the answer + per-candidate counting with a frequency map** ("fix the target, then it's a
         * two-sum / pair-count problem"). The key step to carry to other problems is **shrinking the candidate
         * set**. Only values that can actually occur are worth trying: here, the plank heights and the pairwise
         * sums.
         *
         * ## Alternatives
         * 1. **Only try reachable heights.** Candidates = distinct heights ∪ distinct pairwise sums, which is
         *    O(d²) values. Evaluating each with your same O(d) counting gives O(d³), about 10^9 in the worst case.
         *    That is correct and value-independent, but still heavy.
         * 2. **Invert the loops: enumerate pairs, accumulate by sum** (the expected solution). For every
         *    distinct `x < y` add `min(cnt[x], cnt[y])` to `acc[x + y]`, and for every `x` add `cnt[x] / 2` to
         *    `acc[2x]`. The answer is `max over h of cnt[h] + acc[h]`. This takes O(d²) time (about 5·10^5 pair
         *    visits) and O(d²) space for the `acc` map. It works because of the independence argument above:
         *    each `{x, y}` group contributes to exactly one height, so the totals can simply be added up.
         * 3. **Sorted array + two pointers per candidate** replaces the hash lookups with an O(d) sweep per `H`.
         *    It has the same asymptotics as (1) but better cache behaviour. It is only worth it if hashing
         *    dominates.
         * With up to Θ(d²) distinct sums, any of which could be the answer, O(d²) is the natural target. (2)
         * meets it.
         *
         * ## Parallelism
         * The candidate heights are fully independent, so the outer loop is embarrassingly parallel. You could
         * split the `H` range across threads and take a max-reduce, or in (2) give each thread a slice of `x`
         * with its own `acc` map and merge at the end. But (2) is only ~5·10^5 cheap operations, which a single
         * thread finishes in about a millisecond. Thread start-up and the map merge would cost more than they
         * save. Parallelizing *your* version would just spread a wasteful 2·10^9-step scan across cores. Fixing
         * the algorithm is what wins here, not adding threads.
         *
         * ## Real-world
         * "Combine at most two items to hit an exact size" comes up in lot/packet consolidation (pairing partial
         * pallets or stock remnants into standard units), cutting/joining material to a standard length, and
         * order matching (pairing a buy with a sell so the quantities sum to a lot size). In those settings the
         * values are prices or lengths with huge ranges, so iterating over the *value domain* is never viable.
         * You iterate over the *data* (pairs, or a sorted sweep), and with streaming input you maintain a count
         * map and update the per-target totals incrementally. The broader lesson: when the loop bound is a value
         * from the input rather than its length, check the constraints before trusting it.
         */
        fun maximumWidth(planks: IntArray): Int {
            val index = planks.asIterable().groupingBy { it.toLong() }.eachCount()
            val min = index.keys.min()
            val max = index.keys.max()


            var best = 0
            (min..(max * 2)).forEach { possibleH ->
                var currentLength = index.getOrDefault(possibleH, 0)
                val localIndex = index.filterKeys { it <= possibleH }.toMutableMap()
                localIndex.forEach { (l, cnt) ->
                    if (cnt == 0) return@forEach
                    val addition = if (possibleH - l == l) {
                        val toAdd = cnt / 2
                        localIndex.computeIfPresent(l) { k, v -> 0 }
                        toAdd
                    } else {
                        val matchCnt = localIndex.getOrDefault(possibleH - l, 0)
                        val toAdd = minOf(cnt, matchCnt)
                        localIndex.computeIfPresent(l) { k, v -> v - toAdd }
                        localIndex.computeIfPresent(possibleH - l) { k, v -> v - toAdd }
                        toAdd
                    }
                    currentLength += addition
                }
                best = maxOf(currentLength, best)
            }


            return best
        }

    }
}
