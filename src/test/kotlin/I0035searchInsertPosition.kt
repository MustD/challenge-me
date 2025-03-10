import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0035searchInsertPosition {

    data class Case(
        val nums: List<Int>,
        val target: Int,
        val output: Int,
    )

    private fun prepareCase(nums: String, target: Int, output: Int) =
        Case(nums.split(",").map { it.toInt() }, target, output)

    @Nested
    inner class Solution : AproblemTest<Case, (IntArray, Int) -> Int> {
        override val cases: List<Case> = listOf(
            prepareCase("1,3,5,6", 5, 2),
            prepareCase("1,3,5,6", 2, 1),
            prepareCase("1,3,5,6", 7, 4),
        )
        override val solutions: List<Pair<String, (IntArray, Int) -> Int>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: (IntArray, Int) -> Int): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray(), target)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(nums: IntArray, target: Int): Int {
            fun srch(s: Int, e: Int): Int {
                val size = e - s
                val m = s + (size / 2) //searching middle index
                return when {
                    nums[m] == target -> m //if middle value == target return m index

                    nums[m] > target -> {
                        if (size == 0) return m
                        srch(s, m)
                    } //if middle value > target take left part

                    nums[m] < target -> {
                        if (size == 0) return m + 1
                        srch(m + 1, e)
                    } //if middle value < target take right part
                    else -> -1
                }
            }

            return srch(0, nums.lastIndex)
        }


    }
}