package leetcode.graph_general

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0200 = (Array<CharArray>) -> Int

class I0200numberOfIslands {
    @Suppress("ArrayInDataClass")
    data class Case(
        val grid: Array<CharArray>,
        val output: Int,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0200> {

        override val cases: List<Case> = listOf(
            Case(
                arrayOf(
                    charArrayOf('1', '1', '1', '1', '0'),
                    charArrayOf('1', '1', '0', '1', '0'),
                    charArrayOf('1', '1', '0', '0', '0'),
                    charArrayOf('0', '0', '0', '0', '0'),
                ), 1
            ),
            Case(
                grid = arrayOf(
                    charArrayOf('1', '1', '0', '0', '0'),
                    charArrayOf('1', '1', '0', '0', '0'),
                    charArrayOf('0', '0', '1', '0', '0'),
                    charArrayOf('0', '0', '0', '1', '1'),
                ), 3
            )
        )

        override val solutions = listOf(
            ::solution1.name to ::solution1,
            ::solutionCommunity.name to ::solutionCommunity,
        )

        override fun Case.check(solution: I0200): Pair<Boolean, Any> {
            val clone = grid.map { it.clone() }.toTypedArray()
            val result = solution(clone)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solution1(grid: Array<CharArray>): Int {
            val visitedLand = mutableSetOf<Pair<Int, Int>>()

            fun walk(start: Pair<Int, Int>) {
                val (y, x) = start
                if (start in visitedLand) return
                val isLand = runCatching { grid[y][x] }.getOrDefault('0') == '1'
                if (!isLand) return else visitedLand.add(start)
                walk(y - 1 to x) //go up
                walk(y to x + 1) //go right
                walk(y + 1 to x) //go down
                walk(y to x - 1) //go left
            }

            var counter = 0
            grid.onEachIndexed y@{ y, xA ->
                xA.onEachIndexed x@{ x, _ ->
                    if (visitedLand.contains(y to x)) return@x
                    if (grid[y][x] == '0') return@x
                    walk(y to x)
                    counter++
                }
            }

            return counter
        }

        private fun solutionCommunity(grid: Array<CharArray>): Int {
            fun changeNearby(y: Int, x: Int) {
                grid[y][x] = '0'
                // top
                if (y - 1 in grid.indices && grid[y - 1][x] == '1') changeNearby(y - 1, x)
                // right
                if (x + 1 in grid[0].indices && grid[y][x + 1] == '1') changeNearby(y, x + 1)
                // down
                if (y + 1 in grid.indices && grid[y + 1][x] == '1') changeNearby(y + 1, x)
                // left
                if (x - 1 in grid[0].indices && grid[y][x - 1] == '1') changeNearby(y, x - 1)
            }

            var res = 0

            for (y in 0 until grid.size) {
                for (x in 0 until grid[0].size) {
                    if (grid[y][x] == '1') {
                        grid[y][x] = '0'
                        res++
                        changeNearby(y, x)
                    }
                }
            }

            return res
        }

    }
}
