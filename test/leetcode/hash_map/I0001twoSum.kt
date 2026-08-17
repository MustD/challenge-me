package leetcode.hash_map

import leetcode.ProblemTest
import leetcode.args
import leetcode.expectsAnyOrder
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * https://leetcode.com/problems/two-sum/description/
 */
typealias I0001 = (IntArray, Int) -> IntArray

class I0001twoSum {

    @Nested
    inner class Solution : ProblemTest<I0001> {

        override val cases = testCases<I0001>(
            args("[2,7,11,15]", 9) expectsAnyOrder "[0,1]",
            args("[3,2,4]", 6) expectsAnyOrder "[1,2]",
            args("[3,3]", 6) expectsAnyOrder "[0,1]",
        )

        @Test
        fun test() = check(::solution1, ::solution2, ::solution3, ::solutionAi)

        private fun solution1(nums: IntArray, target: Int): IntArray {
            val list = nums.toList()
            list.indices.forEach { first ->
                val a = list[first]
                list.indices.forEach inner@{ second ->
                    val b = list[second]
                    if (first == second) return@inner
                    if (a + b == target) return listOf(first, second).toIntArray()
                }
            }
            return intArrayOf(-1, -1)
        }

        private fun solution2(nums: IntArray, target: Int): IntArray {
            val list = nums.toList()
            val map = list.mapIndexed { index, i -> i to index }.toMap()
            list.forEachIndexed { indexB, value ->
                map[target - value]?.let { indexA ->
                    if (indexA == indexB) return@forEachIndexed
                    return intArrayOf(indexA, indexB)
                }
            }
            return intArrayOf(-1, -1)
        }

        private fun solution3(nums: IntArray, target: Int): IntArray {
            val map = mutableMapOf<Int, Int>()
            nums.mapIndexed { index, i -> map[i] = index }

            nums.forEachIndexed { indexB, value ->
                map[target - value]?.let { indexA ->
                    if (indexA == indexB) return@forEachIndexed
                    return intArrayOf(indexA, indexB)
                }
            }
            return intArrayOf(-1, -1)
        }

        private fun solutionAi(nums: IntArray, target: Int): IntArray {
            val map = mutableMapOf<Int, Int>()

            nums.forEachIndexed { indexB, value ->
                val complement = target - value
                map[complement]?.let { indexA -> if (indexA != indexB) return intArrayOf(indexA, indexB) }
                map[value] = indexB
            }
            return intArrayOf(-1, -1)
        }


    }
}
