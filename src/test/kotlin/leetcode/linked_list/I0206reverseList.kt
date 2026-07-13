package leetcode.linked_list

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 206. Reverse Linked List  (https://leetcode.com/problems/reverse-linked-list/)
 *
 * Given the head of a singly linked list, reverse the list and return the head of
 * the reversed list. After reversing, the original tail becomes the new head and
 * every `next` pointer points to the node that used to precede it.
 *
 * Constraints:
 * - The number of nodes in the list is in the range [0, 5000].
 * - -5000 <= Node.val <= 5000.
 * - Edge cases to consider: empty list ("[]" -> null) and a single-node list.
 * - Follow-up: solve it both iteratively and recursively.
 */
typealias I0206 = (ListNode?) -> ListNode?

class I0206reverseList {

    @Nested
    inner class Solution : ProblemTest<I0206> {

        override val cases = testCases<I0206>(
            "1,2,3,4,5" expects "5,4,3,2,1",
            "1,2" expects "2,1",
            "1" expects "1",
            "[]" expects "[]",
        )

        @Test
        fun test() = check(::referenceIterative, ::referenceRecursive)


        /**
         * Pattern: in-place pointer reversal ("three-pointer" linked-list walk).
         *
         * Intuition — reversing a singly linked list means flipping every `next` arrow to point
         * backwards. The trap is that the moment you flip `cur.next` to point at the previous node,
         * you lose the reference to the rest of the list. So you must save the next node *before*
         * rewriting the pointer. Carry three references as you walk:
         *   - `prev`: the already-reversed prefix (starts as `null`, which also becomes the new tail's `next`).
         *   - `cur`:  the node currently being rewired.
         *   - `next`: a temporary stash of `cur.next` so you don't lose the unprocessed suffix.
         *
         * Approach (iterative): start `prev = null`, `cur = head`. Each step: stash `next = cur.next`,
         * point `cur.next = prev` (the flip), then slide both forward: `prev = cur`, `cur = next`.
         * When `cur` is null, `prev` is the new head.
         *
         * Complexity: O(n) time (one pass), O(1) extra space — no allocation, pure pointer juggling.
         *
         * Pitfalls:
         *   - Empty list (`head == null`) and single node must work — the loop handles both because
         *     `prev` starts null, so the loop body just returns `head`/`null` correctly.
         *   - Forgetting to stash `next` before the flip orphans the rest of the list (the #1 bug).
         */
        fun referenceIterative(head: ListNode?): ListNode? {
            var prev: ListNode? = null
            var cur = head
            while (cur != null) {
                val next = cur.next
                cur.next = prev
                prev = cur
                cur = next
            }
            return prev
        }

        /**
         * Recursive variant (the follow-up). Recurse to the tail; that tail is the new head and
         * bubbles back up unchanged. On the way back, each node makes its successor point back at
         * it (`head.next.next = head`) and severs its own forward link (`head.next = null`) so the
         * old head ends up as the new tail.
         *
         * Complexity: O(n) time, O(n) space for the call stack — worth noting given the constraint
         * of up to 5000 nodes, the iterative version is the safer real-world choice (no stack risk).
         */
        fun referenceRecursive(head: ListNode?): ListNode? {
            if (head?.next == null) return head
            val newHead = referenceRecursive(head.next)
            head.next!!.next = head
            head.next = null
            return newHead
        }

    }
}
