package leetcode.two_pointers

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0167 = (IntArray, Int) -> IntArray

class I0167twoSumIIinputArrayIsSorted {

    @Nested
    inner class Solution : ProblemTest<I0167> {
        override val cases = testCases<I0167>(
            args("[2,7,11,15]", 9) expects "[1,2]",
            args("[2,3,4]", 6) expects "[1,3]",
            args("[-1,0]", -1) expects "[1,2]",
            args("[-5,-3,0,2,4,6,8]", 5) expects "[2,7]",
        )

        @Test
        fun test() = check(::solution1, ::solution2, ::solution3, ::solutionEditorial)

        fun solution1(numbers: IntArray, target: Int): IntArray {
            for (left in 0 until numbers.lastIndex) {
                for (right in 0 until numbers.size) {
                    if (left == right) continue
                    if ((numbers[left] + numbers[right]) == target) return intArrayOf(left + 1, right + 1)
                }
            }
            return intArrayOf()
        }

        fun solution2(numbers: IntArray, target: Int): IntArray {
            tailrec fun recFind(l: Int, r: Int): IntArray {
                val sum = numbers[l] + numbers[r]
                if (sum == target) return intArrayOf(l + 1, r + 1)
                if (l == r) return intArrayOf()
                val new = if (sum < target) l + 1 to r else l to r - 1
                return recFind(new.first, new.second)
            }
            return recFind(0, numbers.lastIndex)
        }

        fun solution3(numbers: IntArray, target: Int): IntArray {
            fun recFind(l: Int, r: Int): IntArray {
                val sum = numbers[l] + numbers[r]
                if (sum == target) return intArrayOf(l + 1, r + 1)
                return if (sum < target) recFind(l + 1, r) else recFind(l, r - 1)
            }
            return recFind(0, numbers.lastIndex)
        }

        fun solutionEditorial(numbers: IntArray, target: Int): IntArray {
            var low = 0
            var high: Int = numbers.lastIndex

            while (low < high) {
                val sum = numbers[low] + numbers[high]

                if (sum == target) {
                    return intArrayOf(low + 1, high + 1)
                } else if (sum < target) {
                    ++low
                } else {
                    --high
                }
            }
            return intArrayOf(-1, -1)
        }


    }
}
