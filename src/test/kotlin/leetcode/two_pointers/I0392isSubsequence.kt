package leetcode.two_pointers

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0392 = (String, String) -> Boolean

class I0392isSubsequence {

    data class Case(
        val s: String,
        val t: String,
        val output: Boolean,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0392> {
        override val cases: List<Case> = listOf(
            Case("abc", "ahbgdc", true),
            Case("axc", "ahbgdc", false),
        )
        override val solutions: List<Pair<String, I0392>> = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
        )

        override fun Case.check(solution: I0392): Pair<Boolean, Any> {
            val result = solution(s, t)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(s: String, t: String): Boolean {
            if (s.isEmpty()) return true
            var j = 0
            for (i in 0..t.lastIndex) {
                if (s[j] == t[i]) j++
                if (j == s.length) return true
            }
            return false
        }

        fun solution2(s: String, t: String): Boolean {
            tailrec fun recIsSub(sIdx: Int = 0, tIdx: Int = 0): Boolean {
                if (sIdx > s.lastIndex) return true
                if (tIdx > t.lastIndex) return false

                val nexS = if (s[sIdx] == t[tIdx]) sIdx + 1 else sIdx
                return recIsSub(nexS, tIdx + 1)
            }
            return recIsSub()
        }
    }
}
