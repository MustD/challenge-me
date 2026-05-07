package leetcode.linked_list

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0061 = (ListNode?, Int) -> ListNode?

class I0061rotateList {

    @Nested
    inner class Solution : ProblemTest<I0061> {

        override val cases = testCases<I0061>(
            args("[1,2,3,4,5]", 2) expects "[4,5,1,2,3]",
            args("[0,1,2]", 4) expects "[2,0,1]",
        )

        @Test
        fun test() = check(::rotateRight)

        fun rotateRight(head: ListNode?, k: Int): ListNode? {
            if (head?.next == null) return head

            var tail = head
            var n = 1
            while (tail?.next != null) {
                tail = tail.next
                n++
            }

            val steps = k % n
            if (steps == 0) return head

            var newTail = head
            repeat(n - steps - 1) { newTail = newTail?.next }

            val newHead = newTail?.next
            newTail?.next = null
            tail?.next = head

            return newHead
        }

    }
}
