package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0274 = (IntArray) -> Int

class I0274hIndex {

    data class Case(
        val nums: List<Int>,
        val output: Int,
    )

    val prepareCase = { n1: String, r: Int ->
        Case(n1.toIntArray().toList(), r)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0274> {
        override val cases: List<Case> = listOf(
            prepareCase("[3,0,6,1,5]", 3),
            prepareCase("[1,3,1]", 1)
        )
        override val solutions: List<Pair<String, I0274>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0274): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()


        fun solution1(citations: IntArray): Int {
            citations.sortDescending()
            var i = 0
            while (i <= citations.lastIndex && citations[i] > i) {
                i++
            }
            return i
        }


    }
}
