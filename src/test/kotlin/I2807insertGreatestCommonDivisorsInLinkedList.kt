import common.ListNode
import common.toListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * https://leetcode.com/problems/insert-greatest-common-divisors-in-linked-list
 */
class I2807insertGreatestCommonDivisorsInLinkedList {

    data class Case(
        val input: ListNode?,
        val output: ListNode?,
    )

    val parseCase = { head: String, output: String ->
        Case(head.toListNode(), output.toListNode())
    }

    @Nested
    inner class Solution : AproblemTest<Case, (ListNode?) -> ListNode?> {
        override val cases: List<Case> = listOf(
            parseCase("[18,6,10,3]", "[18,6,6,2,10,1,3]"),
            parseCase("[7]", "[7]"),
            parseCase("[12,8]", "[12,4,8]")
        )
        override val solutions: List<Pair<String, (ListNode?) -> ListNode?>> = listOf(
            ::solutionDP.name to ::solutionDP,
        )

        override fun Case.check(solution: (ListNode?) -> ListNode?): Pair<Boolean, Any> {
            val result = solution(input?.copy())
            return (result == output) to result as Any
        }

        @Test
        fun test() = check()

        fun solutionDP(head: ListNode?): ListNode? {
            fun ListNode.insertAfter(value: Int): ListNode {
                val new = ListNode(value).also { it.next = next }
                next = new
                return this
            }

            var node = head
            while (node != null) {
                val next = node.next
                if (next == null) break

                val value = node.`val`
                val min = if (value < next.`val`) value else next.`val`
                val max = if (value > next.`val`) value else next.`val`

                var divisor = min
                while (divisor > 0) {
                    if ((max % divisor == 0).and(min % divisor == 0)) {
                        break
                    }
                    divisor--
                }
                node.insertAfter(divisor)
                node = next
            }
            return head
        }


    }
}