package leetcode.intervals

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toListOfIntLists
import org.junit.jupiter.api.Nested
import kotlin.test.Test


typealias I0452 = (Array<IntArray>) -> Int

class I0452MinimumNumberOfArrowsToBurstBalloons {
    data class Case(
        val input: List<List<Int>>,
        val output: Int,
    )

    fun prepareCase(s: String, r: Int) = Case(s.toListOfIntLists(), r)

    @Nested
    inner class Solution : AproblemTest<Case, I0452> {
        override val cases: List<Case> = listOf(
            prepareCase("[[10,16],[2,8],[1,6],[7,12]]", 2),
            prepareCase("[[1,2],[3,4],[5,6],[7,8]]", 4),
            prepareCase("[[1,2],[2,3],[3,4],[4,5]]", 2),
            prepareCase("[[3,9],[7,12],[3,8],[6,8],[9,10],[2,9],[0,9],[3,9],[0,6],[2,8]]", 2)
        )

        override val solutions = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0452): Pair<Boolean, Any> {
            val actual = solution(input.map { it.toIntArray() }.toTypedArray())
            val result = actual
            return (result == output) to result
        }

        @Test
        fun test() = check()

        //wrong
        fun solution1(points: Array<IntArray>): Int {
            println("--------------------")
            points.sortBy { it[1] }
            val result = mutableListOf<IntRange>()

            fun isIntersects(given: IntRange, with: IntRange): Boolean = when {
                given.first > with.last -> false
                given.last < with.first -> false
                else -> true
            }

            points.forEach { interval ->
                val range = interval[0]..interval[1]
                println("result: $result")
                println("interval: $range")

                var notFound = true
                for ((idx, section) in result.withIndex()) {
                    if (isIntersects(range, section)) {
                        println("given $range intersects with $section")
                        result[idx] = maxOf(range.first, section.first)..minOf(range.last, section.last)
                        notFound = false
                        break
                    }
                }
                if (notFound) {
                    result.add(range)
                }
            }
            return result.size
        }

        fun solutionEditorial(points: Array<IntArray>): Int {
            if (points.isEmpty()) return 0

            // sort by x_end
            points.sortBy { it[1] }

            var arrows = 1
            var xStart: Int
            var xEnd: Int
            var firstEnd = points[0][1]

            for (p in points) {
                xStart = p[0]
                xEnd = p[1]

                // If the current balloon starts after the end of another one,
                // one needs one more arrow
                if (firstEnd < xStart) {
                    arrows++
                    firstEnd = xEnd
                }
            }

            return arrows
        }

    }


}
