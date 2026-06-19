package leetcode.linked_list

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0002 = (ListNode?, ListNode?) -> ListNode?

/**
 * https://leetcode.com/problems/add-two-numbers/description/
 */
class I0002addTwoNumbers {

    @Nested
    inner class Solution : ProblemTest<I0002> {

        override val cases = testCases<I0002>(
            args("[2,4,3]", "[5,6,4]") expects "[7,0,8]",
            args("[0]", "[0]") expects "[0]",
            args("[9,9,9,9,9,9,9]", "[9,9,9,9]") expects "[8,9,9,9,0,0,0,1]",
        )

        @Test
        fun test() = check(::solution1, ::solutionAi)

        private fun solution1(l1: ListNode?, l2: ListNode?): ListNode? {

            fun addNumbers(nodeA: ListNode?, nodeB: ListNode?, add: Boolean = false): ListNode? {
                if ((nodeA == null) and (nodeB == null)) return if (add) ListNode(1) else null
                val a = nodeA?.`val` ?: 0
                val b = nodeB?.`val` ?: 0

                val sum = if (add) a + b + 1 else a + b

                return if (sum > 9) {
                    ListNode(sum - 10, addNumbers(nodeA?.next, nodeB?.next, true))
                } else {
                    ListNode(sum, addNumbers(nodeA?.next, nodeB?.next))
                }
            }
            return addNumbers(l1, l2)
        }

        private fun solutionAi(l1: ListNode?, l2: ListNode?): ListNode? {
            val dummyHead = ListNode(0)
            var p = l1
            var q = l2
            var curr = dummyHead
            var carry = 0

            while (p != null || q != null) {
                val x = p?.`val` ?: 0
                val y = q?.`val` ?: 0
                val sum = carry + x + y
                carry = sum / 10
                curr.next = ListNode(sum % 10)
                curr = curr.next!!
                if (p != null) p = p.next
                if (q != null) q = q.next
            }

            if (carry > 0) {
                curr.next = ListNode(carry)
            }

            return dummyHead.next
        }


    }
}
