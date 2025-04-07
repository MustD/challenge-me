package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0027 = (IntArray, Int) -> Int

class I0027removeElement {

    data class Case(
        val nums: List<Int>,
        val input: Int,
        val output: Int,
    )

    val prepareCase = { nums: String, i: Int, o: Int ->
        Case(nums.toIntArray().toList(), i, o)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0027> {

        override val cases: List<Case> = listOf(
            prepareCase("[3,2,2,3]", 3, 2),
            prepareCase("[0,1,2,2,3,0,4,2]", 2, 5),
        )

        override val solutions: List<Pair<String, I0027>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionCommunity.name to ::solutionCommunity,
        )

        override fun Case.check(solution: I0027): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray(), input)
            return (result == output) to result
        }

        @Test
        fun test() = check()

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
