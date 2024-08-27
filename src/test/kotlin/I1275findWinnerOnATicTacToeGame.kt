import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * https://leetcode.com/problems/find-winner-on-a-tic-tac-toe-game
 */
class I1275findWinnerOnATicTacToeGame {
    data class Case(
        val input: Array<IntArray>,
        val output: String,
    )

    val parseCase = { moves: String, result: String ->
        val a = moves.split("],[").map {
            it.replace("[", "")
                .replace("]", "")
                .split(",")
                .map { i -> i.toInt() }.toIntArray()
        }.toTypedArray()
        Case(a, result)
    }

    @Nested
    inner class Solution : AproblemTest<Case, (Array<IntArray>) -> String> {
        override val cases: List<Case> = listOf(
            parseCase("[[0,0],[2,0],[1,1],[2,1],[2,2]]", "A"),
            parseCase("[[0,0],[1,1],[0,1],[0,2],[1,0],[2,0]]", "B"),
            parseCase("[[0,0],[1,1],[2,0],[1,0],[1,2],[2,1],[0,1],[0,2],[2,2]]", "Draw"),
            parseCase("[[0,0],[1,1]]", "Pending"),
            parseCase("[[1,2],[2,1],[1,0],[0,0],[0,1],[2,0],[1,1]]", "A")
        )

        override val solutions: List<Pair<String, (Array<IntArray>) -> String>> = listOf(
            "I" to ::solution,
        )

        override fun Case.check(solution: (Array<IntArray>) -> String): Pair<Boolean, Any> {
            val result = solution(input)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        fun solution(moves: Array<IntArray>): String {
            fun ifWinner(state: Int): Boolean {
                val wins = listOf(
                    0b100100100, 0b010010010, 0b001001001,
                    0b111000000, 0b000111000, 0b000000111,
                    0b100010001, 0b001010100,
                )
                return wins.any { it and state == it }
            }

            val cells = mapOf(
                listOf(0, 0) to 0b100000000,
                listOf(0, 1) to 0b010000000,
                listOf(0, 2) to 0b001000000,
                listOf(1, 0) to 0b000100000,
                listOf(1, 1) to 0b000010000,
                listOf(1, 2) to 0b000001000,
                listOf(2, 0) to 0b000000100,
                listOf(2, 1) to 0b000000010,
                listOf(2, 2) to 0b000000001,
            )
            var aState = 0b000000000
            var bState = 0b000000000
            moves.forEachIndexed { index, move ->
                val moveMask = checkNotNull(cells[move.toList()])
                if (index % 2 == 0) {
                    aState = aState or moveMask
                } else {
                    bState = bState or moveMask
                }
                when {
                    ifWinner(aState) -> return "A"
                    ifWinner(bState) -> return "B"
                    index == cells.size - 1 -> return "Draw"
                }
            }
            return "Pending"
        }

    }
}