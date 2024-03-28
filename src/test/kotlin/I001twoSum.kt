import org.junit.jupiter.api.Nested
import kotlin.test.Test


/**
 * https://leetcode.com/problems/two-sum/description/
 */
class I001twoSum {
    @Suppress("ArrayInDataClass")
    data class Case(
        val input: IntArray,
        val target: Int,
        val output: IntArray,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (IntArray, Int) -> IntArray> {

        override val cases: List<Case> = listOf(
            Case(intArrayOf(2, 7, 11, 15), 9, intArrayOf(0, 1)),
            Case(intArrayOf(3, 2, 4), 6, intArrayOf(1, 2)),
            Case(intArrayOf(3, 3), 6, intArrayOf(0, 1))
        )

        override val solutions: List<Pair<String, (IntArray, Int) -> IntArray>> = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
            ::solution3.name to ::solution3,
            ::solutionAi.name to ::solutionAi
        )

        override fun Case.check(solution: (IntArray, Int) -> IntArray): Boolean {
            return solution(input, target).toList().sorted() == output.toList().sorted()
        }

        @Test
        fun test() = check()

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