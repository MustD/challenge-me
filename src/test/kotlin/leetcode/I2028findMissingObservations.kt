package leetcode

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class I2028findMissingObservations {
    data class Case(
        val rolls: IntArray,
        val mean: Int,
        val n: Int,
        val output: IntArray,
    )

    val parseCase = { rolls: String, mean: Int, n: Int, output: String ->
        val fromString = { str: String ->
            if (str == "[]") {
                intArrayOf()
            } else {
                str.replace("[", "").replace("]", "").split(",").map { it.toInt() }.toIntArray()
            }
        }
        Case(fromString(rolls), mean, n, fromString(output))
    }

    @Nested
    inner class Solution : AproblemTest<Case, (IntArray, Int, Int) -> IntArray> {

        override val cases: List<Case> = listOf(
            parseCase("[3,2,4,3]", 4, 2, "[6,6]"),
            parseCase("[1,5,6]", 3, 4, "[2,3,2,2]"),
            parseCase("[1,2,3,4]", 6, 4, "[]"),
        )

        override val solutions: List<Pair<String, (IntArray, Int, Int) -> IntArray>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: (IntArray, Int, Int) -> IntArray): Pair<Boolean, Any> {
            val result = solution(rolls, mean, n)
            if (output.toList().sorted() == result.toList().sorted()) return true to result.toList()
            if (((result + rolls).sum() / (rolls.size + n)) == mean) return true to result.toList()
            return false to result.toList()
        }

        @Test
        fun test() = check()

        fun solution1(rolls: IntArray, mean: Int, n: Int): IntArray {
            val array = IntArray(n) { mean }

            val size = rolls.size + n
            var sum = (array + rolls).sum().toFloat()
            val mod = if ((sum / size) > mean) -1 else 1

            var i = 0
            while ((sum / size) != mean.toFloat()) {
                if (array[i] > 1 && array[i] < 6) {
                    array[i] += mod
                    sum += mod
                } else {
                    i++
                }
                if (i == array.size) return intArrayOf()
            }
            return array
        }

    }
}
