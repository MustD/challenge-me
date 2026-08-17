package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0135 = (IntArray) -> Int

class I0135candy {

    @Nested
    inner class Solution : ProblemTest<I0135> {
        override val cases = testCases<I0135>(
            "[1,0,2]" expects 5,
            "[1,2,2]" expects 4,
        )

        @Test
        fun test() = check(::solutionEditorial)


        fun solutionEditorial(ratings: IntArray): Int {
            val left = IntArray(ratings.size) { 1 }
            for (i in 1..ratings.lastIndex) {
                if (ratings[i] > ratings[i - 1]) left[i] = left[i - 1] + 1
            }
            val right = IntArray(ratings.size) { 1 }
            for (i in ratings.lastIndex - 1 downTo 0) {
                if (ratings[i] > ratings[i + 1]) right[i] = right[i + 1] + 1
            }
            return (0..ratings.lastIndex).sumOf { maxOf(left[it], right[it]) }
        }


    }
}
