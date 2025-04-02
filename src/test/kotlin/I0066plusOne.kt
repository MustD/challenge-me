import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0066 = (IntArray) -> IntArray

class I0066plusOne {
    data class Case(
        val input: List<Int>,
        val output: List<Int>,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0066> {

        override val cases: List<Case> = listOf(
            Case(listOf(1, 2, 3), listOf(1, 2, 4)),
        )

        override val solutions = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0066): Pair<Boolean, Any> {
            val result = solution(input.toIntArray()).toList()
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solution1(digits: IntArray): IntArray {
            var output = digits.clone()
            fun addOne(idx: Int) {
                if (idx < 0) {
                    output = intArrayOf(1).plus(output)
                    return
                }
                val num = output[idx] + 1
                output[idx] = num % 10
                if (num >= 10) addOne(idx - 1)
            }

            addOne(output.lastIndex)
            return output
        }


    }
}
