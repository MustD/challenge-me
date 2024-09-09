import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * https://leetcode.com/problems/spiral-matrix-iv
 */
class I2326spiralMatrixIV {
    class ListNode(var `val`: Int) {
        var next: ListNode? = null
    }

    data class Case(
        val m: Int,
        val n: Int,
        val head: ListNode?,
        val output: Array<IntArray>,
    )

    val parseCase = { m: Int, n: Int, head: String, output: String ->
        val h = head.replace("[", "").replace("]", "").split(",").map {
            it.toInt()
        }.reversed().fold<Int, ListNode?>(null) { r, t ->
            ListNode(t).apply { next = r }
        }

        val a = output.split("],[").map {
            it.replace("[", "")
                .replace("]", "")
                .split(",")
                .map { i -> i.toInt() }.toIntArray()
        }.toTypedArray()

        Case(m, n, h, a)
    }

    @Nested
    inner class Solution : AproblemTest<Case, (Int, Int, ListNode?) -> Array<IntArray>> {
        override val cases: List<Case> = listOf(
            parseCase(3, 5, "[3,0,2,6,8,1,7,9,4,2,5,5,0]", "[[3,0,2,6,8],[5,0,-1,-1,1],[5,2,4,9,7]]"),
            parseCase(1, 4, "[0,1,2]", "[[0,1,2,-1]]"),
        )
        override val solutions: List<Pair<String, (Int, Int, ListNode?) -> Array<IntArray>>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: (Int, Int, ListNode?) -> Array<IntArray>): Pair<Boolean, Any> {
            val result = solution(m, n, head)
            val isCorrect = result.map { it.toList() }.toList() == output.map { it.toList() }.toList()
            return isCorrect to result.map { it.toList() }.toList()
        }

        @Test
        fun test() = check()

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