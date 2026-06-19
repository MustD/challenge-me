package leetcode.intervals

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test


typealias I0452 = (Array<IntArray>) -> Int

class I0452MinimumNumberOfArrowsToBurstBalloons {

    @Nested
    inner class Solution : ProblemTest<I0452> {
        override val cases = testCases<I0452>(
            "[[10,16],[2,8],[1,6],[7,12]]" expects 2,
            "[[1,2],[3,4],[5,6],[7,8]]" expects 4,
            "[[1,2],[2,3],[3,4],[4,5]]" expects 2,
            "[[3,9],[7,12],[3,8],[6,8],[9,10],[2,9],[0,9],[3,9],[0,6],[2,8]]" expects 2,
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial)

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
