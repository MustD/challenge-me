import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0074 = (Array<IntArray>, Int) -> Boolean

//https://leetcode.com/problems/search-a-2d-matrix/description/?envType=study-plan-v2&envId=top-interview-150
class I0074searchA2dMatrix {
    @Suppress("ArrayInDataClass")
    data class Case(
        val matrix: Array<List<Int>>,
        val target: Int,
        val output: Boolean,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0074> {

        override val cases: List<Case> = listOf(
            Case(
                arrayOf(
                    listOf(1, 3, 5, 7),
                    listOf(10, 11, 16, 20),
                    listOf(23, 30, 34, 60)
                ), target = 3, output = true
            ),
            Case(
                arrayOf(
                    listOf(1, 3, 5, 7),
                    listOf(10, 11, 16, 20),
                    listOf(23, 30, 34, 60)
                ), target = 13, output = false
            ),
            Case(
                arrayOf(
                    listOf(1, 1)
                ), target = 0, output = false
            ),
            Case(
                arrayOf(
                    listOf(1),
                    listOf(3),
                ), target = 0, output = false
            )


        )

        override val solutions = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
        )

        override fun Case.check(solution: I0074): Pair<Boolean, Any> {
            val input = matrix.map { it.toIntArray() }.toTypedArray()
            val result = solution(input, target)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

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
