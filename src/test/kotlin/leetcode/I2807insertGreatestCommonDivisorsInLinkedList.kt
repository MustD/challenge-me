package leetcode

import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I2807 = (ListNode?) -> ListNode?

/**
 * https://leetcode.com/problems/insert-greatest-common-divisors-in-linked-list
 */
class I2807insertGreatestCommonDivisorsInLinkedList {

    @Nested
    inner class Solution : ProblemTest<I2807> {
        override val cases = testCases<I2807>(
            "[18,6,10,3]" expects "[18,6,6,2,10,1,3]",
            "[7]" expects "[7]",
            "[12,8]" expects "[12,4,8]",
        )

        @Test
        fun test() = check(::solutionDP, ::solution1)

        fun solutionDP(head: ListNode?): ListNode? {
            fun ListNode.insertAfter(value: Int): ListNode {
                val new = ListNode(value).also { it.next = next }
                next = new
                return this
            }

            var node = head
            while (node != null) {
                val next = node.next
                if (next == null) break

                val value = node.`val`
                val min = if (value < next.`val`) value else next.`val`
                val max = if (value > next.`val`) value else next.`val`

                var divisor = min
                while (divisor > 0) {
                    if ((max % divisor == 0).and(min % divisor == 0)) {
                        break
                    }
                    divisor--
                }
                node.insertAfter(divisor)
                node = next
            }
            return head
        }

        fun solution1(head: ListNode?): ListNode? {
            fun ListNode.insertAfter(value: Int): ListNode {
                val new = ListNode(value).also { it.next = next }
                next = new
                return this
            }

            fun findGCD(a: Int, b: Int): Int {
                var num1 = a
                var num2 = b
                while (num2 != 0) {
                    val temp = num2
                    num2 = num1 % num2
                    num1 = temp
                }
                return num1
            }

            var node = head
            while (node != null) {
                val next = node.next
                if (next == null) break
                node.insertAfter(findGCD(node.`val`, next.`val`))
                node = next
            }
            return head
        }


    }
}
