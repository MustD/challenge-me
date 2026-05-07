package leetcode.linked_list

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0082 = (ListNode?) -> ListNode?

class I0082removeDuplicatesFromSortedList2 {

    @Nested
    inner class Solution : ProblemTest<I0082> {

        override val cases = testCases<I0082>(
            "[1,2,3,3,4,4,5]" expects "[1,2,5]",
            "[1,1,1,2,3]" expects "[2,3]",
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(head: ListNode?): ListNode? {
            val dummy = ListNode(0).also { it.next = head }
            var prev: ListNode = dummy
            var curr: ListNode? = head

            while (curr != null) {
                val next = curr.next
                if (next != null && curr.`val` == next.`val`) {
                    val dupVal = curr.`val`
                    while (curr != null && curr.`val` == dupVal) curr = curr.next
                    prev.next = curr
                } else {
                    prev = curr
                    curr = curr.next
                }
            }

            return dummy.next
        }

    }
}
