package leetcode.intervals

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import leetcode.utils.ArrayUtils.toListOfIntLists
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0057 = (Array<IntArray>, IntArray) -> Array<IntArray>

class I0057InsertInterval {
    data class Case(
        val interval: List<List<Int>>,
        val new: List<Int>,
        val output: List<List<Int>>,
    )

    fun prepareCase(s: String, k: String, r: String) = Case(
        s.toListOfIntLists(),
        k.toIntArray().toList(),
        r.toListOfIntLists(),
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0057> {
        override val cases: List<Case> = listOf(
            prepareCase("[[1,3],[6,9]]", "[2,5]", "[[1,5],[6,9]]"),
            prepareCase("[[1,2],[3,5],[6,7],[8,10],[12,16]]", "[4,8]", "[[1,2],[3,10],[12,16]]"),
            prepareCase("[]", "[5,7]", "[[5,7]]"),
            prepareCase("[[1,5]]", "[2,3]", "[[1,5]]"),
        )

        override val solutions = listOf(
            ::solution1.name to ::solution1
        )

        override fun Case.check(solution: I0057): Pair<Boolean, Any> {
            val actual = solution(
                interval.map { it.toIntArray() }.toTypedArray(),
                new.toIntArray()
            )
            val result = actual.map { it.toList() }.toList()
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(intervals: Array<IntArray>, newInterval: IntArray): Array<IntArray> {
            if (intervals.isEmpty()) return arrayOf(newInterval)

            val result = mutableListOf<IntArray>()
            var new = newInterval[0]..newInterval[1]
            var done = false

            for (i in intervals) {
                val c = i[0]..i[1]
                if (c.last < new.first) {
                    result.add(intArrayOf(c.first, c.last))
                }
                if (new.last < c.first) {
                    if (done.not()) {
                        result.add(intArrayOf(new.first, new.last))
                        done = true
                    }
                    result.add(intArrayOf(c.first, c.last))
                }
                if (c.last >= new.first) {
                    new = minOf(c.first, new.first)..maxOf(new.last, c.last)
                }
            }
            if (done.not()) {
                result.add(intArrayOf(new.first, new.last))
            }
            return result.toTypedArray()
        }

    }

}
