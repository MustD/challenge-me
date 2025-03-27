import org.junit.jupiter.api.Nested
import kotlin.test.Test


typealias I0909 = (Array<IntArray>) -> Int

//https://leetcode.com/problems/snakes-and-ladders/description/?envType=study-plan-v2&envId=top-interview-150
class I0909snakesAndLadders {
    @Suppress("ArrayInDataClass")
    data class Case(
        val board: Array<IntArray>,
        val output: Int,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0909> {

        override val cases: List<Case> = listOf(
            Case(
                arrayOf(
                    intArrayOf(-1, -1, -1, -1, -1, -1),
                    intArrayOf(-1, -1, -1, -1, -1, -1),
                    intArrayOf(-1, -1, -1, -1, -1, -1),
                    intArrayOf(-1, 35, -1, -1, 13, -1),
                    intArrayOf(-1, -1, -1, -1, -1, -1),
                    intArrayOf(-1, 15, -1, -1, -1, -1)
                ),
                4
            ),
            Case(
                arrayOf(
                    intArrayOf(-1, -1),
                    intArrayOf(-1, 3),
                ),
                1
            ),
            //dfs killer case
            /*Case(
                arrayOf(
                    intArrayOf(1, 1, -1),
                    intArrayOf(1, 1, 1),
                    intArrayOf(-1, 1, 1)
                ), 4
            )*/
        )

        override val solutions = listOf(
            ::solutionDebug.name to ::solutionDebug,
            ::solution1.name to ::solution1,
//            ::solutionCommunity.name to ::solutionCommunity,  //not working
        )

        override fun Case.check(solution: I0909): Pair<Boolean, Any> {
            val clone = board.map { it.clone() }.toTypedArray()
            val result = solution(clone)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solutionDebug(board: Array<IntArray>): Int {
            val arr = board.toList().reversed()
                .flatMapIndexed { idx, it ->
                    if (idx % 2 == 0) it.toList() else it.reversed()
                }

            val dice = (1..6).toList()
            println((0..arr.lastIndex).map { "$it".padStart(3) })
            println(arr.map { "$it".padStart(3) })

            var min = arr.size * arr.size // max steps
            var log: List<Int> = emptyList()

            fun roll(num: Int = 0, from: Int = 0, rolls: List<Int> = emptyList()) {
                if (num >= min) return // exit if length more than known min
                if (from > arr.lastIndex) return //exit on out of board

                val place = if (-1 == arr[from]) from else arr[from] - 1
                if (place == arr.lastIndex) {
                    min = minOf(min, num)
                    log = rolls
                    return //exit when we reach end
                }
                dice.onEach {
                    roll(num + 1, place + it, rolls.plus(it))
                }
            }
            roll()

            println(log)
            return min
        }

        private fun solution1(board: Array<IntArray>): Int {
            val arr = board.toList().reversed()
                .flatMapIndexed { idx, it ->
                    if (idx % 2 == 0) it.toList() else it.reversed()
                }

            val dice = (1..6).toList()

            var min = arr.size * arr.size // max steps

            fun roll(num: Int = 0, from: Int = 0) {
                if (num >= min) return // exit if length more than known min
                if (from > arr.lastIndex) return //exit on out of board

                val place = if (-1 == arr[from]) from else arr[from] - 1
                if (place == arr.lastIndex) {
                    min = minOf(min, num)
                    return //exit when we reach end
                }
                dice.onEach {
                    roll(num + 1, place + it)
                }
            }
            roll()

            return min
        }

        fun solutionCommunity(board: Array<IntArray>): Int {
            fun col(pos: Int): Int {
                return if (((pos / board.size) % 2) == 0)
                    (pos % board.size)
                else
                    (board.lastIndex - (pos % board.size))
            }

            val last = board.size * board.size
            var steps = 0
            val visited = mutableSetOf<Int>()
            with(ArrayDeque<Int>().apply { add(1) }) {
                while (isNotEmpty() && steps <= last) {
                    repeat(size) {
                        var curr = removeFirst()
                        val jump = board[board.lastIndex - (curr - 1) / board.size][col(curr - 1)]
                        if (jump != -1) curr = jump
                        if (curr == last) return steps
                        for (i in 1..6)
                            if (visited.add(curr + i) && curr + i <= last) add(curr + i)
                    }
                    steps++
                }
            }
            return -1
        }


    }
}
