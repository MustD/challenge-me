package leetcode.array_string

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0189 = (IntArray, Int) -> Unit

class I0189rotateArray {

    data class Case(
        val nums: List<Int>,
        val k: Int,
        val output: List<Int>,
    )

    val prepareCase = { n1: String, k: Int, r: String ->
        Case(n1.toIntArray().toList(), k, r.toIntArray().toList())
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0189> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,2,3,4,5,6,7]", 3, "[5,6,7,1,2,3,4]"),
            prepareCase("[-1,-100,3,99]", 2, "[3,99,-1,-100]"),
        )
        override val solutions: List<Pair<String, I0189>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0189): Pair<Boolean, Any> {
            val array = nums.toIntArray()
            solution(array, k)
            return (array.toList() == output) to array.toList()
        }

        @Test
        fun test() = check()

        fun solution1(nums: IntArray, k: Int): Unit {
            fun move() {
                var holder = nums[0]
                for (i in 1..nums.lastIndex) {
                    val tmp = nums[i]
                    nums[i] = holder
                    holder = tmp
                }
                nums[0] = holder
            }
            return repeat(k) { move() }
        }

        fun solutionEditorial(nums: IntArray, k: Int): Unit {
            val j = k % nums.size
            var count = 0
            var start = 0
            while (count < nums.size) {
                var current = start
                var prev = nums[start]
                do {
                    val next = (current + j) % nums.size
                    val tmp = nums[next]
                    nums[next] = prev
                    prev = tmp
                    current = next
                    count++
                } while (start != current)
                start++
            }
        }

    }
}
