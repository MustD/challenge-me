package leetcode.array_string

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 4106. Find Mismatched Segments Between Two Strings  (https://leetcode.com/problems/find-mismatched-segments-between-two-strings/)
 *
 * You are given two strings s1 and s2 of the same length.
 *
 * A mismatched segment is a maximal contiguous range of indices where the characters of s1 and s2 differ at every
 * index.
 *
 * Return a 2D array containing [sub1, sub2] for each mismatched segment, where sub1 and sub2 are the corresponding
 * substrings from s1 and s2, respectively. Return the segments in increasing order of their starting indices.
 *
 * If there are no mismatched segments, return an empty array.
 *
 * Constraints:
 * - 1 <= s1.length == s2.length <= 100
 * - s1 and s2 consist of lowercase English letters.
 */
typealias I4106 = (String, String) -> List<List<String>>

class I4106findMismatchedSegments {

    @Nested
    inner class Solution : ProblemTest<I4106> {

        override val cases = testCases<I4106>(
            args("earth", "empty") expects """[["ar","mp"],["h","y"]]""",
            args("action", "nation") expects """[["ac","na"]]""",
            args("hot", "hot") expects "[]",
            args("a", "a") expects "[]", // minimum length, no mismatch
            args("a", "b") expects """[["a","b"]]""", // minimum length, segment closed only by the final flush
            args("abc", "xyz") expects """[["abc","xyz"]]""", // whole string is one segment
            args("abca", "xbcy") expects """[["a","x"],["a","y"]]""", // segments at the very first and last index
            args("xab", "xcd") expects """[["ab","cd"]]""", // leading match skipped, segment runs to the end
            args(
                "ababa",
                "aaaaa"
            ) expects """[["b","a"],["b","a"]]""", // identical segments are both kept, buffers reset
            args("abab", "baba") expects """[["abab","baba"]]""", // same letters swapped still differ at every index
            args("abxcd", "bazdc") expects """[["abxcd","bazdc"]]""", // no matching index, nothing splits the run
            args("aabbcc", "abcbcc") expects """[["ab","bc"]]""", // match run after the segment must not emit again
        )

        @Test
        fun test() = check(::findMismatchedSegments)

        /**
         * ## Analysis (lt-check) — verified: 3 official + 9 added cases
         *
         * **Pattern:** linear scan with run grouping (a two-state machine: "inside a segment" / "outside"). The same
         * shape solves run-length encoding, summary ranges and longest-streak problems.
         *
         * **Time: O(n).** One pass over the indices. `addLast` is amortized O(1) and `clear` is O(k) for a run of length
         * k. Each index enters a buffer at most once and is joined at most once, so all `joinToString` calls together
         * cost O(n). Best, average and worst case are all Θ(n), because every index must be compared.
         *
         * **Space: O(n) auxiliary, O(n) output.** The two buffers hold up to n elements each when the strings differ
         * everywhere (`"abc"` / `"xyz"`). The output holds at most 2n characters across at most ⌈n/2⌉ segments. No
         * recursion.
         *
         * **Optimal: O(n) time / O(1) space** (auxiliary, excluding the output). Time is optimal: any index left unread
         * could change the answer. Space is not: the buffers copy characters that already sit in `s1` and `s2`.
         *
         * **Correctness.** Invariant: before each index, the buffers hold exactly the currently open run of differing
         * positions. A differing index extends the run; an equal index closes it, so each emitted run is maximal. Runs
         * close left to right, which gives the required order without sorting. Two details carry the edge cases:
         * - The `isEmpty` guard stops consecutive equal indices from emitting empty segments (`"aabbcc"` / `"abcbcc"`).
         * - The flush after the loop catches a run that reaches the last index, the classic bug in this pattern
         *   (`"a"` / `"b"`).
         *
         * Checking only `accS1` is safe because both buffers always grow and clear together.
         *
         * **Alternative: remember a start index.** Keep `start = -1`; on the first differing index set `start = i`; on
         * an equal index (or at the end) emit `s1.substring(start, i)` and `s2.substring(start, i)`, then reset. Same
         * O(n) time, O(1) auxiliary space, and no per-character boxing in a `MutableList<Char>`. If you do want to
         * accumulate, `StringBuilder` is the idiomatic buffer. Nothing is asymptotically faster than O(n).
         *
         * **Parallelism:** not worthwhile. With n <= 100, thread startup costs far more than the scan. For huge inputs
         * you could split into chunks, scan each independently and merge runs that touch a chunk boundary. The compare
         * step parallelizes well, but building the output stays bounded by memory bandwidth and allocation.
         *
         * **Real-world use:** positional comparison of aligned data, such as fixed-width records, binary patching,
         * checksummed block ranges to re-sync, or reference-vs-read mismatches in genomics. It only works when the
         * inputs are aligned: a single insertion shifts everything, which is why text diff tools use LCS/Myers instead.
         * The same run grouping also coalesces adjacent dirty pages or consecutive log events.
         */
        fun findMismatchedSegments(s1: String, s2: String): List<List<String>> {
            // s1 aabbcc
            // s2 abcbcc
            // = ab, bc
            // s1 aabbbaaccc // s2 aacccaabbb // = bbb, ccc | ccc, bbb
            // s1 abc // s2 abc // = []
            // s1 "", s2"" = []
            val result = mutableListOf<List<String>>()
            val accS1 = mutableListOf<Char>()
            val accS2 = mutableListOf<Char>()

            s1.indices.forEach { idx ->
                val s1Char = s1[idx]
                val s2Char = s2[idx]
                if (s1Char == s2Char) {
                    if (accS1.isEmpty()) return@forEach
                    result.addLast(listOf(accS1.joinToString(""), accS2.joinToString("")))
                    accS1.clear()
                    accS2.clear()
                } else {
                    accS1.addLast(s1Char)
                    accS2.addLast(s2Char)
                }
            }

            if (accS1.isNotEmpty()) result.addLast(listOf(accS1.joinToString(""), accS2.joinToString("")))

            return result


        }

    }
}
