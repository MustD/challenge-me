package leetcode.linked_list

import leetcode.AproblemTest
import leetcode.utils.ListNode
import leetcode.utils.toListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0021 = (ListNode?, ListNode?) -> ListNode?

class I0021mergeTwoSortedLists {
    data class Case(
        val l1: ListNode?,
        val l2: ListNode?,
        val output: ListNode?,
    )

    val prepareCase = { l1: String, l2: String, out: String ->
        Case(l1.toListNode(), l2.toListNode(), out.toListNode())
    }

    @Nested
    inner class Solution : AproblemTest<Case, I0021> {

        override val cases: List<Case> = listOf(
            prepareCase("1,2,4", "1,3,4", "1,1,2,3,4,4"),
        )

        override val solutions: List<Pair<String, I0021>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0021): Pair<Boolean, Any> {
            val result = solution(l1, l2)
            val isCorrect = result.toString() == output.toString()
            return isCorrect to result.toString()
        }

        @Test
        fun test() = check()

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
