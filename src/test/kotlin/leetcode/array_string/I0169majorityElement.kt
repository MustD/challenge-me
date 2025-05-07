package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0169 = (IntArray) -> Int

class I0169majorityElement {

    data class Case(
        val nums: List<Int>,
        val output: Int,
    )

    val prepareCase = { n1: String, r: Int ->
        Case(n1.toIntArray().toList(), r)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0169> {
        override val cases: List<Case> = listOf(
            prepareCase("[3,2,3]", 3)
        )
        override val solutions: List<Pair<String, I0169>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
            ::solutionEditorial2.name to ::solutionEditorial2,
        )

        override fun Case.check(solution: I0169): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()

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
