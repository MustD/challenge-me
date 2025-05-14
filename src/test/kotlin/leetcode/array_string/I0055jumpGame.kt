package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0055 = (IntArray) -> Boolean

class I0055jumpGame {

    data class Case(
        val nums: List<Int>,
        val output: Boolean,
    )

    val prepareCase = { n1: String, r: Boolean ->
        Case(n1.toIntArray().toList(), r)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0055> {
        override val cases: List<Case> = listOf(
            prepareCase("[2,3,1,1,4]", true),
            prepareCase("[3,2,1,0,4]", false),
            prepareCase("[2,0]", true)
        )
        override val solutions: List<Pair<String, I0055>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0055): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()

        /**
         * TLE
         */
        fun solution1(nums: IntArray): Boolean {
            val end = nums.lastIndex
            fun jump(idx: Int = 0): Boolean {
                if (idx == end) return true
                if (idx > end) return false
                if (nums[idx] == 0) return false

                for (i in 1..nums[idx]) {
                    if (i + idx == end) return true
                    if (idx + i > end) continue //not check if out of bound
                    if (nums[i + idx] < nums[idx] - i) continue //not check if it can reach from the current
                    if (jump(idx + i)) return true
                }
                return false
            }
            return jump()
        }

        /**
         * DP Top-down
         */
        fun solutionEditorial(nums: IntArray): Boolean {
            val memo = MutableList<Boolean?>(nums.size) { null }
            memo[nums.lastIndex] = true //setting the last one as good

            fun canJump(pos: Int = 0): Boolean {
                memo[pos]?.let { return it } //if we know the result, return it
                val furthest = minOf(pos + nums[pos], nums.lastIndex)
                for (next in furthest downTo pos + 1) {
                    if (canJump(next)) {
                        memo[pos] = true
                        return true
                    }
                }
                memo[pos] = false
                return false
            }

            return canJump()
        }

    }
}
