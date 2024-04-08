import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * https://leetcode.com/problems/median-of-two-sorted-arrays/description/?source=submission-ac
 */
class I004medianOfTwoSortedArrays {

    @Suppress("ArrayInDataClass")
    data class Case(
        val input1: IntArray,
        val input2: IntArray,
        val result: Double,
    )


    @Nested
    inner class Solution : AproblemTest<Case, (IntArray, IntArray) -> Double> {
        override val cases: List<Case> = listOf(
            Case(listOf(1, 3).toIntArray(), listOf(2).toIntArray(), 2.0),
            Case(listOf(1, 2).toIntArray(), listOf(3, 4).toIntArray(), 2.5)
        )
        override val solutions: List<Pair<String, (IntArray, IntArray) -> Double>> = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
            ::solutionAi.name to ::solutionAi,
        )

        override fun Case.check(solution: (IntArray, IntArray) -> Double): Pair<Boolean, Any> {
            val result = solution(input1, input2)
            return (result == this.result) to result
        }

        @Test
        fun test() = check()

        fun solution1(nums1: IntArray, nums2: IntArray): Double {
            val sorted = (nums1 + nums2).sorted()
            return if ((sorted.size % 2) == 0) {
                listOf(sorted[sorted.size / 2 - 1], sorted[sorted.size / 2]).average()
            } else {
                sorted[sorted.size / 2].toDouble()
            }
        }

        fun solution2(nums1: IntArray, nums2: IntArray): Double {
            if (nums1.size + nums2.size == 1) return (nums1 + nums2).average()
            if (nums1.size + nums2.size == 2) return (nums1 + nums2).average()

            val sorted = (nums1 + nums2).sorted()
            return if ((sorted.size % 2) == 0) {
                (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
            } else {
                sorted[sorted.size / 2].toDouble()
            }
        }

        fun solutionAi(nums1: IntArray, nums2: IntArray): Double {
            if (nums2.size < nums1.size) {
                return solutionAi(nums2, nums1)
            }

            val x = nums1.size
            val y = nums2.size

            var low = 0
            var high = x

            while (low <= high) {
                val partitionX = (low + high) / 2
                val partitionY = (x + y + 1) / 2 - partitionX

                val maxLeftX = if (partitionX == 0) Int.MIN_VALUE else nums1[partitionX - 1]
                val minRightX = if (partitionX == x) Int.MAX_VALUE else nums1[partitionX]

                val maxLeftY = if (partitionY == 0) Int.MIN_VALUE else nums2[partitionY - 1]
                val minRightY = if (partitionY == y) Int.MAX_VALUE else nums2[partitionY]

                if (maxLeftX <= minRightY && maxLeftY <= minRightX) {
                    return if ((x + y) % 2 == 0) {
                        ((maxLeftX.coerceAtLeast(maxLeftY) + minRightX.coerceAtMost(minRightY)) / 2.0)
                    } else maxLeftX.coerceAtLeast(maxLeftY).toDouble()
                } else if (maxLeftX > minRightY) {
                    high = partitionX - 1
                } else {
                    low = partitionX + 1
                }
            }
            throw IllegalArgumentException("The input is not sorted arrays")
        }


    }
}