package leetcode.linked_list

import leetcode.expects
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 876. Middle of the Linked List  (https://leetcode.com/problems/middle-of-the-linked-list/)
 *
 * Given the head of a singly linked list, return the middle node of the linked list.
 * If there are two middle nodes (i.e. the list has an even length), return the second
 * of the two. The returned node is the head of the sublist from the middle onward.
 *
 * Constraints:
 * - The number of nodes in the list is in the range [1, 100] (never empty).
 * - 1 <= Node.val <= 100.
 * - Even length: return the *second* middle node (e.g. [1,2,3,4] -> node 3, not node 2).
 * - Edge cases to consider: single node, and even vs. odd length.
 */
typealias I0876 = (ListNode?) -> ListNode?

class I0876middleNode {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0876> {

        override val cases = leetcode.testCases<I0876>(
            "1,2,3,4,5" expects "3,4,5",       // odd length -> middle node 3
            "1,2,3,4,5,6" expects "4,5,6",     // even length -> second middle (node 4)
            "1,2" expects "2",                 // even length -> second middle
            "1,2,3,4" expects "3,4",           // even length -> second middle (node 3)
            "1" expects "1",                   // single node
        )

        @Test
        fun test() = check(
            ::middleNode,
            ::referenceSolution
        )

        fun middleNode(head: ListNode?): ListNode? {
            var slow = head
            var fast = head
            while (fast?.next != null) {
                slow = slow?.next
                fast = fast.next?.next
            }
            return slow
        }

        /**
         * Reference solution — 876. Middle of the Linked List
         *
         * Restatement: walk a singly linked list (1..100 nodes) and return the node in the
         * middle. For even length there are two middles; return the second one.
         *
         * Pattern: **fast & slow pointers (tortoise and hare / "runner")**. Two references
         * start at the head; one moves 2 nodes per step, the other 1. The fast one reaches
         * the end after ~n/2 steps, and at that moment the slow one has covered half the
         * list. It fits because a singly linked list has no length and no random access —
         * the speed ratio 2:1 converts "find the end" into "find the half" in a single pass.
         *
         * ### Intuition
         * If one runner is twice as fast as the other, when the fast runner finishes the
         * track the slow one is exactly halfway. You never need to know n.
         *
         * ### Core concepts
         * - **Two-speed traversal**: pointers advancing at different rates keep a fixed
         *   *ratio* of distance; here 2:1 pins slow to position ⌊n/2⌋. Same idea drives
         *   Floyd cycle detection and splitting a list for merge sort.
         * - **Loop guard chooses the middle**: `fast != null && fast.next != null` lands slow
         *   on the *second* middle for even n; `fast.next != null && fast.next.next != null`
         *   lands it on the *first*. Which guard you pick is the whole even-length rule.
         * - **Null-safe stepping**: `fast.next.next` may be null — the guard must ensure
         *   `fast.next` exists before the double hop.
         *
         * ### Approach
         * 1. `slow = head`, `fast = head`.
         * 2. While `fast` and `fast.next` are both non-null: `slow = slow.next`,
         *    `fast = fast.next.next`.
         * 3. Return `slow`.
         *
         * Trace [1,2,3,4]: (s=1,f=1) -> (s=2,f=3) -> (s=3,f=null) stop -> node 3 (second middle).
         * Trace [1,2,3]:   (s=1,f=1) -> (s=2,f=3), f.next null stop -> node 2.
         *
         * ### Complexity
         * - Time **O(n)**: fast visits each node at most once, ~n/2 iterations.
         * - Space **O(1)**: two references; the result is a node of the input list.
         *
         * ### Common pitfalls
         * - Using the "first middle" guard and returning node 2 for [1,2,3,4].
         * - Dereferencing `fast.next.next` without checking `fast.next` — NPE on even length.
         * - Returning `slow.val` or copying nodes: the expected output is the existing
         *   sublist starting at the middle node.
         * - Two-pass (count, then walk n/2) is also O(n)/O(1) and correct — just remember
         *   the index is `n / 2` (0-based) to get the second middle.
         */
        fun referenceSolution(head: ListNode?): ListNode? {
            var slow = head
            var fast = head
            while (fast?.next != null) {
                slow = slow!!.next
                fast = fast.next!!.next
            }
            return slow
        }
    }
}
