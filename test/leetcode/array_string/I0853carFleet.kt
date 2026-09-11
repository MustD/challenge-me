package leetcode.array_string

import leetcode.expects
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test

/**
 * 853. Car Fleet  (https://leetcode.com/problems/car-fleet/)
 *
 * There are n cars going to the same destination along a one-lane road. The destination is
 * target miles away.
 *
 * You are given two integer arrays position and speed, both of length n, where position[i] is
 * the position of the ith car and speed[i] is the speed of the ith car (in miles per hour).
 *
 * A car can never pass another car ahead of it, but it can catch up to it and drive bumper to
 * bumper at the same speed. The faster car will slow down to match the slower car's speed. The
 * distance between these two cars is ignored (i.e., they are assumed to have the same position).
 *
 * A car fleet is some non-empty set of cars driving at the same position and same speed. Note
 * that a single car is also a car fleet.
 *
 * If a car catches up to a car fleet right at the destination point, it will still be considered
 * as one car fleet.
 *
 * Return the number of car fleets that will arrive at the destination.
 *
 * Constraints:
 * - n == position.length == speed.length
 * - 1 <= n <= 10^5
 * - 0 < target <= 10^6
 * - 1 <= speed[i] <= 10^6
 * - 0 <= position[i] < target
 * - All the values of position are unique.
 */
typealias I0853 = (Int, IntArray, IntArray) -> Int

class I0853carFleet {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0853> {

        override val cases = leetcode.testCases<I0853>(
            leetcode.args(10, "[1,4]", "[3,2]") expects 1,
            leetcode.args(10, "[4,1,0,7]", "[2,2,1,1]") expects 3,
            leetcode.args(10, "[0,1,2,3]", "[16,8,4,2]") expects 1,
            leetcode.args(12, "[10,8,0,5,3]", "[2,4,1,1,3]") expects 3,
            leetcode.args(10, "[3]", "[3]") expects 1,
            leetcode.args(100, "[0,2,4]", "[4,2,1]") expects 1

        )

        @Test
        fun test() = check(::carFleet, ::referenceSolution, ::referenceSolutionStack)

        // ## Assessment of the attempt
        //
        // Approach reached for: sort the cars by position (via a reverse-ordered `TreeMap`), then
        // sweep front-to-back keeping a running maximum of free-flow arrival time. That is the
        // optimal algorithm — same O(n log n) time and O(n) space as the reference below, and the
        // `if (current < time)` strict comparison correctly merges cars that tie at the destination.
        // No defect to fix; it passes every case.
        //
        // Two things keep it from being what you would ship, both constant-factor, not asymptotic:
        //   * The `TreeMap` is the only sorted container here that is never used *as* a map — no
        //     lookups, no range queries, just one ordered traversal. It pays for boxed `Integer`
        //     keys, boxed `Double` values and a red-black node per car; a sorted primitive array
        //     does the same work with sequential memory access. See `referenceSolution`.
        //   * Keying by position silently depends on the "all positions are unique" constraint.
        //     Two cars sharing a position would overwrite each other and undercount. An index sort
        //     has no such coupling.
        // Also note `result = 1` / `firstEntry()` pre-counts the front car and relies on n >= 1 —
        // correct under the stated constraints, but it would NPE on an empty input.

        /**
         * ## Pattern
         *
         * **Sort by position, then scan front-to-back keeping a running maximum of arrival time.**
         * This is the "monotonic / running-max sweep" family — the same shape as the classic
         * monotonic-stack solution, but since the question only asks for a *count*, a single
         * `max` variable replaces the stack.
         *
         * The key modelling insight: a car never actually needs to be simulated. Convert each car
         * into a single scalar — its **free-flow arrival time** `t = (target - position) / speed`,
         * i.e. how long it would take if nothing were in its way. Then walk the cars from the one
         * **closest to the target** backwards. A car behind merges into the fleet ahead iff its
         * own `t` is `<=` the fleet leader's `t` (it would arrive no later, so it must catch up).
         * If its `t` is strictly greater, it can never catch up and it *becomes* the new leader of
         * a new fleet. Hence: **the number of fleets == the number of strict increases in `t` when
         * scanned in decreasing position order.**
         *
         * Note the fleet leader is always the *slowest* car (largest `t`) seen so far, which is why
         * `current` is a running max and never resets — a car behind must clear not just the car
         * directly ahead, but the whole blockade ahead of it. That is exactly head-of-line blocking.
         *
         * ## Time — O(n log n)
         *
         * - The `position.indices.forEach` loop runs `n` times; each `timings[pos] = time` is a
         *   red-black-tree insert, O(log n). → **O(n log n)**, and this dominates.
         * - The second `timings.forEach` is an in-order traversal: O(n).
         * - Best == average == worst; a `TreeMap` gives no fast path for already-sorted input
         *   (unlike, say, Timsort on an array, which would be O(n) on pre-sorted data).
         *
         * ## Space — O(n) auxiliary
         *
         * The `TreeMap` holds `n` entries. Worth naming the constant factor: every key is a boxed
         * `Integer`, every value a boxed `Double`, and each `Entry` is a separate node with two
         * child pointers, a parent pointer and a colour bit. That is roughly an order of magnitude
         * more memory traffic — and far worse cache locality — than an `n`-element primitive array
         * holding the same information. No recursion, so no stack depth to account for. Output is
         * a single `Int`, O(1).
         *
         * ## Correctness notes
         *
         * - **`result = 1` is correct only because `n >= 1`** is guaranteed by the constraints —
         *   it pre-counts the frontmost car, which always leads a fleet. On an empty input this
         *   would return 1 *and* `firstEntry()` would NPE. Fine here; a landmine if reused.
         * - **The `TreeMap` keyed by position is safe only because "all values of position are
         *   unique"**. Drop that constraint and two cars at the same position would collide, the
         *   second silently overwriting the first, and the count would come out too low. A sorted
         *   index array has no such failure mode — this is the one real coupling to a problem
         *   constraint in the code.
         * - **Ties merge, as required.** The comparison is `if (current < time)` — strictly
         *   greater-than starts a new fleet, so equal arrival times stay one fleet. That is the
         *   statement's "catches up right at the destination still counts as one fleet" rule. Using
         *   `<=` here would be the single most likely off-by-one bug in this problem.
         * - **Floating point is safe at these bounds, but it is a habit worth questioning.**
         *   `distance <= 10^6` and `speed >= 1`, so every quotient fits comfortably in a double's
         *   53-bit mantissa and comparisons between them are exact enough here. The general fix,
         *   when ranges are unknown, is to avoid division entirely and compare
         *   `d1 * s2 <= d2 * s1` using `Long` — cross-multiplication instead of ratios.
         * - `distance / spd.toDouble()` correctly forces floating-point division; `distance / spd`
         *   would be integer division and would wrongly merge cars with different real times.
         *
         * ## Alternatives
         *
         * 1. **Sorted index array + running max — same O(n log n), much smaller constants.**
         *    Build `IntArray(n) { it }`, sort it by `position`, then one pass computing the time
         *    inline. Identical asymptotics, no boxing, no tree nodes, sequential memory access.
         *    This is what you would actually ship. Their `TreeMap` pays a real constant-factor
         *    price for the convenience of "sorted container" semantics it doesn't otherwise use
         *    (no lookups, no range queries, no incremental updates — just one ordered traversal).
         * 2. **Monotonic stack (the textbook answer).** Push arrival times while scanning from the
         *    back; pop whenever the incoming time dominates. Same complexity; the stack's *size*
         *    is the answer. Strictly more machinery than needed when only the count is wanted —
         *    the running max is the better simplification, and their version is already there.
         * 3. **Counting sort / bucket by position — O(n + target) time, O(target) space.** Since
         *    positions are unique integers in `[0, target)` with `target <= 10^6`, you can place
         *    each car directly into `bucket[position]` and sweep the range downward, skipping
         *    empties. This beats O(n log n) when `n` is large relative to `target` (at `n = 10^5`
         *    and `target = 10^6` it is a wash; at `n == target` it wins). It is the only way to
         *    break the comparison-sort barrier here, and it works *only* because of the bounded
         *    integer coordinate range.
         * 4. **If the input arrived already sorted by position, the whole problem is O(n) and
         *    online** — a single streaming pass with one `max` variable, O(1) space beyond input.
         *    The sort is the entire cost; the algorithm itself is trivial.
         *
         * ## Parallelism
         *
         * Honestly: **not worth it at n <= 10^5** — the whole thing runs in well under a
         * millisecond, which is below the cost of spinning up a thread pool. But the *structure*
         * is unusually parallel-friendly, and that is the interesting part:
         *
         * - Computing the `n` arrival times is an **embarrassingly parallel map** — `n` independent
         *   divisions with no data dependencies. It auto-vectorises to SIMD (a divide per lane) and
         *   would split across threads trivially.
         * - The sort is a **parallel merge sort** (`java.util.Arrays.parallelSort`), the standard
         *   divide-and-conquer speedup. This is where the real time goes, so it is the only part
         *   worth parallelising at scale.
         * - The final running-max scan *looks* strictly sequential, but `max` is **associative**,
         *   so it is a **prefix scan (max-scan)** — computable in O(log n) depth with a
         *   Hillis-Steele or Blelloch scan on a GPU/SIMD machine. Then the answer is a parallel
         *   count of positions where `scan[i] > scan[i-1]`. This is the teaching point worth
         *   keeping: *a loop carrying a running fold is not automatically sequential — if the
         *   operator is associative, it is a scan, and scans parallelise.* Contrast with a loop
         *   whose next step needs the previous step's branch decision, which genuinely does not.
         * - Amdahl's ceiling: the sort is ~90% of the runtime, so parallelising only the map/scan
         *   buys almost nothing. You would need `n` in the tens of millions before any of this
         *   beats the single-threaded primitive-array version.
         *
         * ## Real-world
         *
         * This is the **head-of-line blocking** computation, and it shows up constantly:
         *
         * - **In-order commit/retirement.** A CPU's reorder buffer, a database's replication
         *   apply-log, or a Kafka consumer with ordering guarantees: work finishes out of order
         *   but *retires* in order, so the observed completion time of item `i` is
         *   `max(own_time, all earlier items' times)` — literally the running max in this code.
         *   Computing "when does each record actually become visible" is this exact sweep.
         * - **TCP / HTTP-2 head-of-line blocking**, and why HTTP-3 moved to QUIC: one slow stream
         *   ahead stalls everything behind it, and the fix is to remove the ordering constraint
         *   rather than to speed up the slow car.
         * - **Traffic and logistics simulation** — convoy formation, truck platooning ETA, and
         *   airport arrival-slot sequencing all reduce to "who catches whom before the gate".
         * - How the constraints differ in production: the data usually **arrives already ordered**
         *   (by position, offset, or sequence number), so you skip the sort and run the O(n)
         *   streaming version — and it becomes an online algorithm over an unbounded stream with
         *   O(1) state. You would also not build a `TreeMap`: at scale the cache misses from
         *   pointer-chasing a tree of boxed objects cost more than the entire arithmetic, which is
         *   the usual production lesson — the flat-array solution with identical Big-O wins by a
         *   large constant factor, and constants are what you actually pay.
         */
        fun carFleet(target: Int, position: IntArray, speed: IntArray): Int {
            val timings = TreeMap<Int, Double>(reverseOrder<Int>())
            position.indices.forEach { idx ->  // n
                val pos = position[idx]
                val spd = speed[idx]

                val distance = target - pos
                val time = distance / spd.toDouble()
                timings[pos] = time // *log(n)
            }

            var result = 1
            var current = timings.firstEntry().value
            timings.forEach { (_, time) -> // n
                if (current < time) {
                    result++
                    current = time
                }
            }

            return result
        }


        /**
         * ## Restatement
         *
         * `n` cars travel one lane toward a shared finish line at `target`. Each car has a start
         * position and a constant speed; a car may never overtake, so on catching a slower car it
         * glues to it and adopts its speed. Cars driving glued together are one *fleet* (a lone car
         * counts as a fleet, and catching up exactly at the finish line still counts as merging).
         * Return how many fleets cross the line.
         *
         * ## Pattern — sort + running-max sweep (the count-only form of a monotonic stack)
         *
         * Simulation is a trap: cars change speed, merge, and re-merge, and chasing that forward in
         * time is both fiddly and slow. The pattern to reach for whenever "who blocks whom" is
         * ordered along a line is: **reduce each element to one comparable scalar, sort by
         * position, then sweep while folding a running extremum.**
         *
         * ### Intuition
         *
         * Give every car its *free-flow arrival time* — how long it would take if the road were
         * empty: `t = (target - position) / speed`. Now walk the cars from the one nearest the
         * finish line backwards. A car behind catches the fleet ahead **iff its own `t` is no
         * greater than the fleet's `t`** — arriving no later than something you cannot pass means
         * you must have run into it. If its `t` is strictly larger, it is too slow to ever close
         * the gap, so it starts a fleet of its own.
         *
         * So the answer is just *the number of strict increases in `t` scanned in decreasing
         * position order*. Crucially the running value is a **max that never resets**: a car must
         * clear the slowest car anywhere ahead of it, not merely the car directly in front —
         * everything between them is already stuck behind that slowpoke too.
         *
         * ### Core concepts
         *
         * - **Free-flow time reduction** — collapsing an object with several attributes (position,
         *   speed) into a single ordered scalar that answers the question. Once each car is one
         *   number, the problem becomes a scan over numbers. Most "merge / block / interval" puzzles
         *   have such a reduction, and finding it *is* the problem.
         * - **Head-of-line blocking** — in a strictly ordered queue, an item's effective completion
         *   time is `max(its own time, every earlier item's time)`. This is why the fold is a max
         *   and never resets.
         * - **Running fold (prefix max)** — a loop carrying `acc = max(acc, x)`. It is the
         *   stack-free degenerate case of a monotonic stack: keep the stack only when you need the
         *   *members*, keep one variable when you need the *count*.
         * - **Monotonic stack** — the general tool: a stack kept sorted, where each push pops
         *   everything it dominates. Its final size is the fleet count. Worth recognising, but
         *   strictly more machinery than this problem needs.
         * - **Strict vs. non-strict comparison as a tie policy** — `>` merges ties, `>=` splits
         *   them. Which one you write *is* the "catches up exactly at the destination" rule.
         *
         * ## Approach
         *
         * 1. Sort the car indices by position. Here positions are packed into the high 32 bits of a
         *    `Long` with the index in the low bits, so one primitive `LongArray.sort()` orders the
         *    cars with no boxing and no comparator.
         * 2. Iterate from the largest position down to the smallest.
         * 3. For each car compute `t = (target - position) / speed` in floating point.
         * 4. If `t > slowest`, this car cannot catch anything ahead: increment the fleet count and
         *    set `slowest = t`. Otherwise it merges into the current fleet and is ignored.
         * 5. Return the count.
         *
         * ## Complexity
         *
         * - **Time O(n log n)** — dominated entirely by the sort; the sweep is a single O(n) pass
         *   with O(1) work per car. (The comparison-sort barrier can be broken here only by bucket
         *   sorting on position, which is legal because positions are unique integers in
         *   `[0, target)` with `target <= 10^6`: that gives O(n + target).)
         * - **Space O(n)** — the packed array of `n` longs; the sweep itself needs one counter and
         *   one double. No recursion. If the input were already sorted by position, the algorithm
         *   would be a one-pass O(n) time, O(1) space *streaming* computation.
         *
         * ## Common pitfalls
         *
         * - **Integer division.** `(target - position) / speed` on two `Int`s truncates and wrongly
         *   merges cars with different real arrival times. Force a `Double` (or avoid division
         *   entirely — see below).
         * - **Wrong tie comparison.** `>=` instead of `>` splits cars that meet exactly at the
         *   destination into separate fleets, contradicting the statement. This is the classic
         *   off-by-one of this problem.
         * - **Scanning the wrong direction.** Front-to-back (descending position) is what makes the
         *   running max meaningful; ascending order needs a stack instead.
         * - **Resetting the running max**, or comparing only against the immediately preceding car.
         *   The blockade is cumulative.
         * - **Forgetting to sort at all** — the input order of `position` is arbitrary.
         * - **Floating point in general.** Safe at these bounds (`distance <= 10^6`, `speed >= 1`,
         *   so every quotient is exact enough in a 53-bit mantissa), but the range-independent fix
         *   is cross-multiplication in `Long`: compare `d1 * s2 <= d2 * s1` instead of `d1/s1` vs
         *   `d2/s2`.
         * - **Assuming `n >= 1`** when pre-seeding the count at 1. Starting at 0 and letting the
         *   first car trigger the increment handles the empty case for free.
         */
        fun referenceSolution(target: Int, position: IntArray, speed: IntArray): Int {
            val n = position.size

            // Pack (position, index) into one Long so a primitive sort orders the cars by position
            // with no boxing and no comparator. Positions are non-negative, so the high 32 bits
            // sort exactly as the positions do.
            val byPosition = LongArray(n) { i -> (position[i].toLong() shl 32) or i.toLong() }
            byPosition.sort()

            var fleets = 0
            var slowest = 0.0 // running max of free-flow arrival times seen ahead

            for (k in n - 1 downTo 0) { // walk from the car closest to the target backwards
                val i = (byPosition[k] and 0xFFFFFFFFL).toInt()
                val time = (target - position[i]).toDouble() / speed[i]

                // Strictly slower than everything ahead => can never catch up => new fleet leader.
                // Equal times merge, which is the "catches up right at the destination" rule.
                if (time > slowest) {
                    fleets++
                    slowest = time
                }
            }

            return fleets
        }

        /**
         * ## The same problem, solved with an explicit monotonic stack
         *
         * The sweep above keeps a single running max because the question only asks *how many*
         * fleets there are. A monotonic stack is the general form of that idea: it keeps the fleet
         * leaders themselves, not just their count, and it is the tool to reach for the moment the
         * problem asks for anything more than a number (which car leads which fleet, the fleet
         * sizes, each car's actual arrival time).
         *
         * ### Intuition
         *
         * Walk the cars from the one nearest the finish line backwards, pushing each car's
         * free-flow arrival time `t = (target - position) / speed` onto a stack. The stack is
         * maintained **strictly increasing from bottom to top**: the bottom is the frontmost fleet
         * leader, each element above it is a strictly slower car further back that can never catch
         * what is in front of it.
         *
         * That invariant is restored by one rule: after pushing `t`, if `t <= ` the element beneath
         * it, this car arrives no later than the fleet immediately ahead — so it must run into it —
         * and it is popped, absorbed into that fleet. Because the stack was already increasing, one
         * comparison suffices; there is no loop of pops here. (In the general monotonic-stack
         * template the pop is a `while`; this problem's invariant collapses it to an `if`, which is
         * exactly why the stack can be replaced by a single variable.)
         *
         * When the scan ends, every surviving element is one fleet, so `stack.size` is the answer —
         * and, unlike the running-max version, the stack still holds *which* cars lead them.
         *
         * ### Core concepts
         *
         * - **Monotonic stack** — a stack deliberately kept sorted; every push pops whatever it
         *   dominates. The pop *is* the merge/absorb event, which is what makes the structure worth
         *   using: the pops are the answer, not the bookkeeping.
         * - **Stack invariant** — "times strictly increase from bottom to top" holds before and
         *   after every step. Proving the invariant is how you convince yourself one comparison
         *   replaces a rescan of everything ahead.
         * - **Amortised O(1) per element** — each car is pushed once and popped at most once, so
         *   the scan is O(n) total even when written with a `while`.
         * - **Count vs. members** — a fold over an associative operator (`max`) needs one variable;
         *   you only pay for a stack when you need the elements it holds.
         *
         * ## Complexity
         *
         * - **Time O(n log n)** — the sort dominates; the stack scan is O(n) amortised.
         * - **Space O(n)** — the packed sort array plus a stack that can hold all `n` cars in the
         *   worst case (strictly decreasing speeds front to back, where nothing ever merges).
         *   Strictly more than the running-max version's O(1) sweep state.
         *
         * ## Common pitfalls
         *
         * - **Popping on `<` instead of `<=`.** Equal arrival times must merge ("catches up right
         *   at the destination is still one fleet"), so the absorb condition is non-strict here
         *   even though the new-fleet condition in the running-max version is strict — the two are
         *   complements, and flipping one without the other is the classic bug.
         * - **Pushing before checking, or checking before pushing** — either order works, but mixing
         *   them (comparing against the top *after* pushing the same element) makes every car
         *   absorb itself.
         * - **Reporting the pop count rather than the stack size.** The answer is what survives.
         */
        fun referenceSolutionStack(target: Int, position: IntArray, speed: IntArray): Int {
            val n = position.size

            // Same packed (position, index) trick: a primitive sort by position, no boxing.
            val byPosition = LongArray(n) { i -> (position[i].toLong() shl 32) or i.toLong() }
            byPosition.sort()

            // Fleet leaders' arrival times, strictly increasing from bottom to top.
            val stack = DoubleArray(n)
            var top = 0

            for (k in n - 1 downTo 0) { // from the car closest to the target, backwards
                val i = (byPosition[k] and 0xFFFFFFFFL).toInt()
                val time = (target - position[i]).toDouble() / speed[i]

                stack[top++] = time
                // Arrives no later than the fleet directly ahead => catches it => absorbed.
                // `<=` (not `<`) so a car meeting the fleet exactly at the destination still merges.
                if (top > 1 && stack[top - 1] <= stack[top - 2]) top--
            }

            return top
        }

    }
}
