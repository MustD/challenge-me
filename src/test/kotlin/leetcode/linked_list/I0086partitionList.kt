package leetcode.linked_list

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0086 = (ListNode?, Int) -> ListNode?

class I0086partitionList {

    @Nested
    inner class Solution : ProblemTest<I0086> {

        override val cases = testCases<I0086>(
            args("[1,4,3,2,5,2]", 3) expects "[1,2,2,4,3,5]",
            args("[2,1]", 2) expects "[1,2]",
        )

        @Test
        fun test() = check(::partition)

        fun partition(head: ListNode?, x: Int): ListNode? {
            val lessDummy = ListNode(0)
            val geDummy = ListNode(0)

            var lessTail = lessDummy
            var geTail = geDummy
            var cur = head
            while (cur != null) {
                if (cur.`val` < x) {
                    lessTail.next = cur
                    lessTail = cur
                } else {
                    geTail.next = cur
                    geTail = cur
                }
                cur = cur.next
            }
            geTail.next = null
            lessTail.next = geDummy.next
            return lessDummy.next
        }

    }
}
