import org.junit.jupiter.api.Nested
import kotlin.math.max
import kotlin.test.Test


/**
 * https://leetcode.com/problems/longest-substring-without-repeating-characters/description/
 */
class I003longestSubstring {
    data class Case(
        val input: String,
        val output: Int,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (String) -> Int> {

        override val cases: List<Case> = listOf(
            Case("abcabcbb", 3),
            Case("bbbbb", 1),
            Case("pwwkew", 3),
            Case(" ", 1),
            Case("aa", 1),
            Case("", 0),
            Case("dvdf", 3),
        )
        override val solutions: List<Pair<String, (String) -> Int>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionAi.name to ::solutionAi,
        )

        override fun Case.check(solution: (String) -> Int): Pair<Boolean, Any> {
            val result = solution(input)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

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