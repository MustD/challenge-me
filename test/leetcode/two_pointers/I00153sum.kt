package leetcode.two_pointers

import leetcode.ProblemTest
import leetcode.expectsAnyOrder
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0015 = (IntArray) -> List<List<Int>>

class I00153sum {

    @Nested
    inner class Solution : ProblemTest<I0015> {
        override val cases = testCases<I0015>(
            "-1,0,1,2,-1,-4]" expectsAnyOrder "[[-1,-1,2],[-1,0,1]]",
            "[0,1,1]" expectsAnyOrder "[]",
            "[0,0,0]" expectsAnyOrder "[[0,0,0]]",
        )

        @Test
        fun test() = check(::solutionEditorial)


        fun solutionEditorial(nums: IntArray): List<List<Int>> {
            nums.sort()
            val result = mutableListOf<List<Int>>()
            for (i in 0..nums.lastIndex) {
                if (nums[i] > 0) break
                if (i == 0 || nums[i - 1] != nums[i]) {
                    var lo = i + 1
                    var hi = nums.lastIndex
                    while (lo < hi) {
                        val sum = nums[i] + nums[lo] + nums[hi]
                        if (sum < 0) {
                            lo++
                        } else if (sum > 0) {
                            hi--
                        } else {
                            result.add(listOf(nums[i], nums[lo], nums[hi]))
                            lo++
                            hi--
                            while (lo < hi && nums[lo] == nums[lo - 1]) {
                                lo++
                            }
                        }
                    }
                }
            }

            return result
        }


    }
}
