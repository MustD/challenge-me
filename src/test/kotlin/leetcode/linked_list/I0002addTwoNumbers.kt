package leetcode.linked_list

import leetcode.AproblemTest
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * https://leetcode.com/problems/add-two-numbers/description/
 */
class I0002addTwoNumbers {
    data class Case(
        val l1: List<Int>,
        val l2: List<Int>,
        val output: List<Int>,
    )

    private fun List<Int>.toListNode(): ListNode? = this.fold(null) { acc: ListNode?, i: Int -> ListNode(i, acc) }
    private fun ListNode?.toList(): List<Int> {
        val acc = mutableListOf<Int>()
        val input = this ?: return acc
        var curr = input
        while (true) {
            acc.add(curr.`val`)
            curr = curr.next ?: return acc
        }
    }

    @Nested
    inner class Solution : AproblemTest<Case, (ListNode?, ListNode?) -> ListNode?> {

        override val cases: List<Case> = listOf(
            Case(listOf(2, 4, 3), listOf(5, 6, 4), listOf(7, 0, 8)),
            Case(listOf(0), listOf(0), listOf(0)),
            Case(listOf(9, 9, 9, 9, 9, 9, 9), listOf(9, 9, 9, 9), listOf(8, 9, 9, 9, 0, 0, 0, 1)),
        )

        override val solutions: List<Pair<String, (ListNode?, ListNode?) -> ListNode?>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionAi.name to ::solutionAi,
        )

        override fun Case.check(solution: (ListNode?, ListNode?) -> ListNode?): Pair<Boolean, Any> {
            val result = solution(l1.toListNode(), l2.toListNode()).toList()
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

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
