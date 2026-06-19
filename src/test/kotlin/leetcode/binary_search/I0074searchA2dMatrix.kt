package leetcode.binary_search

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0074 = (Array<IntArray>, Int) -> Boolean

//https://leetcode.com/problems/search-a-2d-matrix/description/?envType=study-plan-v2&envId=top-interview-150
class I0074searchA2dMatrix {

    @Nested
    inner class Solution : ProblemTest<I0074> {

        override val cases = testCases<I0074>(
            args("[[1,3,5,7],[10,11,16,20],[23,30,34,60]]", 3) expects true,
            args("[[1,3,5,7],[10,11,16,20],[23,30,34,60]]", 13) expects false,
            args("[[1,1]]", 0) expects false,
            args("[[1],[3]]", 0) expects false,
        )

        @Test
        fun test() = check(::solution1, ::solution2)

        /**
         * - Time Complexity: O(m * log n)
         * - Space Complexity: O(log n)
         */
        private fun solution1(matrix: Array<IntArray>, target: Int): Boolean {
            fun binS(s: Int, e: Int, t: Int, aIdx: Int): Boolean {
                if (s >= e) return matrix[aIdx][s] == t
                val mid = (s + e) / 2
                if (matrix[aIdx][mid] == t) return true
                return if (matrix[aIdx][mid] > t) {
                    binS(s, mid - 1, t, aIdx) //up part search
                } else {
                    binS(mid + 1, e, t, aIdx) //down part search
                }
            }
            matrix.forEachIndexed { aIdx, ints ->
                if (target < matrix[aIdx][0]) return@forEachIndexed
                if (binS(0, ints.lastIndex, target, aIdx)) return true
            }
            return false
        }


        /**
         * - Time Complexity: O(log(m×n))
         * - Space Complexity: O(log(m×n))
         */
        private fun solution2(matrix: Array<IntArray>, target: Int): Boolean {
            fun binS(s: Int, e: Int, aIdx: Int): Boolean {
                if (s >= e) return matrix[aIdx][s] == target
                val mid = (s + e) / 2
                if (matrix[aIdx][mid] == target) return true
                return if (matrix[aIdx][mid] > target) {
                    binS(s, mid - 1, aIdx) //up part search
                } else {
                    binS(mid + 1, e, aIdx) //down part search
                }
            }

            fun mBinS(s: Int, e: Int): Boolean {
                if (s >= e) return binS(0, matrix[s].lastIndex, s)
                val mIdx = (s + e) / 2

                val midS = matrix[mIdx][0]
                val midE = matrix[mIdx][matrix[mIdx].lastIndex]
                if (midS == target) return true
                if (midE == target) return true

                if ((target > midS).and(target < midE)) {
                    return binS(0, matrix[mIdx].lastIndex, mIdx)
                }
                return if (target > matrix[mIdx][matrix[mIdx].lastIndex]) {
                    mBinS(mIdx + 1, e) //matrix up
                } else {
                    mBinS(s, mIdx - 1) //matrix down
                }
            }

            return mBinS(0, matrix.lastIndex)
        }


    }
}
