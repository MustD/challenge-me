package leetcode.intervals

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0056 = (Array<IntArray>) -> Array<IntArray>

class I0056MergeIntervals {

    @Nested
    inner class Solution : ProblemTest<I0056> {
        override val cases = testCases<I0056>(
            "[[1,3],[2,6],[8,10],[15,18]]" expects "[[1,6],[8,10],[15,18]]",
            "[[1,4],[4,5]]" expects "[[1,5]]",
            "[[1,4],[0,4]]" expects "[[0,4]]",
            "[[1,4],[0,1]]" expects "[[0,4]]",
            "[[1,4],[0,0]]" expects "[[0,0],[1,4]]",
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(intervals: Array<IntArray>): Array<IntArray> {
            intervals.sortBy { it[0] }
            val result = mutableListOf<IntArray>()
            var current = intervals[0]
            for (i in 1..intervals.lastIndex) {
                if (intervals[i][0] <= current[1]) {
                    current[0] = minOf(current[0], intervals[i][0])
                    current[1] = maxOf(current[1], intervals[i][1])
                } else {
                    result.add(current)
                    current = intervals[i]
                }
            }
            result.add(current)
            return result.toTypedArray()
        }

    }

}
