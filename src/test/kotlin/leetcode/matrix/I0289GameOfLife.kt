package leetcode.matrix

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toListOfIntLists
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0289 = (Array<IntArray>) -> Unit

class I0289GameOfLife {
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
    inner class Solution : AproblemTest<Case, I0289> {

        override val cases: List<Case> = listOf(
            prepareCase("[[0,1,0],[0,0,1],[1,1,1],[0,0,0]]", "[[0,0,0],[1,0,1],[0,1,1],[0,1,0]]"),
            prepareCase("[[1,1],[1,0]]", "[[1,1],[1,1]]"),
        )

        override val solutions = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0289): Pair<Boolean, Any> {
            val input = matrix.map { it.toIntArray() }.toTypedArray()
            solution(input)
            val result = input.map { it.toList() }.toList()
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solution1(board: Array<IntArray>): Unit {
            val state = mutableMapOf<Pair<Int, Int>, Int>()
            fun isLife(y: Int, x: Int): Boolean {
                val current = board[y][x]
                var count = 0
                if (board.getOrNull(y - 1)?.getOrNull(x - 1) == 1) count++
                if (board.getOrNull(y - 1)?.getOrNull(x) == 1) count++
                if (board.getOrNull(y - 1)?.getOrNull(x + 1) == 1) count++
                if (board.getOrNull(y)?.getOrNull(x - 1) == 1) count++
                if (board.getOrNull(y)?.getOrNull(x + 1) == 1) count++
                if (board.getOrNull(y + 1)?.getOrNull(x - 1) == 1) count++
                if (board.getOrNull(y + 1)?.getOrNull(x) == 1) count++
                if (board.getOrNull(y + 1)?.getOrNull(x + 1) == 1) count++
                return when (current) {
                    0 -> count == 3
                    1 -> count in 2..3
                    else -> false
                }
            }
            for (y in board.indices) {
                for (x in board[y].indices) {
                    state[y to x] = if (isLife(y, x)) 1 else 0
                }
            }

            for (y in board.indices) {
                for (x in board[y].indices) {
                    board[y][x] = state.getOrDefault(y to x, 0)
                }
            }
        }
    }
}
