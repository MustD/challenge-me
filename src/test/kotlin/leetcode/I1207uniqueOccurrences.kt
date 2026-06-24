package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 1207. Unique Number of Occurrences  (https://leetcode.com/problems/unique-number-of-occurrences/)
 *
 * Given an array of integers `arr`, return `true` if the number of occurrences of each value in the
 * array is unique, and `false` otherwise. That is: count how many times each distinct value appears,
 * then check whether all of those occurrence counts are themselves distinct.
 *
 * Constraints:
 * - 1 <= arr.length <= 1000
 * - -1000 <= arr[i] <= 1000
 */
typealias I1207 = (IntArray) -> Boolean

class I1207uniqueOccurrences {

    @Nested
    inner class Solution : ProblemTest<I1207> {

        override val cases = testCases<I1207>(
            "[1,2,2,1,1,3]" expects true,                 // counts: {1->3, 2->2, 3->1} all unique
            "[1,2]" expects false,                        // counts: {1->1, 2->1} collide
            "[-3,0,1,-3,1,1,1,-3,10,0]" expects true,
            "[1]" expects true,                           // single element edge case
        )

        @Test
        fun test() = check(::uniqueOccurrences)

        /**
         * Pattern: frequency-count + distinctness check via a hash map. Two passes:
         * (1) tally each value's count into a map; (2) test whether all counts are themselves distinct.
         *
         * The distinctness test here is done cleverly without a second map. After counting:
         *   - `map.keys.size`        = number of distinct VALUES        (= number of counts produced)
         *   - `map.values.toSet().size` = number of distinct COUNTS
         * If every count is unique, deduping the counts removes nothing, so the two sizes match.
         * Any collision (two values sharing a count) shrinks the value-set, making them differ.
         * This is equivalent to the textbook `counts.size == counts.toSet().size`, just phrased via
         * the map's own key-count as the "before dedup" number.
         *
         * Time:  O(n). One pass to build the map (n insert/lookups, each O(1) amortized), plus
         *        `values.toSet()` which is O(k) where k = distinct values (k <= n). Total O(n).
         * Space: O(k) auxiliary for the map, plus O(k) for the temporary count-set. With the tight
         *        domain here (-1000..1000 → at most 2001 keys, n <= 1000) this is effectively bounded.
         *
         * Correctness / edge cases:
         *   - Single element `[1]` → one count of 1, trivially unique → true. Handled.
         *   - Negative values and 0 are fine — they're just map keys, no sign assumptions.
         *   - No empty-array case (constraint: length >= 1), but it would also return true safely
         *     (0 == 0).
         *   - No integer-overflow risk: max count is 1000, well within Int.
         *
         * Alternative approaches:
         *   - Bounded-array counting: since values fit in [-1000,1000], use an IntArray(2001) instead
         *     of a HashMap to count, then a Boolean "seen counts" array indexed by count (counts are
         *     in 1..1000). Same O(n) time, O(1) extra space (fixed-size arrays), and avoids hashing
         *     overhead — the constant-factor win when the value domain is small and known. This is
         *     the production-grade trick: trade generality for a dense array when the key space is tight.
         *   - The map-of-counts is already asymptotically optimal: every element must be inspected at
         *     least once, so O(n) is a hard lower bound. No better Big-O exists.
         *
         * Parallelism: not worth it. n <= 1000 is far below the threshold where thread-spawn or
         * fork/join overhead pays off. In principle the count phase is a parallel histogram
         * (map-reduce: partition the array, count locally, merge maps) and the distinctness check is a
         * trivial reduce, but the merge contention and overhead dominate at this size. Parallel
         * histograms only earn their keep on very large arrays or as part of a larger pipeline
         * (e.g. distributed word-count / GROUP BY ... COUNT across shards).
         *
         * Real-world: this is exactly a GROUP BY value, then "are the COUNTs distinct?" — the kind of
         * cardinality/frequency analysis databases and analytics engines do constantly. At scale the
         * counts come from approximate sketches (HyperLogLog, count-min) over streaming/unbounded data
         * rather than an exact in-memory map, trading a little accuracy for bounded memory.
         */
        fun uniqueOccurrences(arr: IntArray): Boolean {
            val map = mutableMapOf<Int, Int>()

            arr.forEach { num ->
                val value = map[num] ?: 0
                map[num] = value + 1
            }

            val occurrence = map.keys.size
            val frequency = map.values.toSet().size

            return occurrence == frequency
        }

    }
}
