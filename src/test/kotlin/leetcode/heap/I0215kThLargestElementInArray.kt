package leetcode.heap

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test

typealias I0215 = (IntArray, Int) -> Int

class I0215kThLargestElementInArray {

    data class Case(
        val nums: List<Int>,
        val k: Int,
        val output: Int,
    )

    val prepareCase = { nums: String, k: Int, o: Int ->
        Case(nums.toIntArray().toList(), k, o)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0215> {

        override val cases: List<Case> = listOf(
            prepareCase("[3,2,1,5,6,4]", 2, 5),
            prepareCase("[3,2,3,1,2,4,5,5,6]", 4, 4),
        )

        override val solutions: List<Pair<String, I0215>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0215): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray(), k)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(nums: IntArray, k: Int): Int {
            nums.sort()
            return nums[nums.size - k]
        }

        fun solutionEditorial(nums: IntArray, k: Int): Int {
            val heap = PriorityQueue<Int>()
            nums.forEach {
                heap.add(it)
                if (heap.size > k) heap.poll()
            }
            return heap.peek()
        }

    }
}
