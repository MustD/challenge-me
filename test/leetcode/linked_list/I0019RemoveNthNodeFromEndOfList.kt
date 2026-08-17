package leetcode.linked_list

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0019 = (ListNode?, Int) -> ListNode?

class I0019RemoveNthNodeFromEndOfList {

    @Nested
    inner class Solution : ProblemTest<I0019> {

        override val cases = testCases<I0019>(
            args("[1,2,3,4,5]", 2) expects "[1,2,3,5]",
            args("[1]", 1) expects "[]",
            args("[1,2]", 1) expects "[1]",
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(head: ListNode?, n: Int): ListNode? {
            val dummy = ListNode(0)
            dummy.next = head
            var length = 0
            var curr = head

            while (curr != null) {
                length++
                curr = curr.next
            }

            length -= n
            curr = dummy

            while (length > 0) {
                length--
                curr = curr?.next
            }
            curr?.next = curr.next?.next
            return dummy.next
        }


    }
}
