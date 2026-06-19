package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I2419 = (IntArray) -> Int

class I2419longestSubarrayWithMaximumBitwiseAnd {

    @Nested
    inner class Solution : ProblemTest<I2419> {
        override val cases = testCases<I2419>(
            "[1,2,3,3,2,2]" expects 2,
            "[1,2,3,4]" expects 1,
            "[311155,311155,311155,311155,311155,311155,311155,311155,201191,311155]" expects 8,
            "[378034,378034,378034]" expects 3,
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(nums: IntArray): Int {
            var maxVal = 0
            var ans = 0
            var currentStreak = 0

            for (num in nums) {
                if (maxVal < num) {
                    maxVal = num
                    ans = 0
                    currentStreak = 0
                }

                if (maxVal == num) {
                    currentStreak++
                } else {
                    currentStreak = 0
                }

                ans = ans.coerceAtLeast(currentStreak)
            }
            return ans
        }

    }
}
