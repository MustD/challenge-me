package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

// LeetCode accepts ANY array of `n` dice whose values, combined with `rolls`, produce the target
// `mean` (there can be many valid answers). The original AproblemTest mirrored this with a fallback
// that accepted any result with the correct mean. The ProblemTest harness compares against a single
// expected value, so each expected below is pinned to the *deterministic* output `solution1` emits.
// Notably case 2 ([1,5,6],3,4) emits [1,2,3,3] — the old test's literal [2,3,2,2] is also valid and
// only passed via that mean fallback. The third case is genuinely impossible, so [] is the answer.
typealias I2028 = (IntArray, Int, Int) -> IntArray

class I2028findMissingObservations {

    @Nested
    inner class Solution : ProblemTest<I2028> {

        override val cases = testCases<I2028>(
            args("[3,2,4,3]", 4, 2) expects "[6,6]",
            args("[1,5,6]", 3, 4) expects "[1,2,3,3]",
            args("[1,2,3,4]", 6, 4) expects "[]",
        )

        @Test
        fun test() = check(::solution1)

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
