import common.IntArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I2419longestSubarrayWithMaximumBitwiseAnd {

    data class Case(
        val input: IntArray,
        val output: Int,
    )

    val prepareCase = { s: String, o: Int ->
        Case(s.toIntArray(), o)
    }

    @Nested
    inner class Solution : AproblemTest<Case, (IntArray) -> Int> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,2,3,3,2,2]", 2),
            prepareCase("[1,2,3,4]", 1),
            prepareCase("[311155,311155,311155,311155,311155,311155,311155,311155,201191,311155]", 8),
            prepareCase("[378034,378034,378034]", 3),
        )
        override val solutions: List<Pair<String, (IntArray) -> Int>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: (IntArray) -> Int): Pair<Boolean, Any> {
            val result = solution(input.copyOf())
            return (result == output) to result
        }

        @Test
        fun test() = check()

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