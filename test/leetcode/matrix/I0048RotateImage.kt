package leetcode.matrix

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

// LeetCode's signature is `(Array<IntArray>) -> Unit`: the solution rotates `matrix` in place
// and returns nothing. The ProblemTest harness asserts on the *return value*, so an in-place
// mutation would be invisible. To make the rotation checkable we return the (mutated) input
// matrix instead of Unit. To match LeetCode exactly, drop the `: Array<IntArray>` return type
// and the trailing `return matrix`.
typealias I0048 = (Array<IntArray>) -> Array<IntArray>

class I0048RotateImage {

    @Nested
    inner class Solution : ProblemTest<I0048> {

        override val cases = testCases<I0048>(
            args("[[1,2,3],[4,5,6],[7,8,9]]") expects "[[7,4,1],[8,5,2],[9,6,3]]",
            args("[[5,1,9,11],[2,4,8,10],[13,3,6,7],[15,14,12,16]]")
                    expects "[[15,13,2,5],[14,3,4,1],[12,6,8,9],[16,7,10,11]]",
        )

        @Test
        fun test() = check(::solutionEditorial)

        private fun solutionEditorial(matrix: Array<IntArray>): Array<IntArray> {
            val s: Int = matrix.size
            val li = matrix.lastIndex
            for (i in 0..<(s + 1) / 2) {
                for (j in 0..<s / 2) {

                    val temp = matrix[li - j][i]
                    matrix[li - j][i] = matrix[li - i][li - j]
                    matrix[li - i][li - j] = matrix[j][li - i]
                    matrix[j][li - i] = matrix[i][j]
                    matrix[i][j] = temp
                }
            }
            return matrix
        }


    }
}
