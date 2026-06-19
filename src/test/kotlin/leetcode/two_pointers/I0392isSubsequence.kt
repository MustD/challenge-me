package leetcode.two_pointers

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0392 = (String, String) -> Boolean

class I0392isSubsequence {

    @Nested
    inner class Solution : ProblemTest<I0392> {
        override val cases = testCases<I0392>(
            args("abc", "ahbgdc") expects true,
            args("axc", "ahbgdc") expects false,
        )

        @Test
        fun test() = check(::solution1, ::solution2)

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
