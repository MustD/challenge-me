package leetcode.linked_list

import leetcode.utils.ListNode
import leetcode.utils.toListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals

// I0141 needs a CYCLIC linked list as input, which the string->ListNode converter (and therefore
// the ProblemTest harness) cannot build — so this problem keeps a small SELF-CONTAINED harness that
// constructs the cycle by hand. `pos` is the LeetCode "index the tail's next connects to" (-1 = no
// cycle).
//
// Why not AproblemTest: its failure message interpolates the whole `Case`, and a data class holding
// a cyclic ListNode would loop forever in `toString()` (that's why the old test was @Disabled). Here
// the cyclic node is built transiently and never stringified — `Case` holds only the raw String/Int
// description — so the assertions compare plain Booleans and the test can run normally.
private typealias I0141 = (ListNode?) -> Boolean

class I0141LinkedListCycle {

    // Cyclic input can't be a serialized string, so cases stay as (nums, pos, expected) and the
    // cyclic list is assembled per run by [build] — never stored or printed.
    data class Case(val nums: String, val pos: Int, val expected: Boolean)

    @Nested
    inner class Solution {

        private fun Case.build(): ListNode? {
            val head = nums.toListNode()
            if (pos < 0) return head
            var target: ListNode? = null
            var tail: ListNode? = null
            var current = head
            var i = 0
            while (current != null) {
                if (i == pos) target = current
                if (current.next == null) tail = current
                current = current.next
                i++
            }
            tail?.next = target
            return head
        }

        private val cases = listOf(
            Case("[3,2,0,-4]", 1, true),
            Case("[1,2]", 0, true),
            Case("[1]", -1, false),
        )

        private val solutions = listOf<Pair<String, I0141>>(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial,
        )

        @Test
        fun test() {
            solutions.forEach { (name, solution) ->
                cases.forEachIndexed { i, case ->
                    assertEquals(case.expected, solution(case.build()), "$name case[${i + 1}] failed")
                }
            }
        }

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
