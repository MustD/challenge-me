package leetcode.array_string

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0088 = (IntArray, Int, IntArray, Int) -> IntArray

class I0088mergeSortedArray {

    @Nested
    inner class Solution : ProblemTest<I0088> {
        override val cases = testCases<I0088>(
            args("[1,2,3,0,0,0]", 3, "[2,5,6]", 3) expects "[1,2,2,3,5,6]",
        )

        @Test
        fun test() = check(::solution1, ::solution2)

        fun solution1(nums1: IntArray, m: Int, nums2: IntArray, n: Int): IntArray {
            for (i in 0 until n) {
                nums1[m + i] = nums2[i]
            }
            nums1.sort()
            return nums1
        }

        fun solution2(nums1: IntArray, m: Int, nums2: IntArray, n: Int): IntArray {
            var mP = m - 1
            var nP = n - 1

            for (i in nums1.lastIndex downTo 0) {
                if (mP >= 0 && (nP < 0 || nums1[mP] >= nums2[nP])) {
                    nums1[i] = nums1[mP--]
                } else {
                    nums1[i] = nums2[nP--]
                }
            }
            return nums1
        }

    }
}
