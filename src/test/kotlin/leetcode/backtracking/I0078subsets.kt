package leetcode.backtracking

import leetcode.ProblemTest
import leetcode.expectsAnyOrder
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 78. Subsets  (https://leetcode.com/problems/subsets/)
 *
 * Given an integer array `nums` of **unique** elements, return all possible subsets (the power set).
 * The solution set must not contain duplicate subsets. The subsets may be returned in any order.
 *
 * Constraints:
 * - 1 <= nums.length <= 10
 * - -10 <= nums[i] <= 10
 * - All the numbers of `nums` are unique.
 *
 * Notes:
 * - There are exactly 2^n subsets (each element is either in or out), so output size is 2^n and
 *   n <= 10 means at most 1024 subsets.
 * - The empty subset `[]` is part of the answer — don't forget it.
 */
typealias I0078 = (IntArray) -> List<List<Int>>

class I0078subsets {

    @Nested
    inner class Solution : ProblemTest<I0078> {

        // "Return the solution in any order" -> `expectsAnyOrder` (recursive multiset compare,
        // so neither the order of the subsets nor the order inside each subset has to match).
        override val cases = testCases<I0078>(
            "[1,2,3]" expectsAnyOrder "[[],[1],[2],[1,2],[3],[1,3],[2,3],[1,2,3]]",
            "[0]" expectsAnyOrder "[[],[0]]",
            "[1,2]" expectsAnyOrder "[[],[1],[2],[1,2]]",
            "[-1,0,1]" expectsAnyOrder "[[],[-1],[0],[-1,0],[1],[-1,1],[0,1],[-1,0,1]]",
        )

        @Test
        fun test() = check(::subsetsBin, ::subsets)

        /**
         * ## Analysis — `subsets` (recursive)
         *
         * **What this code actually does.** It is *permutation* backtracking with de-duplication bolted on, not subset
         * backtracking. Every recursive frame loops over **all** of `nums` and takes any element not currently in
         * `state`, so the search tree enumerates every ordered arrangement of every size: `[1,2]` and `[2,1]` are two
         * distinct paths. The two lines that rescue correctness are `state.toList().sorted()` (canonicalises each path
         * into one representative) and `result` being a `mutableSetOf<List<Int>>` (collapses the duplicates).
         *
         * **Pattern.** "Enumerate loosely, canonicalise, dedupe in a set." It is a legitimate and very general
         * technique — when a canonical form is cheap and the correct pruning is hard to see, it gets you a correct
         * answer fast. Its cost is that the *work* is proportional to the loose enumeration, not to the answer size.
         * The canonical subset pattern instead prunes structurally: pass a `start` index and let each frame consider
         * only `nums[start..]`, which makes every subset reachable by exactly one path and removes the need for both
         * the `sorted()` and the `Set`.
         *
         * **Time — `O(n! · n log n)`, i.e. ~`e·n!` recursion nodes.** The node count is the number of ordered
         * sequences of distinct elements, `Σ(k=0..n) n!/(n-k)! ≈ e·n!`. At each node the body does
         * `state.toList().sorted()` → `O(k log k)`, plus hashing that list into `result` → `O(k)`.
         *
         * Concretely at the constraint ceiling `n = 10`: **~9.86M** nodes and ~9.86M throw-away list allocations,
         * versus the **1024** subsets that actually exist — roughly a **9,600×** overshoot. It still finishes (the
         * tests are `n <= 3`), and `n <= 10` is exactly why this passes, but the gap between `e·n!` and `2^n` is the
         * whole lesson: `10! ≈ 3.6M` while `2^10 = 1024`, and by `n = 13` the loose version is ~10^10 nodes while the
         * pruned one is 8192.
         *
         * **Space.** Output `Θ(n · 2^n)` (unavoidable — it *is* the answer). Auxiliary: `state` is `O(n)`, recursion
         * depth is at most `n` so `O(n)` frames — genuinely cheap. The real memory cost is *transient*: ~`e·n!`
         * short-lived lists handed to the GC, which is allocation pressure rather than live heap.
         *
         * **Correctness notes.**
         * - `result.add(emptyList())` up front is required: `backtrack()` only records *after* an insert, so the empty
         *   state is never emitted by the loop. Good catch — that is the classic LC 78 off-by-one.
         * - `if (state.add(it))` cleverly doubles as the "not used yet" test and the insert. This is sound **only
         *   because the problem guarantees unique elements**. On LC 90 (*Subsets II*, duplicates allowed) a
         *   `Set<Int>` of *values* cannot distinguish "the other 2" from "the 2 I already used", and the enumeration
         *   would silently lose subsets like `[2,2]`.
         * - `state.remove(it)` after the recursive call is the undo half of backtracking — without it `state` would
         *   leak across siblings. Present and correct.
         * - `.sorted()` forces every subset into ascending value order, which is fine here ("any order") and is what
         *   makes the dedupe work. Note it is not order-preserving w.r.t. the input: for `nums = [3,1]` this emits
         *   `[1,3]`. If a variant demanded subsets in *index* order, the canonical form would have to be built from
         *   indices, not values.
         * - No overflow surface: values are in `[-10,10]` and nothing is summed.
         *
         * **The one-line fix that collapses `e·n!` → `2^n`** (do not add it as a second solution; just internalise the
         * shape): give `backtrack` a `start: Int`, loop `for (i in start until nums.size)`, and recurse with `i + 1`.
         * Each subset is then produced along exactly one path, so `result` can be a plain `MutableList` and the
         * `sorted()` disappears.
         *
         * **Parallelism.** Poor fit as written. `result` and `state` are shared mutable state, so the recursion is not
         * thread-safe at all; parallelising would mean fork/join over the top-level element choices with a *private*
         * result set per task, merged at the end. Even done properly the payoff is negative here: `n <= 10` means the
         * total work is microseconds, far below thread-pool dispatch overhead, and the workload is allocation-bound
         * (every task competes for the same allocator and GC) rather than compute-bound. See the note on `subsetsBin`
         * for the version that *does* parallelise cleanly.
         */
        fun subsets(nums: IntArray): List<List<Int>> {
            val result = mutableSetOf<List<Int>>()
            result.add(emptyList())
            val state = mutableSetOf<Int>()

            fun backtrack() {
                nums.forEach {
                    if (state.add(it)) {
                        result.add(state.toList().sorted())
                        backtrack()
                        state.remove(it)
                    }
                }
            }

            backtrack()
            return result.toList()
        }

        /**
         * ## Analysis — `subsetsBin` (bitmask / binary counter)
         *
         * **What this code actually does.** It drops recursion entirely and leans on the observation baked into the
         * problem: a subset *is* a length-`n` bit vector — element `i` is in or out. So counting `combination` from
         * `0` to `2^n - 1` enumerates every subset exactly once, in one pass, with no dedupe and no backtracking. The
         * inner loop asks "is bit `numIdx` set in `combination`?" via `combinationCheck == (combinationCheck and
         * combination)` and appends `nums[numIdx]` if so.
         *
         * **Pattern.** *Bitmask subset enumeration* (a.k.a. the binary-counter power set). This is the entry point to
         * the whole bitmask-DP family — once a subset is an `Int`, "subset of" is `a and b == a`, union is `or`,
         * complement is `inv()`, and cardinality is `Integer.bitCount`. Worth having in muscle memory; it recurs far
         * beyond LC 78.
         *
         * **Time — `Θ(n · 2^n)`, and this is optimal.** The outer `forEach` runs `2^n` times, the inner one exactly
         * `n` times, each iteration doing O(1) bit work. No hidden costs: no sorting, no hashing, no set membership.
         * At `n = 10` that is `1024 · 10 ≈ 10K` primitive steps — compare the ~9.86M nodes of the recursive
         * `subsets` above. The `Θ` is tight in both directions, and you cannot do asymptotically better, because the
         * *answer itself* contains `n · 2^(n-1)` integers (each element appears in half the subsets). Any correct
         * solution must at minimum write that many values, so `O(n · 2^n)` is a hard output-size lower bound. **This
         * solution is optimal.**
         *
         * **Space.** Output `Θ(n · 2^n)` — unavoidable, it is the answer. **Auxiliary space is `O(1)`**, which is the
         * standout property: no recursion stack, no `Set`, and each `state` list is not scratch space but goes
         * straight into `result`. Contrast the recursive version's ~`e·n!` throw-away lists; here allocation count is
         * exactly `2^n`, one per subset, with zero garbage.
         *
         * **Correctness notes.**
         * - The empty subset falls out for free at `combination == 0` (inner loop matches nothing, empty `state` is
         *   added). No special-case needed — precisely the off-by-one the recursive version had to patch with an
         *   explicit `result.add(emptyList())`. Encoding-driven correctness beats remembered edge cases.
         * - `state` is allocated *inside* the outer loop, so no state leaks between subsets. Hoisting it out and
         *   clearing it would be a bug here (the same list would be aliased into `result` `2^n` times).
         * - Uniqueness is structural: distinct masks give distinct subsets, so no dedupe is required. Note this also
         *   means the approach does **not** transfer unchanged to LC 90 (*Subsets II*) — with duplicate values,
         *   different masks can produce equal subsets and you would need sorting + a skip rule (or a set).
         * - Ordering: elements within each subset come out in *input index* order (inner loop ascends `numIdx`), and
         *   subsets come out in binary-counting order. For `[1,2,3]` that is exactly LC's sample output ordering.
         *   The harness uses `expectsAnyOrder` so it doesn't matter, but unlike the recursive version this one is
         *   index-order-preserving, not value-sorted — for `nums = [3,1]` it yields `[3,1]`, not `[1,3]`.
         * - **Overflow is the real trap here, hidden by the constraints.** `1 shl nums.size` is `Int` arithmetic, and
         *   the JVM masks the shift count to 5 bits: `1 shl 32` is `1`, not `2^32`. So at `n = 32` `total` becomes
         *   `1` and the loop silently emits only the empty subset; at `n = 31` it is `Int.MIN_VALUE` (negative) and
         *   the range is empty. `n <= 10` makes this unreachable, but the general lesson is that the bitmask power
         *   set caps out at `Long` (`1L shl n`, `n <= 62`) — and long before that, `2^n` makes it infeasible anyway.
         * - `combinationCheck == (combinationCheck and combination)` is correct; the more idiomatic tests are
         *   `combination and combinationCheck != 0` or `(combination shr numIdx) and 1 == 1`. Keep the parentheses
         *   either way — Kotlin's `and` is an infix *function*, so it binds looser than `==`, and dropping them
         *   changes the meaning (`a and b == 0` parses as `a and (b == 0)`).
         *
         * **Alternatives and how they trade off.**
         * - *Iterative cascading*: start with `[[]]`, and for each `num` append a copy of every existing subset with
         *   `num` added — same `Θ(n · 2^n)`, no bit tricks, and it scales past `n = 62` since it never builds a mask.
         *   Slightly worse constant (it copies lists repeatedly rather than building each once).
         * - *Include/exclude backtracking with a `start` index*: `Θ(n · 2^n)` time, `O(n)` stack. Same asymptotics as
         *   yours but with recursion overhead — its real advantage is that it generalises to problems where you must
         *   *prune* (combination sum, N-queens); a flat mask loop cannot prune.
         * - *Gray-code order*: iterate masks so consecutive subsets differ by exactly one element. Useless when you
         *   must materialise every subset (you still write `Θ(n · 2^n)` values), but a big win when you only need an
         *   aggregate per subset (sum, XOR, feasibility) — you update incrementally in `O(1)` per subset, dropping
         *   the whole enumeration to `Θ(2^n)`.
         * - *Lazy generation*: return a `Sequence<List<Int>>` yielding one subset per mask. Same total time, but
         *   `O(n)` live memory instead of `Θ(n · 2^n)` — the version you'd actually want if the caller filters and
         *   keeps only a few.
         *
         * **Parallelism — this one is the genuine article.** Unlike the recursive `subsets` (shared mutable `state`
         * and `result`, inherently sequential), this is an *embarrassingly parallel map*: `mask -> subset` is a pure
         * function with zero cross-iteration data dependency. The clean shape is to preallocate an array of size
         * `2^n` and have each worker own a disjoint mask range, writing to `result[mask]` — disjoint indices mean no
         * locks, no atomics, no merge step (`(0..<total).toList().parallelStream()` gets you most of the way, though
         * boxing the range costs more than the work). The honest caveat: at `n <= 10` the entire computation is a few
         * microseconds, well under thread-pool dispatch latency, so a parallel version is strictly slower. Even at
         * large `n` the ceiling is low — the loop is allocation- and memory-bandwidth-bound, not compute-bound, so
         * threads contend on the allocator and you saturate memory long before cores. SIMD is likewise a poor fit:
         * the bit test vectorises trivially, but building variable-length `List<Int>` objects does not. The real
         * speedup, if you needed one, is representing each subset as an `Int` mask instead of a `List<Int>` — that
         * turns the inner loop into a no-op and the whole answer into one contiguous `IntArray`.
         *
         * **Real-world experience.** Bitmask subset enumeration is production machinery, not a toy. It underpins
         * Held-Karp TSP and assignment/scheduling DPs; SQL query optimisers (System R and descendants) do DP over
         * subsets of relations to pick a join order — and that `2^n` is exactly why optimisers switch to greedy or
         * genetic search past ~12 tables; feature-subset selection and ablation grids enumerate the same way. The
         * companion trick you'll want next is *submask enumeration*: `var s = m; while (s > 0) { …; s = (s - 1) and m }`
         * walks every subset of a mask in `O(3^n)` total across all masks. Two practical notes from real systems:
         * (1) permission/flag sets are stored as bitmasks precisely because a whole set fits in one word and set
         * operations become single instructions, (2) nobody materialises a real power set — `n = 25` is already 33M
         * subsets, so you either stream them lazily, prune with a bound, or pick a different algorithm. Knowing the
         * enumeration is cheap per subset is what lets you recognise when the *count*, not the per-item cost, is the
         * thing that kills you.
         */
        fun subsetsBin(nums: IntArray): List<List<Int>> {
            val result = mutableListOf<List<Int>>()
            val total = 1 shl nums.size

            (0..<total).forEach { combination ->
                val state = mutableListOf<Int>()
                nums.indices.forEach { numIdx ->
                    val combinationCheck = 1 shl numIdx
                    if (combinationCheck == (combinationCheck and combination)) state.add(nums[numIdx])
                }
                result.add(state)
            }

            return result
        }
    }
}
