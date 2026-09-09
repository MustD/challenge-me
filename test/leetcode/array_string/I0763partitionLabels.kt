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
