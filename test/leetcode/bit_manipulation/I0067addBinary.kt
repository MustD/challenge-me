package leetcode.bit_manipulation

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0067 = (String, String) -> String

class I0067addBinary {

    @Nested
    inner class Solution : ProblemTest<I0067> {

        override val cases = testCases<I0067>(
            args("11", "1") expects "100",
            args("1010", "1011") expects "10101",
        )

        @Test
        fun test() = check(::solution1)

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
