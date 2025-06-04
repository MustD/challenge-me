package leetcode.matrix

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import leetcode.utils.ArrayUtils.toListOfIntLists
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0054 = (Array<IntArray>) -> List<Int>

class I0054spiralMatrix {
    data class Case(
        val matrix: List<List<Int>>,
        val output: List<Int>,
    )

    fun prepareCase(matrix: String, output: String): Case {
        return Case(
            matrix.toListOfIntLists(),
            output.toIntArray().toList(),
        )
    }

    @Nested
    inner class Solution : AproblemTest<Case, I0054> {

        override val cases: List<Case> = listOf(
            prepareCase("[[1,2,3],[4,5,6],[7,8,9]]", "[1,2,3,6,9,8,7,4,5]"),
            prepareCase("[[1,2,3,4],[5,6,7,8],[9,10,11,12]]", "[1,2,3,4,8,12,11,10,9,5,6,7]"),
            prepareCase("[[3],[2]]", "[3,2]")
        )

        override val solutions = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0054): Pair<Boolean, Any> {
            val result = solution(matrix.map { it.toIntArray() }.toTypedArray())
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

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


            matrix.forEach {
                println(it.toList())
            }

            var direction = if (matrix[0].size > 1) 'r' else 'd'
            repeat((1..(matrix.size * matrix[0].size)).count()) {
                result.add(coords.get())
                println("r: $result d: $direction")
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
