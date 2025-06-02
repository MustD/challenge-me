package leetcode.sliding_window

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0076 = (String, String) -> String

class I0076minimumWindowSubstring {
    data class Case(
        val s: String,
        val t: String,
        val output: String,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0076> {
        override val cases: List<Case> = listOf(
            Case("ADOBECODEBANC", "ABC", "BANC"),
            Case("a", "a", "a"),
            Case("a", "aa", ""),
            Case("ab", "a", "a"),
            Case("ab", "b", "b")
        )
        override val solutions: List<Pair<String, I0076>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0076): Pair<Boolean, Any> {
            val result = solution(s, t)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solution1(s: String, t: String): String {
            if (s.length < t.length) return ""
            if (s.length == 1 && t.length == 1) return if (s[0] == t[0]) s else ""

            val usageMap = mutableMapOf<Char, Int>()
            t.forEach { usageMap[it] = usageMap.getOrDefault(it, 0) + 1 }


            fun isValid() = usageMap.values.all { it <= 0 }

            fun add(c: Char) {
                if (usageMap.containsKey(c)) usageMap[c] = usageMap.getOrDefault(c, 0) - 1
            }

            fun remove(c: Char) {
                if (usageMap.containsKey(c)) usageMap[c] = usageMap.getOrDefault(c, 0) + 1
            }

            var result = ""
            var low = 0
            add(s[low])
            if (isValid()) result = s[low].toString()

            var high = 1
            add(s[high])

            while (low <= high) {
                print("l:$low, h:$high | ")
                print("$usageMap | ")

                if (isValid()) {
                    result = when {
                        result == "" -> s.substring(low, high + 1)
                        result.length > (high - low) -> s.substring(low, high + 1)
                        else -> result
                    }
                    remove(s[low])
                    low++
                } else {
                    high++
                    if (high > s.lastIndex) break
                    add(s[high])
                }
                println(result)
            }

            return result
        }

    }
}
