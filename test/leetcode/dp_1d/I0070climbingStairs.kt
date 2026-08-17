package leetcode.dp_1d

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0070 = (Int) -> Int

class I0070climbingStairs {

    @Nested
    inner class Solution : ProblemTest<I0070> {
        override val cases = testCases<I0070>(
            2 expects 2,
            3 expects 3,
        )

        @Test
        fun test() = check(::solution1)

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
