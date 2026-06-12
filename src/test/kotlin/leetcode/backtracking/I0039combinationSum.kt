package leetcode.backtracking

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0039 = (IntArray, Int) -> List<List<Int>>

class I0039combinationSum {

    @Nested
    inner class Solution : ProblemTest<I0039> {

        override val cases = testCases<I0039>(
            args("[2,3,6,7]", 7) expects "[[2,2,3],[7]]",
            args("[2,3,5]", 8) expects "[[2,2,2,2],[2,3,3],[3,5]]",
            args("[2]", 1) expects "[]",
        )

        @Test
        fun test() = check(::combinationSum, ::combinationSumNoSort)

        fun combinationSum(candidates: IntArray, target: Int): List<List<Int>> {
            val result = mutableSetOf<List<Int>>()
            val state = mutableListOf<Int>()
            fun backtrack(sum: Int = 0) {
                if (sum > target) return
                if (sum == target) {
                    result.add(state.toList().sorted())
                    return
                }

                for (i in candidates.indices) {
                    state.addLast(candidates[i])
                    backtrack(sum + candidates[i])
                    state.removeLast()
                }
            }

            backtrack()
            return result.toList()
        }

        fun combinationSumNoSort(candidates: IntArray, target: Int): List<List<Int>> {
            val result = mutableSetOf<List<Int>>()
            val state = mutableListOf<Int>()
            fun backtrack(sum: Int = 0, fromIndx: Int = 0) {
                if (sum > target) return
                if (sum == target) {
                    result.add(state.toList())
                    return
                }

                for (i in fromIndx..candidates.lastIndex) {
                    state.addLast(candidates[i])
                    backtrack(sum + candidates[i], i)
                    state.removeLast()
                }
            }

            backtrack()
            return result.toList()
        }

    }
}
