package leetcode.sliding_window

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.math.max
import kotlin.test.Test


typealias I0003 = (String) -> Int

/**
 * https://leetcode.com/problems/longest-substring-without-repeating-characters/description/
 */
class I0003longestSubstring {

    @Nested
    inner class Solution : ProblemTest<I0003> {

        override val cases = testCases<I0003>(
            "abcabcbb" expects 3,
            "bbbbb" expects 1,
            "pwwkew" expects 3,
            " " expects 1,
            "aa" expects 1,
            "" expects 0,
            "dvdf" expects 3,
        )

        @Test
        fun test() = check(::solution1, ::solutionAi)

        private fun solution1(s: String): Int {
            if (s.length <= 1) return s.length
            s.length
            var curMax = 0
            val map: MutableMap<Char, Int> = HashMap()

            var lowerIndex = 0
            for (upperIndex in s.indices) {
                if (map.containsKey(s[upperIndex])) {
                    lowerIndex = maxOf(
                        map.getOrDefault(s[upperIndex], 0),
                        lowerIndex
                    )
                }
                curMax = maxOf(curMax, upperIndex - lowerIndex + 1)
                map[s[upperIndex]] = upperIndex + 1
            }
            return curMax
        }

        fun solutionAi(s: String): Int {
            val n = s.length
            var ans = 0
            val map: MutableMap<Char, Int> = HashMap()

            var j = 0
            for (i in 0 until n) {
                if (map.containsKey(s[i])) {
                    j = maxOf(map[s[i]] ?: 0, j)
                }
                ans = max(ans, i - j + 1)
                map[s[i]] = i + 1
            }
            return ans
        }

    }
}
