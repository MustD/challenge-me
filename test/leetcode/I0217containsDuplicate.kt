package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 217. Contains Duplicate  (https://leetcode.com/problems/contains-duplicate/)
 *
 * Given an integer array `nums`, return `true` if any value appears **at least twice** in the array,
 * and return `false` if every element is distinct.
 *
 * Constraints:
 * - 1 <= nums.length <= 10^5
 * - -10^9 <= nums[i] <= 10^9
 */
typealias I0217 = (IntArray) -> Boolean

class I0217containsDuplicate {

    @Nested
    inner class Solution : ProblemTest<I0217> {

        override val cases = testCases<I0217>(
            "[1,2,3,1]" expects true,
            "[1,2,3,4]" expects false,
            "[1,1,1,3,3,4,3,2,4,2]" expects true,
        )

        @Test
        fun test() = check(::containsDuplicate)

        fun containsDuplicate(nums: IntArray): Boolean {
            TODO("implement")
        }

    }
}
