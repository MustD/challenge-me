package leetcode.matrix

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0054 = (Array<IntArray>) -> List<Int>

class I0054spiralMatrix {

    @Nested
    inner class Solution : ProblemTest<I0054> {

        override val cases = testCases<I0054>(
            "[[1,2,3],[4,5,6],[7,8,9]]" expects "[1,2,3,6,9,8,7,4,5]",
            "[[1,2,3,4],[5,6,7,8],[9,10,11,12]]" expects "[1,2,3,4,8,12,11,10,9,5,6,7]",
            "[[3],[2]]" expects "[3,2]",
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(matrix: Array<IntArray>): List<Int> {
            val result = mutableListOf<Int>()

            data class Coords(var x: Int, var y: Int)

            val coords = Coords(0, 0)
            fun Coords.moveLeft() = x--
            fun Coords.moveRight() = x++
            fun Coords.moveUp() = y--
            fun Coords.moveDown() = y++
            fun Coords.get() = matrix[y][x]

            data class Bounds(var left: Int, var right: Int, var up: Int, var down: Int)

            val bounds = Bounds(left = 0, right = matrix[0].lastIndex, up = 0, down = matrix.lastIndex)
            fun Bounds.reduceUpper() = up++
            fun Bounds.reduceBottom() = down--
            fun Bounds.reduceLeft() = left++
            fun Bounds.reduceRight() = right--


            var direction = if (matrix[0].size > 1) 'r' else 'd'
            repeat((1..(matrix.size * matrix[0].size)).count()) {
                result.add(coords.get())
                when (direction) {
                    'r' -> {
                        coords.moveRight()
                        if (coords.x == bounds.right) {
                            bounds.reduceUpper()
                            direction = 'd'
                        }
                    }

                    'd' -> {
                        coords.moveDown()
                        if (coords.y == bounds.down) {
                            bounds.reduceRight()
                            direction = 'l'
                        }
                    }

                    'l' -> {
                        coords.moveLeft()
                        if (coords.x == bounds.left) {
                            bounds.reduceBottom()
                            direction = 'u'
                        }
                    }

                    'u' -> {
                        coords.moveUp()
                        if (coords.y == bounds.up) {
                            bounds.reduceLeft()
                            direction = 'r'
                        }
                    }

                    else -> throw IllegalStateException("direction: $direction")
                }

            }
            return result
        }


    }
}
