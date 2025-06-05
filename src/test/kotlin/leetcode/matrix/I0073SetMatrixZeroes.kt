package leetcode.matrix

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toListOfIntLists
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0073 = (Array<IntArray>) -> Unit

class I0073SetMatrixZeroes {
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
    inner class Solution : AproblemTest<Case, I0073> {

        override val cases: List<Case> = listOf(
            prepareCase("[[1,1,1],[1,0,1],[1,1,1]]", "[[1,0,1],[0,0,0],[1,0,1]]"),
            prepareCase("[[0,1,2,0],[3,4,5,2],[1,3,1,5]]", "[[0,0,0,0],[0,4,5,0],[0,3,1,0]]"),
        )

        override val solutions = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0073): Pair<Boolean, Any> {
            val input = matrix.map { it.toIntArray() }.toTypedArray()
            solution(input)
            val result = input.map { it.toList() }.toList()
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solution1(matrix: Array<IntArray>): Unit {
            val zRows = mutableSetOf<Int>()
            val zCols = mutableSetOf<Int>()
            for (y in matrix.indices) {
                for (x in matrix[y].indices) {
                    if (matrix[y][x] == 0) {
                        zRows.add(y)
                        zCols.add(x)
                    }
                }
            }
            for (y in matrix.indices) {
                for (x in matrix[y].indices) {
                    if (y in zRows || x in zCols) {
                        matrix[y][x] = 0
                    }
                }
            }
        }
    }
}
