package leetcode.two_pointers

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import leetcode.utils.ArrayUtils.toListOfIntLists
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0015 = (IntArray) -> List<List<Int>>

class I00153sum {

    data class Case(
        val nums: List<Int>,
        val output: List<List<Int>>,
    )

    val prepareCase = { n1: String, r: String ->
        Case(n1.toIntArray().toList(), r.toListOfIntLists())
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0015> {
        override val cases: List<Case> = listOf(
            prepareCase("-1,0,1,2,-1,-4]", "[[-1,-1,2],[-1,0,1]]"),
            prepareCase("[0,1,1]", "[]"),
            prepareCase("[0,0,0]", "[[0,0,0]]"),
        )
        override val solutions: List<Pair<String, I0015>> = listOf(
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0015): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray())
            val isCorrect = result.map { it.sorted() } == output.map { it.sorted() }
            return isCorrect to result
        }

        @Test
        fun test() = check()


        fun solutionEditorial(nums: IntArray): List<List<Int>> {
            nums.sort()
            val result = mutableListOf<List<Int>>()
            for (i in 0..nums.lastIndex) {
                if (nums[i] > 0) break
                if (i == 0 || nums[i - 1] != nums[i]) {
                    var lo = i + 1
                    var hi = nums.lastIndex
                    while (lo < hi) {
                        println("i: $i, lo: $lo, hi: $hi")
                        val sum = nums[i] + nums[lo] + nums[hi]
                        if (sum < 0) {
                            lo++
                        } else if (sum > 0) {
                            hi--
                        } else {
                            result.add(listOf(nums[i], nums[lo], nums[hi]))
                            lo++
                            hi--
                            while (lo < hi && nums[lo] == nums[lo - 1]) {
                                lo++
                            }
                        }
                    }
                }
            }

            return result
        }


    }
}
