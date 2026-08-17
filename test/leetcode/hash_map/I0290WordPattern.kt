package leetcode.hash_map

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0290 = (String, String) -> Boolean

class I0290WordPattern {

    @Nested
    inner class Solution : ProblemTest<I0290> {

        override val cases = testCases<I0290>(
            args("abba", "dog cat cat dog") expects true,
            args("abba", "dog cat cat fish") expects false,
            args("aaaa", "dog cat cat dog") expects false,
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(pattern: String, s: String): Boolean {
            val map = mutableMapOf<Char, String>()
            val used = mutableSetOf<String>()
            val words = s.split(" ")
            if (words.size != pattern.length) return false

            for (i in pattern.indices) {
                if (map.containsKey(pattern[i])) {
                    if (words[i] != map[pattern[i]]) {
                        return false
                    }
                } else {
                    if (used.contains(words[i])) return false
                    map[pattern[i]] = words[i]
                    used.add(words[i])
                }
            }
            return true
        }


    }
}
