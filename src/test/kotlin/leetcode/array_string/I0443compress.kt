package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 443. String Compression  (https://leetcode.com/problems/string-compression/)
 *
 * Given an array of characters `chars`, compress it in place using run-length encoding:
 * walk the consecutive groups of equal characters and, for each group, append the character
 * followed by the group length — but only write the length when it is 2 or more (a group of
 * length 1 is written as just the character). A length of 10 or more is written as its
 * individual digit characters (e.g. a run of 12 'a's becomes 'a','1','2').
 *
 * The compressed result must be stored back into the front of the input `chars`, and the
 * function returns the new length. Elements beyond the returned length are irrelevant. You must
 * use only constant extra space.
 *
 * Constraints:
 * - 1 <= chars.length <= 2000
 * - chars[i] is a lowercase/uppercase English letter, a digit, or a symbol.
 * - The compressed length is always <= the original length.
 *
 * Harness caveat (read before solving):
 * - The real LeetCode answer is *both* the returned length *and* the characters written into the
 *   first `length` slots of `chars`. This harness compares only the returned `Int`, so it verifies
 *   the length but NOT what you wrote into the array. Make sure your in-place writes are correct
 *   too — the test passing on length alone is not full proof. (If you want the harness to check the
 *   written prefix as well, change the typealias to return `CharArray` and have your solution return
 *   `chars.copyOf(length)`; the `CharArray` converter is registered.)
 * - Aim for the in-place two-pointer (read/write index) technique: a `write` pointer placing
 *   characters and a `read` pointer scanning each group. O(n) time, O(1) extra space.
 */
typealias I0443 = (CharArray) -> Int

class I0443compress {

    @Nested
    inner class Solution : ProblemTest<I0443> {

        override val cases = testCases<I0443>(
            // ["a","a","b","b","c","c","c"] -> first 6 chars become a,2,b,2,c,3
            """["a","a","b","b","c","c","c"]""" expects 6,
            // single char, stays uncompressed
            """["a"]""" expects 1,
            // run of 13 'a's + run of 2 'b's -> a,1,3,b,2  (length is split into digits)
            """["a","a","a","a","a","a","a","a","a","a","a","a","a","b","b"]""" expects 5,
            // all distinct -> nothing compressed, length unchanged
            """["a","b","c"]""" expects 3,
            // run of exactly 2 -> a,2
            """["a","a"]""" expects 2,
        )

        @Test
        fun test() = check(::compress)

        /**
         * Analysis of this solution
         *
         * Pattern: in-place two-pointer (fast/slow, a.k.a. read/write index). `fast` scans each
         * run of equal chars; `slow` is the write head placing the compressed output back into the
         * same array. This is the standard "overwrite the array as you consume it" template used by
         * remove-duplicates / move-zeroes style problems.
         *
         * Time: O(n). The outer `while` plus the inner run-scanning `while` together advance `fast`
         * exactly once past every element — each index is visited a constant number of times.
         * `count.toString().forEach` writes at most 4 digit chars per run (n <= 2000), so the digit
         * work is bounded and doesn't change the linear bound.
         *
         * Space: O(1) auxiliary, as required — the compression happens inside the input array.
         * The one nuance: `count.toString()` allocates a tiny temporary String (<= 4 chars). It is
         * bounded-constant, not truly zero-allocation; a strictly allocation-free version would
         * write the digits manually (see "Alternatives"). No recursion, so no stack cost.
         *
         * Correctness / why in-place is safe: the key invariant is `slow <= from` at the start of
         * every run (both begin at 0). Processing a run advances `slow` by `1 + digits(count)` and
         * the read head by `count`; since `1 + digits(count) <= count` for all count >= 1, the write
         * head never overtakes the read start of the current run — so we never clobber a character
         * before reading it. Edge cases handled cleanly: single element (`count == 1`, digit skipped),
         * run of exactly 2 (`a,2`), and multi-digit runs like 13 -> `a,1,3` via the per-digit
         * `forEach`. `char` is captured before the inner loop, so the write is correct even after
         * `fast` has moved on.
         *
         * Harness caveat: the typealias returns `Int`, so the test verifies only the returned length,
         * not the bytes written into the prefix. This solution's prefix is correct, but a length-only
         * pass is not full proof (see the file header for how to check the written prefix too).
         *
         * Alternatives:
         * - Manual digit emission instead of `count.toString()`: write digits into the array and
         *   reverse that short span in place (or compute a power-of-ten divisor first). Same O(n)/O(1)
         *   asymptotics but zero heap allocation — the "purest" in-place answer. Marginal in practice.
         * - Build a fresh output list/StringBuilder then copy back: simpler to read but O(n) extra
         *   space, violating the constant-space constraint. Not better here.
         * This solution is already asymptotically optimal: every character must be read at least once
         * to count runs, so O(n) time is a lower bound, and O(1) space matches the constraint.
         *
         * Parallelism: RLE is *mostly* sequential because of the in-place write dependency, but the
         * counting phase is a classic map-reduce — split the array into k chunks, run-length-encode
         * each independently, then merge, fixing up runs that straddle chunk boundaries (a boundary
         * where the last char of chunk i equals the first of chunk i+1 must be combined). Real speedup
         * only materializes on large inputs; at n <= 2000 the thread/coordination overhead dwarfs any
         * gain, so single-threaded is the right call here — that trade-off *is* the lesson.
         *
         * Real-world: run-length encoding is a genuine production primitive — bitmap/fax formats
         * (BMP, PCX, TIFF Packbits), and as a pre-stage before entropy coding in several codecs.
         * More relevantly today, columnar analytics stores (Parquet, ORC, Arrow) RLE-encode long runs
         * of repeated column values, and vectorized query engines operate directly on the encoded runs
         * to skip decompression. There the input is streaming/unbounded and chunked, the "return the
         * length" contract becomes "emit (value,count) pairs", and cache/SIMD behavior over large
         * batches matters far more than the micro-optimization of avoiding one small String allocation.
         */
        fun compress(chars: CharArray): Int {
            var slow = 0
            var fast = 0
            while (fast <= chars.lastIndex) {
                val char = chars[fast]

                val from = fast
                while (fast <= chars.lastIndex && char == chars[fast]) fast++

                val count = fast - from
                chars[slow++] = char
                if (count > 1) count.toString().forEach { chars[slow++] = it }
            }
            return slow
        }

    }
}
