package leetcode.hash_map

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test


typealias I0128 = (IntArray) -> Int

class I0128LongestConsecutiveSequence {
    data class Case(
        val nums: List<Int>,
        val output: Int,
    )

    val prepareCase = { nums: String, output: Int ->
        Case(nums.toIntArray().toList(), output)
    }

    @Nested
    inner class Solution : AproblemTest<Case, I0128> {

        override val cases: List<Case> = listOf(
            prepareCase("[100,4,200,1,3,2]", 4),
            prepareCase("[0,3,7,2,5,8,4,6,0,1]", 9),
            prepareCase("[1,0,1,2]", 3),
        )

        override val solutions: List<Pair<String, I0128>> = listOf(
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0128): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()

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
