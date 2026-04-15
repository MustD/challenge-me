package leetcode.graph_general

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0130 = (Array<CharArray>) -> Array<CharArray>

class I0130surroundedRegions {

    @Nested
    inner class Solution : ProblemTest<I0130> {

        override val cases = testCases<I0130>(
            """ [["X","X","X","X"],["X","O","O","X"],["X","X","O","X"],["X","O","X","X"]]"""
                    expects
                    """[["X","X","X","X"],["X","X","X","X"],["X","X","X","X"],["X","O","X","X"]]""",
            """[["O","X","X","O","X"],["X","O","O","X","O"],["X","O","X","O","X"],["O","X","O","O","O"],["X","X","O","X","O"]]"""
                    expects
                    """[["O","X","X","O","X"],["X","X","X","X","O"],["X","X","X","O","X"],["O","X","O","O","O"],["X","X","O","X","O"]]"""
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(board: Array<CharArray>): Array<CharArray> {
            val yMax = board.lastIndex
            val xMax = board[0].lastIndex

            fun isOutOfBounds(y: Int, x: Int): Boolean = y !in 0..yMax || x !in 0..xMax
            fun markRegionAsNotSurrounded(y: Int, x: Int) {
                val stack = ArrayDeque<Pair<Int, Int>>()

                stack.addLast(y to x)
                while (stack.isNotEmpty()) {
                    val (y, x) = stack.removeFirst()
                    when {
                        isOutOfBounds(y, x) -> Unit
                        board[y][x] == 'X' -> Unit
                        board[y][x] == 'S' -> Unit
                        else -> {
                            board[y][x] = 'S'
                            stack.addLast(y - 1 to x)
                            stack.addLast(y + 1 to x)
                            stack.addLast(y to x - 1)
                            stack.addLast(y to x + 1)
                        }

                    }
                }
            }
            board.first().forEachIndexed { x, _ -> markRegionAsNotSurrounded(0, x) }
            board.last().forEachIndexed { x, _ -> markRegionAsNotSurrounded(yMax, x) }
            board.forEachIndexed { y, _ -> markRegionAsNotSurrounded(y, 0) }
            board.forEachIndexed { y, _ -> markRegionAsNotSurrounded(y, xMax) }

            board.forEachIndexed { y, chars ->
                chars.forEachIndexed { x, _ ->
                    if (board[y][x] == 'O') board[y][x] = 'X'
                    if (board[y][x] == 'S') board[y][x] = 'O'
                }
            }

            return board
        }

    }
}
