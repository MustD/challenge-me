package leetcode.array_string

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4038. Count Integers Appearing in a Single Block  (https://leetcode.com/problems/count-integers-appearing-in-a-single-block/)
 *
 * You are given an integer array nums.
 *
 * An integer x is special if all occurrences of x in nums appear in a single contiguous block.
 *
 * Return the number of distinct special integers in nums.
 *
 * Constraints:
 * - 1 <= nums.length <= 100
 * - 1 <= nums[i] <= 100
 */
typealias I4038 = (IntArray) -> Int

class I4038countSpecialIntegers {

    @Nested
    inner class Solution : leetcode.ProblemTest<I4038> {

        override val cases = leetcode.testCases<I4038>(
            "[1,2,2,1]" expects 1,
            "[3,3,1,2,2,1]" expects 2,
            "[1]" expects 1,                    // single element: one block of length 1 (n = 1 boundary)
            "[1,1,1,1]" expects 1,              // all equal: the only block ends at the array's end
            "[1,2,3,4]" expects 4,              // all distinct: every singleton is its own single block
            "[1,2,2]" expects 2,                // special block sits at the very last position
            "[1,1,7,1,2,2,3]" expects 3,        // 1 split into two blocks; 7 and 3 are singletons
            "[5,5,1,5,5]" expects 1,            // same value at both ends (split); middle singleton special
            "[2,1,2,1,2]" expects 0,            // alternating: no value forms a single block
            "[1,2]" expects 2,                  // n = 2, loop runs once: both flushes (in-loop + tail) fire
            "[100,100,1]" expects 2,            // max value 100; block closed inside the loop, tail singleton
            "[1,2,3,1,2,3]" expects 0,          // every value has count 2 but streaks of 1 — all split
            """
            [1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13,14,14,15,15,16,16,17,17,18,18,19,19,20,20,
             21,21,22,22,23,23,24,24,25,25,26,26,27,27,28,28,29,29,30,30,31,31,32,32,33,33,34,34,35,35,36,36,37,37,
             38,38,39,39,40,40,41,41,42,42,43,43,44,44,45,45,46,46,47,47,48,48,49,49,1,1]
            """ expects 48,                     // n = 100 ceiling: pairs 2..49 special, 1 split across both ends
        )

        @Test
        fun test() = check(::countSpecialIntegers)

        /**
         * ## Analysis (validated — all 13 cases pass)
         *
         * **Idea.** A value is special ⇔ its *first* run (maximal block of equal neighbours) already contains
         * every occurrence. So: count totals up front, then walk the runs and compare each run's length with
         * the value's total. `streak == index[current]` can only be true for a value's first run (every run is
         * ≥ 1 and totals are the sum of all runs), so no value is ever counted twice — no `seen` set needed.
         *
         * **Pattern.** *Frequency map + run-length encoding (RLE) scan* — "precompute a global statistic, then
         * compare a local one against it". Same shape as "is this window/segment the whole population of x?".
         *
         * ### Time — O(n)
         * - `groupingBy { it }.eachCount()` — one pass, O(n) expected (hash map).
         * - `for (idx in 1..nums.lastIndex)` — one pass; each step is O(1) plus one `index[current]` lookup
         *   done only at run boundaries (≤ number of runs ≤ n).
         * - Best = average = worst: always exactly two linear passes.
         *
         * ### Space — O(d), d = distinct values (≤ min(n, 100))
         * - The `Map<Int, Int>` from `eachCount()` (a `LinkedHashMap` with boxed `Int`s). Everything else is O(1).
         *
         * ### Correctness notes
         * - **Tail flush** after the loop (`if (streak == index[current]) result++`) is the classic RLE pitfall —
         *   forgetting it drops the last run. You handled it; `[1,2,2]` and `[1,2]` probe it.
         * - `nums.first()` relies on `n ≥ 1` (guaranteed). For `n = 1` the range `1..0` is empty and only the
         *   tail flush runs — correct.
         * - `index[current]` returns `Int?`; comparing `Int == Int?` is fine and never null here, since every
         *   `current` came from `nums`.
         *
         * ### Alternatives
         * - **First/last index + count:** record `first[x]`, `last[x]`, `cnt[x]`; x is special iff
         *   `last[x] - first[x] + 1 == cnt[x]`. One pass, O(n)/O(d); no run tracking, no tail-flush edge case.
         * - **Count blocks instead of lengths:** increment `blocks[nums[i]]` whenever `i == 0 || nums[i] != nums[i-1]`,
         *   answer = number of values with `blocks == 1`. Single pass, arguably the most direct restatement of the
         *   definition — this is what the n = 100 expectation was checked with.
         * - **Array instead of map:** values are 1..100, so `IntArray(101)` replaces the boxed hash map — same
         *   Big-O, far smaller constant. Worth knowing as the "bounded key domain → counting array" reflex.
         * - Asymptotically optimal already: every element must be read (a single unseen element could split a
         *   block), so Ω(n).
         *
         * ### Parallelism
         * Not worth it at n ≤ 100 — thread startup dwarfs the work. In principle it is a clean map-reduce: split the
         * array into chunks, each chunk emits per-value (first, last, count) — or its run list plus boundary
         * elements — and the merge stitches runs that cross chunk borders. Associative merge → parallel prefix
         * friendly, but only pays off at millions of elements, and it is memory-bound (Amdahl ceiling ≈ memory
         * bandwidth, not cores).
         *
         * ### Real world
         * - RLE is everywhere: columnar storage (Parquet/ORC run-length pages), bitmap compression, image formats.
         *   "Is value x contained in a single run?" is exactly the question a columnar engine asks when a column is
         *   *clustered* by x — it decides whether a range scan can skip whole pages via min/max statistics.
         * - Log/event streams: "did this session's events arrive contiguously or interleaved?" — in a stream you
         *   can't precompute totals, so the first/last-index or blocks-count variant (single pass, mergeable)
         *   is the one that survives; the two-pass count-then-scan version needs the data twice.
         */
        fun countSpecialIntegers(nums: IntArray): Int {
            val index = nums.asIterable().groupingBy { it }.eachCount()
            var result = 0

            var current = nums.first()
            var streak = 1
            for (idx in 1..nums.lastIndex) {
                val num = nums[idx]

                if (num != current) {
                    if (streak == index[current]) result++
                    streak = 1
                    current = num
                } else {
                    streak++
                }
            }
            if (streak == index[current]) result++

            return result
        }

    }
}
