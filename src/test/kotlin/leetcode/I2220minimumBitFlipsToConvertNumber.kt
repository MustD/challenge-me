package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I2220minimumBitFlipsToConvertNumber {

    data class Case(
        val start: Int,
        val goal: Int,
        val output: Int,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (Int, Int) -> Int> {
        override val cases: List<Case> = listOf(
            Case(10, 7, 3),
            Case(3, 4, 3),
        )
        override val solutions: List<Pair<String, (Int, Int) -> Int>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: (Int, Int) -> Int): Pair<Boolean, Any> {
            val result = solution(start, goal)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(start: Int, goal: Int): Int {
            var counter = 0
            for (i in 0 until 32) {
                if ((start shr i and 1) != (goal shr i and 1)) counter++
            }
            return counter
        }
    }
}
