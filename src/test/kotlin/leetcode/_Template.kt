package leetcode.TODO_category

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0000 = (Int) -> Int

class I0000problem {

    @Nested
    inner class Solution : ProblemTest<I0000> {

        override val cases = testCases<I0000>(
            0 expects 0,
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(n: Int): Int {
            return n
        }

    }
}
