import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * https://leetcode.com/problems/apply-operations-on-array-to-maximize-sum-of-squares
 */
class I2897applyOperationsOnArrayToMaximizeSumOfSquares {
    data class Case(
        val nums: List<Int>,
        val k: Int,
        val output: Int,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (List<Int>, Int) -> Int> {
        override val cases: List<Case> = listOf(
            Case(listOf(2, 6, 5, 8), 2, 261),
            Case(listOf(4, 5, 4, 7), 3, 90),
            Case(listOf(96, 66, 60, 58, 32, 17, 63, 21, 30, 44, 15, 8, 98, 93), 2, 32258),
            Case(listOf(25, 52, 75, 65), 4, 24051),

            )

        override val solutions: List<Pair<String, (List<Int>, Int) -> Int>> = listOf(
            "solution 1" to ::solution1,
            "solution 2" to ::solution2,
        )

        override fun Case.check(solution: (List<Int>, Int) -> Int): Pair<Boolean, Any> {
            val result = solution(nums, k)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun List<Int>.print(): List<Int> {
            println("--- list $this ---")
            forEach { println(it.toString(2)) }
            println("------------------")
            return this
        }

        //too long
        fun solution1(nums: List<Int>, k: Int): Int {
            fun List<Int>.operation(i: Int, j: Int): List<Int> = mapIndexed { idx, value ->
                when (idx) {
                    i -> get(i) and get(j)
                    j -> get(i) or get(j)
                    else -> value
                }
            }

            fun List<Int>.calculation() = takeLast(k).sumOf { it * it }

            var result: List<Int> = nums.sorted()
            repeat(k) { kIdx ->
                repeat(nums.size) { idx ->
                    result = result.operation(idx, result.lastIndex - kIdx)
                }
            }

            return result.calculation() % 1000000007
        }

        //todo: optimize
        fun solution2(nums: List<Int>, k: Int): Int {
            fun List<Int>.operation(i: Int, j: Int): List<Int> = mapIndexed { idx, value ->
                when (idx) {
                    i -> get(i) and get(j)
                    j -> get(i) or get(j)
                    else -> value
                }
            }

            fun List<Int>.calculation() = takeLast(k).sumOf { it * it }

            var result: List<Int> = nums.sorted()
            repeat(k) { kIdx ->
                repeat(nums.size) { idx ->
                    result = result.operation(idx, result.lastIndex - kIdx)
                }
            }

            return result.calculation() % 1000000007

        }
    }

}
