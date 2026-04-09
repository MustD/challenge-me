package leetcode.bit_manipulation

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0190 = (Int) -> Int

class I0190reverseBits {

    @Nested
    inner class Solution : ProblemTest<I0190> {

        override val cases = testCases<I0190>(
            43261596 expects 964176192,
            2147483644 expects 1073741822,
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(n: Int): Int {
            val str = n.toString(2).padStart(32, '0')
            val reversed = str.reversed()
            return reversed.toInt(2)
        }

    }
}
