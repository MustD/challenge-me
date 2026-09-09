package leetcode.array_string

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 763. Partition Labels  (https://leetcode.com/problems/partition-labels/)
 *
 * You are given a string s. We want to partition the string into as many parts as possible so
 * that each letter appears in at most one part.
 *
 * Note that the partition is done so that after concatenating all the parts in order, the
 * resultant string should be s.
 *
 * Return a list of integers representing the size of these parts.
 *
 * Constraints:
 * - 1 <= s.length <= 500
 * - s consists of lowercase English letters.
 */
typealias I0763 = (String) -> List<Int>

class I0763partitionLabels {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0763> {

        override val cases = leetcode.testCases<I0763>(
            "ababcbacadefegdehijhklij" expects "[9,7,8]",
            "eccbbbbdec" expects "[10]",
            "abcd" expects "[1, 1, 1, 1]",
            "a" expects "[1]",
            "abbbbbbbbbbbbbbbbba" expects "[19]",
        )

        @Test
        fun test() = check(::partitionLabels)

        /**
         * ## Pattern
         * Greedy "last occurrence" scan — the same idea behind merge-intervals: every character
         * implicitly defines an interval `[firstIndex, lastIndex]`, and a partition boundary can
         * only be cut once the running window has swallowed every interval it has touched so far.
         * This is a single linear pass with no backtracking, because the *last* occurrence of a
         * char is known up front (first pass), so the window's right edge only ever grows.
         *
         * ## How it works
         * 1. First pass builds `maxIdxes`: last index of each letter in `s` — O(n).
         * 2. Second pass extends `currentMaxIdx` to the farthest last-occurrence seen among the
         *    letters visited so far in the current partition. When the loop's own `index` catches
         *    up to `currentMaxIdx`, every letter in the current window is guaranteed to have no
         *    further occurrence beyond this point, so the partition is closed here — this is the
         *    greedy-correctness argument: closing as early as this condition allows can never be
         *    wrong, because closing later only makes the part bigger without necessity, and closing
         *    earlier is impossible (some letter in the window still recurs past `index`).
         *
         * ## Complexity
         * - Time: O(n) — two linear passes over `s` (`forEachIndexed` twice), each O(1) work per
         *   char (map read/write). `maxOf` and map lookups are O(1) amortized (26-letter alphabet).
         * - Space: O(1) auxiliary beyond output — `maxIdxes` holds at most 26 entries (lowercase
         *   English letters), so it's bounded, not O(n). `result` is the required output.
         *
         * ## Correctness notes
         * - `getOrDefault(ch, 0)` is a defensive default but unreachable in practice: `maxIdxes` is
         *   populated from every character of the same `s` being iterated, so every `ch` is always a
         *   key by the time the second pass reads it.
         * - Single-character strings and strings where every char is unique both fall out naturally:
         *   `currentMaxIdx` immediately equals `index` each step, giving all-1 partitions.
         * - Order-sensitive `expects` in the test cases is fine here — partition sizes have one
         *   correct order (left to right along `s`), unlike problems that accept "any order".
         *
         * ## Alternatives
         * - Same asymptotic complexity, different bookkeeping: track `start` of the current
         *   partition explicitly and emit `index - start + 1` instead of an incrementing counter —
         *   equivalent, marginally more direct since it avoids a separate `currentStreak++`.
         * - A true interval-merging solution (collect `[first, last]` per letter, sort by start,
         *   merge overlapping intervals) generalizes to arbitrary alphabets but costs O(n log n) for
         *   the sort and is strictly worse here — this problem's fixed small alphabet is exactly why
         *   the one-pass greedy beats general interval merging.
         * - This is already asymptotically optimal: O(n) is a lower bound since every character of
         *   `s` must be inspected at least once.
         *
         * ## Parallelism
         * Not worth it. The first pass (building `maxIdxes`) is embarrassingly parallel in
         * principle (independent per-char max), but at n <= 500 the fork/join or thread-pool
         * overhead dwarfs the O(n) work it would save. The second pass is inherently sequential: each
         * step's decision (`currentMaxIdx`, close-or-continue) depends on the running max carried
         * from every prior step in the same partition — a true data dependency, not just an
         * implementation choice. Only a genuinely huge alphabet-parallel variant (e.g. computing
         * last-occurrence maps for many independent strings concurrently) would be a sane place to
         * introduce concurrency, and that's parallelism across inputs, not within this one.
         *
         * ## Real-world angle
         * The "extend the boundary to the farthest dependency, then cut" shape recurs in log
         * compaction/checkpointing (flush once every record referencing older data has been
         * consumed), interval scheduling, and JVM/GC region compaction (can't reclaim a region until
         * every live reference into it has been relocated). The production twist is usually that the
         * "last occurrence" isn't known in advance — here it's precomputed in one pass because the
         * whole string is in memory; for a stream you'd need either a reverse pass first or an
         * eviction-based structure (e.g. LRU-style) instead of a direct index map.
         */
        fun partitionLabels(s: String): List<Int> {
            val maxIdxes = mutableMapOf<Char, Int>()
            s.forEachIndexed { index, ch -> maxIdxes[ch] = index }

            val result = ArrayList<Int>()
            var currentStreak = 1
            var currentMaxIdx = 0

            s.forEachIndexed { index, ch ->
                currentMaxIdx = maxOf(currentMaxIdx, maxIdxes.getOrDefault(ch, 0))
                if (currentMaxIdx == index) { //reach for end of current part
                    result.addLast(currentStreak)
                    currentStreak = 1
                } else { //still need to count
                    currentStreak++
                }
            }
            return result
        }

    }
}
