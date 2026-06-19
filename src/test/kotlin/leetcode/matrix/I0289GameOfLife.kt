package leetcode.matrix

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

// LeetCode's signature is `(Array<IntArray>) -> Unit`: the solution advances `board` one
// generation in place and returns nothing. The ProblemTest harness asserts on the *return value*,
// so we return the (mutated) input board instead of Unit to make the next state checkable. To
// match LeetCode exactly, drop the `: Array<IntArray>` return type and the trailing `return board`.
typealias I0289 = (Array<IntArray>) -> Array<IntArray>

class I0289GameOfLife {

    @Nested
    inner class Solution : ProblemTest<I0289> {

        override val cases = testCases<I0289>(
            args("[[0,1,0],[0,0,1],[1,1,1],[0,0,0]]") expects "[[0,0,0],[1,0,1],[0,1,1],[0,1,0]]",
            args("[[1,1],[1,0]]") expects "[[1,1],[1,1]]",
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(board: Array<IntArray>): Array<IntArray> {
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
            return board
        }
    }
}
