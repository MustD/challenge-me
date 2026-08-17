package leetcode.hash_map

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0242 = (String, String) -> Boolean

class I0242ValidAnagram {

    @Nested
    inner class Solution : ProblemTest<I0242> {

        override val cases = testCases<I0242>(
            args("anagram", "nagaram") expects true,
            args("rat", "car") expects false,
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(s: String, t: String): Boolean {
            val counter = mutableMapOf<Char, Int>()
            s.forEach { counter[it] = counter.getOrDefault(it, 0) + 1 }
            val remove = { c: Char -> counter[c] = counter.getOrDefault(c, 0) - 1 }
            t.forEach { remove(it) }

            return counter.values.all { it == 0 }
        }


    }
}
