package leetcode.dp_1d

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0070climbingStairs {

    data class Case(
        val input: Int,
        val output: Int,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (Int) -> Int> {
        override val cases: List<Case> = listOf(
            Case(2, 2),
            Case(3, 3),
        )
        override val solutions: List<Pair<String, (Int) -> Int>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: (Int) -> Int): Pair<Boolean, Any> {
            val result = solution(input)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(n: Int): Int {
            val memo = IntArray(n + 1) { idx ->
                when (idx) {
                    0 -> 0
                    1 -> 1
                    2 -> 2
                    3 -> 3
                    else -> -1
                }
            }

            fun calculate(x: Int): Int {
                if (memo[x] != -1) return memo[x]
                val result = calculate(x - 1) + calculate(x - 2)
                memo[x] = result
                return result
            }

            return calculate(n)
        }


    }
}
