package leetcode.math

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0069 = (Int) -> Int

class I0069sqrt {

    @Nested
    inner class Solution : ProblemTest<I0069> {

        override val cases = testCases<I0069>(
            0 expects 0,
            1 expects 1,
            4 expects 2,
            8 expects 2,
            9 expects 3,
            2147395599 expects 46339,
        )

        @Test
        fun test() = check(::binarySearch, ::newton)

        // Binary search: find largest m where m*m <= x
        fun binarySearch(x: Int): Int {
            if (x < 2) return x
            var lo = 1L
            var hi = x.toLong() / 2
            var result = 0L
            while (lo <= hi) {
                val mid = lo + (hi - lo) / 2
                val sq = mid * mid
                when {
                    sq == x.toLong() -> return mid.toInt()
                    sq < x.toLong() -> {
                        result = mid; lo = mid + 1
                    }

                    else -> hi = mid - 1
                }
            }
            return result.toInt()
        }

        // Newton's method: iteratively refine guess until stable
        fun newton(x: Int): Int {
            if (x < 2) return x
            var g = x.toLong()
            while (g * g > x) {
                g = (g + x / g) / 2
            }
            return g.toInt()
        }

    }
}
