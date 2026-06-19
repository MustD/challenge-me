package leetcode.heap

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test

typealias I0215 = (IntArray, Int) -> Int

class I0215kThLargestElementInArray {

    @Nested
    inner class Solution : ProblemTest<I0215> {

        override val cases = testCases<I0215>(
            args("[3,2,1,5,6,4]", 2) expects 5,
            args("[3,2,3,1,2,4,5,5,6]", 4) expects 4,
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial)

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
