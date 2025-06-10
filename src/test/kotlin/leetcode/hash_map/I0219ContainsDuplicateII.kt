package leetcode.hash_map

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.math.abs
import kotlin.test.Test


typealias I0219 = (IntArray, Int) -> Boolean

class I0219ContainsDuplicateII {
    data class Case(
        val nums: List<Int>,
        val k: Int,
        val output: Boolean,
    )

    val prepareCase = { nums: String, k: Int, output: Boolean ->
        Case(nums.toIntArray().toList(), k, output)
    }

    @Nested
    inner class Solution : AproblemTest<Case, I0219> {

        override val cases: List<Case> = listOf(
            prepareCase("[1,2,3,1]", 3, true),
            prepareCase("[1,0,1,1]", 1, true),
            prepareCase("[1,2,3,1,2,3]", 2, false),
        )

        override val solutions: List<Pair<String, I0219>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0219): Pair<Boolean, Any> {
            val result = solution(nums.toIntArray(), k)
            return (result == output) to result
        }

        @Test
        fun test() = check()

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
