package leetcode.matrix

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

// LeetCode's signature is `(Array<IntArray>) -> Unit`: the solution zeroes out rows/cols in
// `matrix` in place and returns nothing. The ProblemTest harness asserts on the *return value*,
// so we return the (mutated) input matrix instead of Unit to make the result checkable. To match
// LeetCode exactly, drop the `: Array<IntArray>` return type and the trailing `return matrix`.
typealias I0073 = (Array<IntArray>) -> Array<IntArray>

class I0073SetMatrixZeroes {

    @Nested
    inner class Solution : ProblemTest<I0073> {

        override val cases = testCases<I0073>(
            args("[[1,1,1],[1,0,1],[1,1,1]]") expects "[[1,0,1],[0,0,0],[1,0,1]]",
            args("[[0,1,2,0],[3,4,5,2],[1,3,1,5]]") expects "[[0,0,0,0],[0,4,5,0],[0,3,1,0]]",
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(matrix: Array<IntArray>): Array<IntArray> {
            val zRows = mutableSetOf<Int>()
            val zCols = mutableSetOf<Int>()
            for (y in matrix.indices) {
                for (x in matrix[y].indices) {
                    if (matrix[y][x] == 0) {
                        zRows.add(y)
                        zCols.add(x)
                    }
                }
            }
            for (y in matrix.indices) {
                for (x in matrix[y].indices) {
                    if (y in zRows || x in zCols) {
                        matrix[y][x] = 0
                    }
                }
            }
            return matrix
        }
    }
}
