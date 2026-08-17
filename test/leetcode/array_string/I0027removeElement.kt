package leetcode.array_string

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0027 = (IntArray, Int) -> Int

class I0027removeElement {

    @Nested
    inner class Solution : ProblemTest<I0027> {

        override val cases = testCases<I0027>(
            args("[3,2,2,3]", 3) expects 2,
            args("[0,1,2,2,3,0,4,2]", 2) expects 5,
        )

        @Test
        fun test() = check(::solution1, ::solutionCommunity)

        fun solution1(nums: IntArray, `val`: Int): Int {
            if (nums.isEmpty()) return 0

            var start = 0
            var end = nums.lastIndex
            var result = 0

            while (start <= end) {
                if (nums[end] == `val`) {
                    end--
                    continue
                }

                if (nums[start] == `val`) {
                    nums[start] = nums[end]
                    end--
                    start++
                } else {
                    start++
                }

                result++
            }

            return result
        }

        fun solutionCommunity(nums: IntArray, `val`: Int): Int {
            var k = 0 // Pointer for the position to store elements not equal to `val`

            for (i in nums.indices) {
                // Only move elements that are not equal to `val`
                if (nums[i] != `val`) {
                    nums[k] = nums[i] // Move the valid element to the `k`-th position
                    k++
                }
            }

            return k // Return the count of elements not equal to `val`
        }


    }
}
