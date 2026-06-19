package leetcode.linked_list

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0021 = (ListNode?, ListNode?) -> ListNode?

class I0021mergeTwoSortedLists {

    @Nested
    inner class Solution : ProblemTest<I0021> {

        override val cases = testCases<I0021>(
            args("1,2,4", "1,3,4") expects "1,1,2,3,4,4",
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(list1: ListNode?, list2: ListNode?): ListNode? {
            if (list1 == null) return list2
            if (list2 == null) return list1

            val head = ListNode(0)
            var l1Cursor = list1
            var l2Cursor = list2
            var resultCursor = head
            while (true) {
                println(head.toString())
                if (l1Cursor == null) {
                    resultCursor.next = l2Cursor
                    break
                }
                if (l2Cursor == null) {
                    resultCursor.next = l1Cursor
                    break
                }
                if (l1Cursor.`val` <= l2Cursor.`val`) {
                    resultCursor.next = ListNode(l1Cursor.`val`)
                    l1Cursor = l1Cursor.next
                } else {
                    resultCursor.next = ListNode(l2Cursor.`val`)
                    l2Cursor = l2Cursor.next
                }
                resultCursor = resultCursor.next!!
            }

            return head.next
        }


    }
}
