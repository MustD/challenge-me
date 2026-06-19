package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0045 = (IntArray) -> Int

class I0045jumpGameII {

    @Nested
    inner class Solution : ProblemTest<I0045> {
        override val cases = testCases<I0045>(
            "[2,3,1,1,4]" expects 2,
            "[2,3,0,1,4]" expects 2,
            "[9,8,2,2,0,2,2,0,4,1,5,7,9,6,6,0,6,5,0,5]" expects 3,
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial)

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

            for (i in 0..nums.lastIndex - 1) {
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
