package leetcode

import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I2326 = (Int, Int, ListNode?) -> Array<IntArray>

/**
 * https://leetcode.com/problems/spiral-matrix-iv
 */
class I2326spiralMatrixIV {

    @Nested
    inner class Solution : ProblemTest<I2326> {
        override val cases = testCases<I2326>(
            args(3, 5, "[3,0,2,6,8,1,7,9,4,2,5,5,0]") expects "[[3,0,2,6,8],[5,0,-1,-1,1],[5,2,4,9,7]]",
            args(1, 4, "[0,1,2]") expects "[[0,1,2,-1]]",
        )

        @Test
        fun test() = check(::solution1)

        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION") //leetcode seems to have old kotlin...
        private fun solution1(m: Int, n: Int, head: ListNode?): Array<IntArray> {
            var maxX = n - 1
            var maxY = m - 1
            var minX = 0
            var minY = 0
            val field = (0 until m).map { IntArray(n) { -1 } }.toTypedArray()

            var vector = 1 to 0
            var position = 0 to maxY
            var node = head
            while (node != null) {
                position.let { (x, y) ->
                    field[y][x] = node!!.`val`
                    node = node!!.next
                }
                (position.first + vector.first to position.second + vector.second).let { (nx, ny) ->
                    when {
                        vector.first != 0 && nx > maxX -> {
                            vector = 0 to -1
                            maxY--
                        }

                        vector.second != 0 && ny < minY -> {
                            vector = -1 to 0
                            maxX--
                        }

                        vector.first != 0 && nx < minX -> {
                            vector = 0 to 1
                            minY++
                        }

                        vector.second != 0 && ny > maxY -> {
                            vector = 1 to 0
                            minX++
                        }

                        else -> Unit
                    }
                }
                position = (position.first + vector.first to position.second + vector.second)
            }
            return field.reversed().toTypedArray()
        }
    }
}
