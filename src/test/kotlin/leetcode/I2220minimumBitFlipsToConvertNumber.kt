package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I2220 = (Int, Int) -> Int

class I2220minimumBitFlipsToConvertNumber {

    @Nested
    inner class Solution : ProblemTest<I2220> {
        override val cases = testCases<I2220>(
            args(10, 7) expects 3,
            args(3, 4) expects 3,
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(start: Int, goal: Int): Int {
            var counter = 0
            for (i in 0 until 32) {
                if ((start shr i and 1) != (goal shr i and 1)) counter++
            }
            return counter
        }
    }
}
