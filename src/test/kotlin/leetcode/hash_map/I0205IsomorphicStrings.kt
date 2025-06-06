package leetcode.hash_map

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0205 = (String, String) -> Boolean

class I0205IsomorphicStrings {
    data class Case(
        val s: String,
        val t: String,
        val output: Boolean,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0205> {

        override val cases: List<Case> = listOf(
            Case("egg", "add", true),
            Case("foo", "bar", false),
            Case("paper", "title", true),
            Case("badc", "baba", false)
        )
        override val solutions: List<Pair<String, I0205>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0205): Pair<Boolean, Any> {
            val result = solution(s, t)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solution1(s: String, t: String): Boolean {
            val mapping = mutableMapOf<Char, Char>()
            val used = mutableSetOf<Char>()
            println("============")
            for (i in s.indices) {
                println(mapping)
                if (mapping.containsKey(s[i])) {
                    if (mapping[s[i]] != t[i]) return false
                } else {
                    if(used.contains(t[i])) return false
                    mapping[s[i]] = t[i]
                    used.add(t[i])
                }
            }
            return true
        }


    }
}
