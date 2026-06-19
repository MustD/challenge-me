package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0274 = (IntArray) -> Int

class I0274hIndex {

    @Nested
    inner class Solution : ProblemTest<I0274> {
        override val cases = testCases<I0274>(
            "[3,0,6,1,5]" expects 3,
            "[1,3,1]" expects 1,
        )

        @Test
        fun test() = check(::solution1)


        fun solution1(citations: IntArray): Int {
            citations.sortDescending()
            var i = 0
            while (i <= citations.lastIndex && citations[i] > i) {
                i++
            }
            return i
        }


    }
}
