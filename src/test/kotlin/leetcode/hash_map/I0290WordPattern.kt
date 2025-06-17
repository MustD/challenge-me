package leetcode.hash_map

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0290 = (String, String) -> Boolean

class I0290WordPattern {
    data class Case(
        val pattern: String,
        val s: String,
        val output: Boolean,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0290> {

        override val cases: List<Case> = listOf(
            Case("abba", "dog cat cat dog", true),
            Case("abba", "dog cat cat fish", false),
            Case("aaaa", "dog cat cat dog", false),
        )
        override val solutions: List<Pair<String, I0290>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0290): Pair<Boolean, Any> {
            val result = solution(pattern, s)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

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
