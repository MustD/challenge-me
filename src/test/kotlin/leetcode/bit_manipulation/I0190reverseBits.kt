package leetcode.bit_manipulation

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0190 = (Int) -> Int

class I0190reverseBits {

    data class Case(
        val a: Int,
        val output: Int,
    )


    @Nested
    inner class Solution : AproblemTest<Case, I0190> {

        override val cases: List<Case> = listOf(
            Case(43261596, 964176192),
            Case(2147483644, 1073741822),
        )

        override val solutions: List<Pair<String, I0190>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0190): Pair<Boolean, Any> {
            val result = solution(a)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(n: Int): Int {
            val str = n.toString(2).padStart(32, '0')
            val reversed = str.reversed()
            return reversed.toInt(2)
        }


    }
}
