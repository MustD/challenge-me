package leetcode.sliding_window

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0076 = (String, String) -> String

class I0076minimumWindowSubstring {

    @Nested
    inner class Solution : ProblemTest<I0076> {
        override val cases = testCases<I0076>(
            args("ADOBECODEBANC", "ABC") expects "BANC",
            args("a", "a") expects "a",
            args("a", "aa") expects "",
            args("ab", "a") expects "a",
            args("ab", "b") expects "b",
        )

        @Test
        fun test() = check(::solution1)

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
