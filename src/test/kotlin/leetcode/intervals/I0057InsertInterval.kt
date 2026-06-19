package leetcode.intervals

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0057 = (Array<IntArray>, IntArray) -> Array<IntArray>

class I0057InsertInterval {

    @Nested
    inner class Solution : ProblemTest<I0057> {
        override val cases = testCases<I0057>(
            args("[[1,3],[6,9]]", "[2,5]") expects "[[1,5],[6,9]]",
            args("[[1,2],[3,5],[6,7],[8,10],[12,16]]", "[4,8]") expects "[[1,2],[3,10],[12,16]]",
            args("[]", "[5,7]") expects "[[5,7]]",
            args("[[1,5]]", "[2,3]") expects "[[1,5]]",
        )

        @Test
        fun test() = check(::solution1)

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
