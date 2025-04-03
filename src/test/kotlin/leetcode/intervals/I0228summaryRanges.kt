package leetcode.intervals

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0228 = (IntArray) -> List<String>

class I0228summaryRanges {
    data class Case(
        val input: IntArray,
        val output: List<String>,
    )

    fun prepareCase(s: String, r: List<String>) = Case(s.toIntArray(), r)

    @Nested
    inner class Solution : AproblemTest<Case, I0228> {
        override val cases: List<Case> = listOf(
            prepareCase("[0,1,2,4,5,7]", listOf("0->2", "4->5", "7")),
            prepareCase("[0,2,3,4,6,8,9]", listOf("0", "2->4", "6", "8->9")),
        )

        override val solutions = listOf(
            ::solution1.name to ::solution1
        )

        override fun Case.check(solution: I0228): Pair<Boolean, Any> {
            val result = solution(input)
            return (result.sorted() == output.sorted()) to result
        }

        @Test
        fun test() = check()

        fun solution1(nums: IntArray): List<String> {
            if (nums.isEmpty()) return emptyList()

            val result = mutableListOf<String>()
            fun Pair<Int, Int>.toRange() = if (first == second) first.toString() else "$first->$second"

            var current = Pair(nums[0], nums[0])
            for (i in 1..nums.lastIndex) {
                val value = nums[i]

                if (current.second + 1 == value) {
                    current = Pair(current.first, value)
                } else {
                    result.add(current.toRange())
                    current = Pair(value, value)
                }
            }
            result.add(current.toRange())

            return result
        }

    }

}
