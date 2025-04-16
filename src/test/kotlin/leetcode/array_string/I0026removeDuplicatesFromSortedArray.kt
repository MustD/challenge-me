package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0026 = (IntArray) -> Int

class I0026removeDuplicatesFromSortedArray {

    data class Case(
        val nums: List<Int>,
        val output: Int,
    )

    val prepareCase = { nums: String, o: Int ->
        Case(nums.toIntArray().toList(), o)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0026> {

        override val cases: List<Case> = listOf(
            prepareCase("[1,1,2]", 2),
            prepareCase("[0,0,1,1,1,2,2,3,3,4]", 5),
        )

        override val solutions: List<Pair<String, I0026>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0026): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(nums: IntArray): Int {
            var insertIdx  = 1
            for (i in 1 .. nums.lastIndex) {
                if (nums[i] != nums[i - 1]) {
                    nums[insertIdx] = nums[i]
                    insertIdx++
                }
            }
            return insertIdx
        }


    }
}
