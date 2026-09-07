package coderun

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias T0000 = (Int) -> Int

class _Template {

    @Nested
    inner class Solution : ProblemTest<T0000> {

        override val cases = testCases<T0000>(
            0 expects 0,
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(n: Int): Int {
            return n
        }

    }
}
