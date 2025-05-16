package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0045 = (IntArray) -> Int

class I0045jumpGameII {

    data class Case(
        val nums: List<Int>,
        val output: Int,
    )

    val prepareCase = { n1: String, r: Int ->
        Case(n1.toIntArray().toList(), r)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0045> {
        override val cases: List<Case> = listOf(
            prepareCase("[2,3,1,1,4]", 2),
            prepareCase("[2,3,0,1,4]", 2),
            prepareCase("[9,8,2,2,0,2,2,0,4,1,5,7,9,6,6,0,6,5,0,5]", 3)
        )
        override val solutions: List<Pair<String, I0045>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0045): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()

        /**
         * MLE
         */
        fun solution1(nums: IntArray): Int {
            if (nums.size == 1) return 0
            val memo = MutableList<Int?>(nums.size) { null }
            memo[0] = 0

            val queue = ArrayDeque<Int>()
            queue.add(0)
            while (queue.isNotEmpty()) {
                val idx = queue.removeFirst()
                for (jump in nums[idx] downTo 1) {
                    val target = idx + jump //2
                    if (target >= nums.lastIndex) return (memo[idx] ?: 0) + 1 //if jump over the last index, return
                    memo[target] = minOf((memo[idx] ?: 0) + 1, memo[target] ?: Int.MAX_VALUE)
                    queue.add(target)
                }
            }
            return 0
        }

        fun solutionEditorial(nums: IntArray): Int {

            // The starting range of the first jump is [0, 0]
            var answer = 0
            var curEnd = 0
            var curFar = 0

            for (i in 0 .. nums.lastIndex - 1 ) {
                // Update the farthest reachable index of this jump.
                curFar = maxOf(curFar, i + nums[i])

                // If we finish the starting range of this jump,
                // Move on to the starting range of the next jump.
                if (i == curEnd) {
                    answer++
                    curEnd = curFar
                }
            }

            return answer
        }


    }
}
