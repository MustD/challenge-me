package leetcode.linked_list

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0092 = (ListNode?, Int, Int) -> ListNode?

class I0092ReverseLinkedListII {

    @Nested
    inner class Solution : ProblemTest<I0092> {

        override val cases = testCases<I0092>(
            args("[1,2,3,4,5]", 2, 4) expects "[1,4,3,2,5]",
            args("[5]", 1, 1) expects "[5]",
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(head: ListNode?, left: Int, right: Int): ListNode? {
            if (head == null) return null
            var start = head
            var m = left
            var n = right

            // Move the two pointers until they reach the proper starting point
            // in the list.
            var cur: ListNode? = start
            var prev: ListNode? = null
            while (m > 1) {
                prev = cur
                cur = cur?.next
                m--
                n--
            }

            // The two pointers that will fix the final connections.
            val con: ListNode? = prev
            val tail: ListNode? = cur

            // Iteratively reverse the nodes until n becomes 0.
            var third: ListNode? = null
            while (n > 0) {
                third = cur?.next
                cur?.next = prev
                prev = cur
                cur = third
                n--
            }


            // Adjust the final connections as explained in the algorithm
            if (con != null) {
                con.next = prev
            } else {
                start = prev
            }

            tail!!.next = cur
            return start
        }


    }
}
