package leetcode.hash_map

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.math.abs
import kotlin.test.Test


typealias I0219 = (IntArray, Int) -> Boolean

class I0219ContainsDuplicateII {

    @Nested
    inner class Solution : ProblemTest<I0219> {

        override val cases = testCases<I0219>(
            args("[1,2,3,1]", 3) expects true,
            args("[1,0,1,1]", 1) expects true,
            args("[1,2,3,1,2,3]", 2) expects false,
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial)

        private fun solution1(nums: IntArray, k: Int): Boolean {
            val map = mutableMapOf<Int, List<Int>>().withDefault { listOf() }
            for (i in nums.indices) {
                val old = map.getValue(nums[i])
                val new = old.plus(i)
                map[nums[i]] = new
                if (new.size > 1) {
                    val a = new[new.lastIndex / 2]
                    val b = new[new.lastIndex / 2 + 1]
                    if (abs(a - b) <= k) return true
                }
            }
            return false
        }

        private fun solutionEditorial(nums: IntArray, k: Int): Boolean {
            val set = HashSet<Int>()
            for (i in nums.indices) {
                if (set.contains(nums[i])) return true
                set.add(nums[i])
                if (set.size > k) {
                    set.remove(nums[i - k])
                }
            }
            return false
        }


    }
}
