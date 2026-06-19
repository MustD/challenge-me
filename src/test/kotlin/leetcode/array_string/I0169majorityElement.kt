package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0169 = (IntArray) -> Int

class I0169majorityElement {

    @Nested
    inner class Solution : ProblemTest<I0169> {
        override val cases = testCases<I0169>(
            "[3,2,3]" expects 3,
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial, ::solutionEditorial2)

        fun solution1(nums: IntArray): Int {
            val count = mutableMapOf<Int, Int>()
            for (n in nums) {
                count[n] = (count.getOrDefault(n, 0) + 1).also {
                    if (it > nums.size / 2) return n
                }
            }
            return 0
        }

        fun solutionEditorial(nums: IntArray): Int {
            nums.sort()
            return nums[nums.size / 2]
        }

        //boyer-moore-voting-algorithm
        fun solutionEditorial2(nums: IntArray): Int {
            var count = 0
            var candidate = 0

            for (n in nums) {
                if (count == 0) {
                    candidate = n
                }
                count += if (n == candidate) 1 else -1
            }

            return candidate
        }

    }
}
