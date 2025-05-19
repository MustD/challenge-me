package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.math.cos
import kotlin.test.Test

private typealias I0134 = (IntArray, IntArray) -> Int

class I0134gasStation {

    data class Case(
        val gas: List<Int>,
        val cost: List<Int>,
        val output: Int,
    )

    val prepareCase = { n1: String, n2: String, r: Int ->
        Case(n1.toIntArray().toList(), n2.toIntArray().toList(), r)
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0134> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,2,3,4,5]", "[3,4,5,1,2]", 3),
            prepareCase("[2,3,4]", "3,4,3]", -1)
        )
        override val solutions: List<Pair<String, I0134>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0134): Pair<Boolean, Any> {
            val result = solution(gas.toIntArray(), cost.toIntArray())
            return (result == output) to result
        }

        @Test
        fun test() = check()


        fun solution1(gas: IntArray, cost: IntArray): Int {
            fun canComplete(i: Int): Boolean {
                var tank = 0
                for (j in 0..gas.lastIndex) {
                    val idx = (i + j) % gas.size
                    tank += gas[idx]
                    tank -= cost[idx]
                    if (tank < 0) return false
                }
                return true
            }

            for (i in gas.indices) {
                if (canComplete(i)) return i
            }
            return -1
        }

        fun solutionEditorial(gas: IntArray, cost: IntArray): Int {
            var currGain = 0
            var totalGain = 0
            var answer = 0
            for (i in gas.indices) {
                totalGain += gas[i] - cost[i]
                currGain += gas[i] - cost[i]

                if (currGain < 0) {
                    currGain = 0
                    answer = i + 1
                }
            }
            return if (totalGain >= 0) answer else -1
        }


    }
}
