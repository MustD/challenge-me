package leetcode.hash_map

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0242 = (String, String) -> Boolean

class I0242ValidAnagram {
    data class Case(
        val s: String,
        val t: String,
        val output: Boolean,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0242> {

        override val cases: List<Case> = listOf(
            Case("anagram", "nagaram", true),
            Case("rat", "car", false),
        )
        override val solutions: List<Pair<String, I0242>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0242): Pair<Boolean, Any> {
            val result = solution(s, t)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solution1(s: String, t: String): Boolean {
            val counter = mutableMapOf<Char, Int>()
            s.forEach { counter[it] = counter.getOrDefault(it, 0) + 1 }
            val remove = { c: Char -> counter[c] = counter.getOrDefault(c, 0) - 1 }
            t.forEach { remove(it) }

            return counter.values.all { it == 0 }
        }


    }
}
