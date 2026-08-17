package leetcode.array_string

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0134 = (IntArray, IntArray) -> Int

class I0134gasStation {

    @Nested
    inner class Solution : ProblemTest<I0134> {
        override val cases = testCases<I0134>(
            args("[1,2,3,4,5]", "[3,4,5,1,2]") expects 3,
            args("[2,3,4]", "3,4,3]") expects -1,
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial)


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
