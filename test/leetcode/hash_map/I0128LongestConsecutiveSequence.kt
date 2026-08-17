package leetcode.hash_map

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test


typealias I0128 = (IntArray) -> Int

class I0128LongestConsecutiveSequence {

    @Nested
    inner class Solution : ProblemTest<I0128> {

        override val cases = testCases<I0128>(
            "[100,4,200,1,3,2]" expects 4,
            "[0,3,7,2,5,8,4,6,0,1]" expects 9,
            "[1,0,1,2]" expects 3,
        )

        @Test
        fun test() = check(::solutionEditorial)

        private fun solutionEditorial(nums: IntArray): Int {
            val set = nums.toSet()
            var result = 0
            for (i in set) {
                if (set.contains(i - 1)) continue
                var current = i
                var streak = 0
                while (set.contains(current)) {
                    streak++
                    current++
                }
                result = maxOf(result, streak)
            }
            return result
        }


    }
}
