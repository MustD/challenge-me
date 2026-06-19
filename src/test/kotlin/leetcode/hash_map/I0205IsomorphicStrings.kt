package leetcode.hash_map

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0205 = (String, String) -> Boolean

class I0205IsomorphicStrings {

    @Nested
    inner class Solution : ProblemTest<I0205> {

        override val cases = testCases<I0205>(
            args("egg", "add") expects true,
            args("foo", "bar") expects false,
            args("paper", "title") expects true,
            args("badc", "baba") expects false,
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(s: String, t: String): Boolean {
            val mapping = mutableMapOf<Char, Char>()
            val used = mutableSetOf<Char>()
            println("============")
            for (i in s.indices) {
                println(mapping)
                if (mapping.containsKey(s[i])) {
                    if (mapping[s[i]] != t[i]) return false
                } else {
                    if (used.contains(t[i])) return false
                    mapping[s[i]] = t[i]
                    used.add(t[i])
                }
            }
            return true
        }


    }
}
