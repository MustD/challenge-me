package leetcode.linked_list

import leetcode.AproblemTest
import leetcode.utils.ListNode
import leetcode.utils.toListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0019 = (ListNode?, Int) -> ListNode?

class I0019RemoveNthNodeFromEndOfList {
    data class Case(
        val head: ListNode?,
        val n: Int,
        val output: ListNode?,
    )

    val prepareCase = { head: String, n: Int, out: String ->
        val outNode = if ("[]" == out) null else out.toListNode()
        Case(head.toListNode(), n, outNode)
    }

    @Nested
    inner class Solution : AproblemTest<Case, I0019> {

        override val cases: List<Case> = listOf(
            prepareCase("[1,2,3,4,5]", 2, "[1,2,3,5]"),
            prepareCase("[1]", 1, "[]"),
            prepareCase("[1,2]", 1, "[1]"),
        )

        override val solutions: List<Pair<String, I0019>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0019): Pair<Boolean, Any> {
            val result = solution(head, n)
            val isCorrect = result.toString() == output.toString()
            return isCorrect to result.toString()
        }

        @Test
        fun test() = check()

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
