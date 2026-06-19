package leetcode.matrix

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0036 = (Array<CharArray>) -> Boolean

class I0036validSudoku {

    @Nested
    inner class Solution : ProblemTest<I0036> {
        override val cases = testCases<I0036>(
            """
                [["5","3",".",".","7",".",".",".","."]
                ,["6",".",".","1","9","5",".",".","."]
                ,[".","9","8",".",".",".",".","6","."]
                ,["8",".",".",".","6",".",".",".","3"]
                ,["4",".",".","8",".","3",".",".","1"]
                ,["7",".",".",".","2",".",".",".","6"]
                ,[".","6",".",".",".",".","2","8","."]
                ,[".",".",".","4","1","9",".",".","5"]
                ,[".",".",".",".","8",".",".","7","9"]]
            """ expects true,
            """
                [["8","3",".",".","7",".",".",".","."]
                ,["6",".",".","1","9","5",".",".","."]
                ,[".","9","8",".",".",".",".","6","."]
                ,["8",".",".",".","6",".",".",".","3"]
                ,["4",".",".","8",".","3",".",".","1"]
                ,["7",".",".",".","2",".",".",".","6"]
                ,[".","6",".",".",".",".","2","8","."]
                ,[".",".",".","4","1","9",".",".","5"]
                ,[".",".",".",".","8",".",".","7","9"]]
            """ expects false,
        )

        @Test
        fun test() = check(::solution1, ::community, ::solution2)

        fun solution1(board: Array<CharArray>): Boolean {
            val check = { arr: CharArray ->
                val res = arr.filterNot { it == '.' }
                res.size == res.distinct().size
            }

            for (i in 0..8) {
                val row = board[i]
                if (check(row).not()) {
//                    print("row $row" + row.toList())
                    return false
                }
            }

            for (i in 0..8) {
                val col = arrayOf(
                    board[0][i], board[1][i], board[2][i], board[3][i],
                    board[4][i], board[5][i], board[6][i], board[7][i],
                    board[8][i]
                ).toCharArray()
                if (check(col).not()) {
//                    print("col $col" + col.toList())
                    return false
                }
            }

            for (cell in 0..8) {
                val subY = (cell / 3)
                val subX = (cell % 3)
                val subBoard = (0..8).map { board[(it / 3) + (subY * 3)][(it % 3) + (subX * 3)] }.toCharArray()
                if (check(subBoard).not()) {
//                    print("cell $cell" + subBoard.toList())
                    return false
                }
            }

            return true
        }


        val n = 9
        fun community(board: Array<CharArray>): Boolean {
            for (x in 0 until n) {
                val col = BooleanArray(n)
                for (y in 0 until n) {
                    val cell = board[y][x]
                    if (cell == '.') continue
                    if (col[cell - '1']) return false
                    col[cell - '1'] = true
                }
            }

            for (y in 0 until n) {
                val row = BooleanArray(n)
                for (x in 0 until n) {
                    val cell = board[y][x]
                    if (cell == '.') continue
                    if (row[cell - '1']) return false
                    row[cell - '1'] = true
                }
            }

            for (offset in 0 until n) {
                val offsetX = offset % 3
                val offsetY = offset / 3

                val square = BooleanArray(n)
                for (pos in 0 until n) {
                    val x = 3 * offsetX + pos % 3
                    val y = 3 * offsetY + pos / 3

                    val cell = board[y][x]
                    if (cell == '.') continue
                    if (square[cell - '1']) return false
                    square[cell - '1'] = true
                }
            }

            return true
        }

        fun solution2(board: Array<CharArray>): Boolean {
            fun check(arr: CharArray): Boolean {
                val result = BooleanArray(9)
                for (c in arr) {
                    if (c == '.') continue
                    if (result[c - '1']) return false
                    result[c - '1'] = true
                }
                return true
            }

            for (i in 0..8) {
                val row = board[i]
                if (check(row).not()) {
                    return false
                }
            }

            for (i in 0..8) {
                val col = arrayOf(
                    board[0][i], board[1][i], board[2][i], board[3][i],
                    board[4][i], board[5][i], board[6][i], board[7][i],
                    board[8][i]
                ).toCharArray()
                if (check(col).not()) {
                    return false
                }
            }

            for (cell in 0..8) {
                val subY = (cell / 3)
                val subX = (cell % 3)
                val subBoard = (0..8).map { board[(it / 3) + (subY * 3)][(it % 3) + (subX * 3)] }.toCharArray()
                if (check(subBoard).not()) {
                    return false
                }
            }

            return true
        }


    }
}
