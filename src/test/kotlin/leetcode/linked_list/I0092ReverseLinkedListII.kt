package leetcode.linked_list

import leetcode.AproblemTest
import leetcode.utils.ListNode
import leetcode.utils.toListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0092 = (ListNode?, Int, Int) -> ListNode?

class I0092ReverseLinkedListII {
    data class Case(
        val head: ListNode?,
        val left: Int,
        val right: Int,
        val output: ListNode?,
    )

    val prepareCase = { head: String, left: Int, right: Int, out: String ->
        Case(head.toListNode(), left, right, out.toListNode())
    }

    @Nested
    inner class Solution : AproblemTest<Case, I0092> {

        override val cases: List<Case> = listOf(
            prepareCase("[1,2,3,4,5]", 2, 4, "[1,4,3,2,5]"),
            prepareCase("[5]", 1, 1, "[5]"),
        )

        override val solutions: List<Pair<String, I0092>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0092): Pair<Boolean, Any> {
            val result = solution(head, left, right)
            val isCorrect = result.toString() == output.toString()
            return isCorrect to result.toString()
        }

        @Test
        fun test() = check()

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
