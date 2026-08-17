package leetcode.binary_search

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0004 = (IntArray, IntArray) -> Double

/**
 * https://leetcode.com/problems/median-of-two-sorted-arrays/description/?source=submission-ac
 */
class I0004medianOfTwoSortedArrays {

    @Nested
    inner class Solution : ProblemTest<I0004> {
        override val cases = testCases<I0004>(
            args("[1,3]", "[2]") expects 2.0,
            args("[1,2]", "[3,4]") expects 2.5,
        )

        @Test
        fun test() = check(::solution1, ::solution2, ::solutionAi)

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
