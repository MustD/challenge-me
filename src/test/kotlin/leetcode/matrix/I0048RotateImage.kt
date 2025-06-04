package leetcode.matrix

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toListOfIntLists
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0048 = (Array<IntArray>) -> Unit

class I0048RotateImage {
    data class Case(
        val matrix: List<List<Int>>,
        val output: List<List<Int>>,
    )

    fun prepareCase(matrix: String, output: String): Case {
        return Case(
            matrix.toListOfIntLists(),
            output.toListOfIntLists(),
        )
    }

    @Nested
    inner class Solution : AproblemTest<Case, I0048> {

        override val cases: List<Case> = listOf(
            prepareCase("[[1,2,3],[4,5,6],[7,8,9]]", "[[7,4,1],[8,5,2],[9,6,3]]"),
            prepareCase(
                "[[5,1,9,11],[2,4,8,10],[13,3,6,7],[15,14,12,16]]",
                "[[15,13,2,5],[14,3,4,1],[12,6,8,9],[16,7,10,11]]"
            ),
        )

        override val solutions = listOf(
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0048): Pair<Boolean, Any> {
            val input = matrix.map { it.toIntArray() }.toTypedArray()
            solution(input)
            val result = input.map { it.toList() }.toList()
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solutionEditorial(matrix: Array<IntArray>): Unit {
            val s: Int = matrix.size
            val li = matrix.lastIndex
            for (i in 0..<(s + 1) / 2) {
                for (j in 0..<s / 2) {

                    val temp = matrix[li - j][i]
                    matrix[li - j][i] = matrix[li - i][li - j]
                    matrix[li - i][li - j] = matrix[j][li - i]
                    matrix[j][li - i] = matrix[i][j]
                    matrix[i][j] = temp
                }
            }
        }


    }
}
