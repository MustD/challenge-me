package leetcode.linked_list

import leetcode.AproblemTest
import leetcode.utils.ListNode
import leetcode.utils.toListNode
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0141 = (ListNode?) -> Boolean

class I0141LinkedListCycle {
    data class Case(
        val input: ListNode?,
        val output: Boolean,
    )

    val prepareCase = { input: String, link: Int, out: Boolean ->
        val linkedList = input.toListNode()
        if (link < 0) {
            Case(linkedList, out)
        } else {
            var node: ListNode? = null
            var current = linkedList
            var i = 0
            while (current?.next != null) {
                if (i == link) node = current
                current = current.next
                i++
            }
            current?.next = node
            Case(linkedList, out)
        }
    }

    @Nested
    inner class Solution : AproblemTest<Case, I0141> {

        override val cases: List<Case> = listOf(
            prepareCase("[3,2,0,-4]", 1, true),
            prepareCase("[1,2]", 0, true),
            prepareCase("[1]", -1, false),
        )

        override val solutions: List<Pair<String, I0141>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0141): Pair<Boolean, Any> {
            val result = solution(input)
            val isCorrect = result.toString() == output.toString()
            return isCorrect to result
        }

        @Test
        @Disabled("solution is correct, but cycle prevents correct reporting")
        fun test() = check()

        private fun solution1(head: ListNode?): Boolean {
            val set = mutableSetOf<Int>()
            var current = head
            while (current != null) {
                val code = System.identityHashCode(current)
                if (set.contains(code)) return true
                set.add(code)
                current = current.next
            }
            return false
        }

        private fun solutionEditorial(head: ListNode?): Boolean {
            if (head == null) return false
            var slow = head
            var fast = head.next
            while (slow !== fast) {
                if (fast == null || fast.next == null) return false
                slow = slow?.next!!
                fast = fast.next!!.next
            }
            return true
        }


    }
}
