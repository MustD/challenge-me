package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0080 = (IntArray) -> Int

class I0080removeDuplicatesFromSortedArray2 {

    @Nested
    inner class Solution : ProblemTest<I0080> {
        override val cases = testCases<I0080>(
            "[1,1,1,2,2,3]" expects 5,
            "[0,0,1,1,1,1,2,3,3]" expects 7,
            "[1,1,1]" expects 2,
        )

        @Test
        fun test() = check(::solution1, ::solution2)

        fun solution1(nums: IntArray): Int {
            var count = 0
            var i = 0
            while (i < nums.lastIndex) {
                if (nums[i] > nums[i + 1]) return i + 1
                if (count == 3) return i + 1

                if (count == 0) {
                    if (nums[i] == nums[i + 1]) count++
                } else {
                    if (nums[i] == nums[i + 1]) {
                        nums[i + 1] = -1
                        var j = i + 1
                        while (j <= nums.lastIndex - 1) {
                            val t = nums[j]
                            nums[j] = nums[j + 1]
                            nums[j + 1] = t
                            j++
                        }
                        continue
                    } else {
                        count = 0
                    }
                }
                i++
            }
            return nums.size
        }

        fun solution2(nums: IntArray): Int {
            if (nums.size <= 2) return nums.size

            var slow = 2
            for (fast in 2 until nums.size) {
                if (nums[slow - 2] != nums[fast]) nums[slow++] = nums[fast]
            }

            return slow
        }

    }
}
