package leetcode.array_string

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

// LeetCode's signature is `(IntArray, Int) -> Unit`: the solution rotates `nums` in place by `k`
// steps and returns nothing. The ProblemTest harness asserts on the *return value*, so we return
// the (mutated) input array instead of Unit to make the rotation checkable. To match LeetCode
// exactly, drop the `: IntArray` return type and the trailing `return nums`.
private typealias I0189 = (IntArray, Int) -> IntArray

class I0189rotateArray {

    @Nested
    inner class Solution : ProblemTest<I0189> {
        override val cases = testCases<I0189>(
            args("[1,2,3,4,5,6,7]", 3) expects "[5,6,7,1,2,3,4]",
            args("[-1,-100,3,99]", 2) expects "[3,99,-1,-100]",
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial)

        fun solution1(nums: IntArray, k: Int): IntArray {
            fun move() {
                var holder = nums[0]
                for (i in 1..nums.lastIndex) {
                    val tmp = nums[i]
                    nums[i] = holder
                    holder = tmp
                }
                nums[0] = holder
            }
            repeat(k) { move() }
            return nums
        }

        fun solutionEditorial(nums: IntArray, k: Int): IntArray {
            val j = k % nums.size
            var count = 0
            var start = 0
            while (count < nums.size) {
                var current = start
                var prev = nums[start]
                do {
                    val next = (current + j) % nums.size
                    val tmp = nums[next]
                    nums[next] = prev
                    prev = tmp
                    current = next
                    count++
                } while (start != current)
                start++
            }
            return nums
        }

    }
}
