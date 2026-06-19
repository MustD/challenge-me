package leetcode.graph_bfs

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0909 = (Array<IntArray>) -> Int

//https://leetcode.com/problems/snakes-and-ladders/description/?envType=study-plan-v2&envId=top-interview-150
class I0909snakesAndLadders {

    @Nested
    inner class Solution : ProblemTest<I0909> {

        override val cases = testCases<I0909>(
            """
                [[-1,-1,-1,-1,-1,-1],[-1,-1,-1,-1,-1,-1],[-1,-1,-1,-1,-1,-1],
                 [-1,35,-1,-1,13,-1],[-1,-1,-1,-1,-1,-1],[-1,15,-1,-1,-1,-1]]
            """ expects 4,
            "[[-1,-1],[-1,3]]" expects 1,
            "[[1,1,-1],[1,1,1],[-1,1,1]]" expects -1,
            "[[-1,-1,-1],[-1,9,8],[-1,8,9]]" expects 1,
        )

        @Test
        fun test() = check(::solutionDFS, ::solutionBFS)

        private fun solutionDFS(board: Array<IntArray>): Int {
            val arr = board.toList().reversed()
                .flatMapIndexed { idx, it ->
                    if (idx % 2 == 0) it.toList() else it.reversed()
                }

            val dice = (1..6).toList()

            var min = arr.size * arr.size // max steps
            var solved = false

            fun roll(num: Int = 0, from: Int = 0, visited: Set<Int> = emptySet()) {
                if (num >= min) return // exit if length more than known min
                if (from > arr.lastIndex) return //exit on out of board

                val place = if (-1 == arr[from]) from else arr[from] - 1
                if (visited.contains(place)) return  //exit if we have already been there
                if (place == arr.lastIndex) {
                    min = minOf(min, num)
                    solved = true
                    return //exit when we reach end
                }
                dice.onEach {
                    roll(num + 1, place + it, visited.plus(place))
                }
            }
            roll()

            return if (solved) min else -1
        }

        private fun solutionBFS(board: Array<IntArray>): Int {
            val arr = board.toList().reversed()
                .flatMapIndexed { idx, it ->
                    if (idx % 2 == 0) it.toList() else it.reversed()
                }
            val dice = (1..6).toList()


            val queue = ArrayDeque<Pair<Int, Int>>()
            queue.addLast(0 to 0) //adding start position

            val toVisit = mutableSetOf<Int>()
            while (queue.isNotEmpty()) {
                val (start, throws) = queue.removeFirst()

                val place = if (-1 == arr[start]) start else arr[start] - 1 //snake or ladder
                if (place == arr.lastIndex) return throws //exit on final step

                dice.onEach { roll ->
                    val target = place + roll
                    if (!toVisit.add(target)) return@onEach // If already visited, skip
                    if (target > arr.lastIndex) return@onEach //out of range
                    queue.addLast(target to throws + 1)
                }
            }

            return -1
        }
    }
}
