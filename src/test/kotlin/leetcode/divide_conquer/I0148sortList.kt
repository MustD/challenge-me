package leetcode.divide_conquer

import leetcode.AproblemTest
import leetcode.utils.ListNode
import leetcode.utils.toListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0148 = (ListNode?) -> ListNode?

class I0148sortList {

    data class Case(
        val head: ListNode?,
        val output: ListNode?,
    )

    val parseCase = { head: String, output: String ->
        Case(head.toListNode(), output.toListNode())
    }

    @Nested
    inner class Solution : AproblemTest<Case, I0148> {
        override val cases: List<Case> = listOf(
            parseCase("[4,2,1,3]", "[1,2,3,4]"),
        )
        override val solutions = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0148): Pair<Boolean, Any> {
            val result = solution(head?.deepCopy())
            return (result.toString() == output.toString()) to result.toString()
        }

        @Test
        fun test() = check()


        fun solution1(head: ListNode?): ListNode? {

            fun getMid(head: ListNode?): ListNode? {
                var slow = head
                var fast = head

                var prev: ListNode? = null
                while (fast?.next != null) {
                    prev = slow
                    slow = slow?.next
                    fast = fast.next?.next
                }
                prev?.next = null // split the list
                return slow
            }

            fun merge(l1: ListNode?, l2: ListNode?): ListNode? {
                val dummy = ListNode(0)
                var tail = dummy

                var p1 = l1
                var p2 = l2

                while (p1 != null && p2 != null) {
                    if (p1.`val` < p2.`val`) {
                        tail.next = p1
                        p1 = p1.next
                    } else {
                        tail.next = p2
                        p2 = p2.next
                    }
                    tail = tail.next!!
                }
                tail.next = p1 ?: p2

                return dummy.next
            }

            fun sortList(head: ListNode?): ListNode? {
                if (head?.next == null) return head

                val mid = getMid(head)
                val left = sortList(head)
                val right = sortList(mid)

                return merge(left, right)
            }

            return sortList(head)

        }

    }
}
