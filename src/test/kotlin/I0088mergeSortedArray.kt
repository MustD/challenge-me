import common.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0088 = (IntArray, Int, IntArray, Int) -> IntArray

class I0088mergeSortedArray {

    @Suppress("ArrayInDataClass")
    data class Case(
        val nums1: IntArray,
        val m: Int,
        val nums2: IntArray,
        val n: Int,
        val output: IntArray,
    )

    val prepareCase = { n1: String, m: Int, n2: String, n: Int, r: String ->
        Case(n1.toIntArray(), m, n2.toIntArray(), n, r.toIntArray())
    }


    @Nested
    inner class Solution : AproblemTest<Case, I0088> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,2,3,0,0,0]", 3, "[2,5,6]", 3, "[1,2,2,3,5,6]"),
        )
        override val solutions: List<Pair<String, I0088>> = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
        )

        override fun Case.check(solution: I0088): Pair<Boolean, Any> {
            val result = solution(nums1.copyOf(), m, nums2.copyOf(), n)
            return (result.toList() == output.toList()) to result
        }

        @Test
        fun test() = check()

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
