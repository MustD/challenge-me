package leetcode.intervals

import leetcode.ProblemTest
import leetcode.expectsAnyOrder
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0228 = (IntArray) -> List<String>

class I0228summaryRanges {

    @Nested
    inner class Solution : ProblemTest<I0228> {
        // Old harness compared `result.sorted() == output.sorted()` (order-insensitive).
        override val cases = testCases<I0228>(
            "[0,1,2,4,5,7]" expectsAnyOrder """["0->2","4->5","7"]""",
            "[0,2,3,4,6,8,9]" expectsAnyOrder """["0","2->4","6","8->9"]""",
        )

        @Test
        fun test() = check(::solution1)

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
