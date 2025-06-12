package leetcode.intervals

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toListOfIntLists
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0056 = (Array<IntArray>) -> Array<IntArray>

class I0056MergeIntervals {
    data class Case(
        val input: List<List<Int>>,
        val output: List<List<Int>>,
    )

    fun prepareCase(s: String, r: String) = Case(s.toListOfIntLists(), r.toListOfIntLists())

    @Nested
    inner class Solution : AproblemTest<Case, I0056> {
        override val cases: List<Case> = listOf(
            prepareCase("[[1,3],[2,6],[8,10],[15,18]]", "[[1,6],[8,10],[15,18]]"),
            prepareCase("[[1,4],[4,5]]", "[[1,5]]"),
            prepareCase("[[1,4],[0,4]]", "[[0,4]]"),
            prepareCase("[[1,4],[0,1]]", "[[0,4]]"),
            prepareCase("[[1,4],[0,0]]", "[[0,0],[1,4]]")
        )

        override val solutions = listOf(
            ::solution1.name to ::solution1
        )

        override fun Case.check(solution: I0056): Pair<Boolean, Any> {
            val actual = solution(input.map { it.toIntArray() }.toTypedArray())
            val result = actual.map { it.toList() }.toList()
            return (result == output) to result
        }

        @Test
        fun test() = check()

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
