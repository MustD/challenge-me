package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0135 = (IntArray) -> Int

class I0135candy {

    data class Case(
        val nums: List<Int>,
        val output: Int,
    )

    val prepareCase = { n1: String, r: Int ->
        Case(n1.toIntArray().toList(), r)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0135> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,0,2]", 5),
            prepareCase("[1,2,2]", 4)
        )
        override val solutions: List<Pair<String, I0135>> = listOf(
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0135): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()


        fun solutionEditorial(ratings: IntArray): Int {
            val left = IntArray(ratings.size) { 1 }
            for (i in 1..ratings.lastIndex) {
                if (ratings[i] > ratings[i - 1]) left[i] = left[i - 1] + 1
            }
            val right = IntArray(ratings.size) { 1 }
            for (i in ratings.lastIndex - 1 downTo 0) {
                if (ratings[i] > ratings[i + 1]) right[i] = right[i + 1] + 1
            }
            return (0..ratings.lastIndex).sumOf { maxOf(left[it], right[it]) }
        }


    }
}
