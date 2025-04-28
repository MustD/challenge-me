package leetcode.bit

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0067 = (String, String) -> String

class I0067addBinary {

    data class Case(
        val a: String,
        val b: String,
        val output: String,
    )


    @Nested
    inner class Solution : AproblemTest<Case, I0067> {

        override val cases: List<Case> = listOf(
            Case("11", "1", "100"),
            Case("1010", "1011", "10101"),
        )

        override val solutions: List<Pair<String, I0067>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0067): Pair<Boolean, Any> {
            val result = solution(a, b)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(a: String, b: String): String {
            val n = a.length
            val m = b.length
            if (n < m) return solution1(b, a)

            val sb = StringBuilder()
            var carry = 0
            var j = b.lastIndex
            for (i in a.lastIndex downTo 0) {
                if (a[i] == '1') ++carry
                if (j > -1 && b[j--] == '1') ++carry
                if (carry % 2 == 1) sb.append('1') else sb.append('0')
                carry /= 2
            }
            if (carry == 1) sb.append('1')

            return sb.reversed().toString()
        }


    }
}
