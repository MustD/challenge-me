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
        fun test() = check(::middleNode)

        /**
         * Analysis — middleNode (verified: all 5 cases pass)
         *
         * Pattern: **Fast & slow pointers (tortoise and hare)** — the "runner" technique.
         * `fast` advances two nodes per step, `slow` one. When `fast` runs off the end,
         * `slow` sits at the midpoint. This is the same two-speed idea reused for
         * cycle detection (Floyd), finding the k-th-from-end node, and palindrome checks.
         *
         * Why the even-length rule falls out for free: the loop condition is
         * `while (fast?.next != null)`.
         *  - Odd length (e.g. 5): fast lands exactly on the last node, `fast.next == null`
         *    stops the loop, slow is on the single true middle (node 3).
         *  - Even length (e.g. 4): fast overshoots to `null` (last iteration does
         *    `fast = fast.next?.next`), slow has taken one extra step and rests on the
         *    *second* middle (node 3 of [1,2,3,4]) — exactly what the problem asks.
         *  A single-line change of the guard to `while (fast != null && fast.next != null)`
         *  would instead return the *first* middle on even input, so the condition here is
         *  load-bearing, not cosmetic.
         *
         * Time:  **O(n)**. `fast` traverses the list once at 2x, so the loop runs ~n/2
         *        iterations — linear in the number of nodes.
         * Space: **O(1)** auxiliary. Two pointers, no recursion, no allocation. The
         *        returned node is a reference into the existing list (output space, not
         *        counted). Iterative, so no call-stack growth.
         *
         * Correctness / edge cases:
         *  - Single node: `fast?.next` is null immediately, loop body never runs, returns head. Correct.
         *  - The `if (head == null) return head` guard is defensive — the constraints
         *    promise a non-empty list [1,100], so it never fires here, but it keeps the
         *    function total. The `?.` safe-calls already handle null internally, so this
         *    guard is belt-and-suspenders rather than strictly required.
         *
         * Alternative approaches:
         *  1. **Two-pass count**: walk once to get length n, walk again n/2 steps.
         *     Same O(n)/O(1) asymptotics but ~1.5n node visits vs. this one-pass ~1.5n
         *     (fast does n, slow does n/2) — a wash, but two-pass needs the full list
         *     up front. Fast/slow is the idiomatic single-pass answer.
         *  2. **Array/list buffer**: push all nodes into a list, index the middle.
         *     O(n) time but O(n) space — strictly worse; only worth it if you also need
         *     random access to other nodes.
         *  This solution is already optimal: you must touch every node to know where the
         *  end (and thus the middle) is, so no better than O(n) time is possible, and O(1)
         *  space is the floor for a pointer-returning traversal.
         *
         * Parallelism: **not applicable / not worth it.** A singly linked list is inherently
         *  sequential — you can't jump to node k without following k pointers, so the traversal
         *  can't be partitioned across threads without first materializing an index (which is
         *  itself the O(n) sequential walk). For n <= 100 any threading overhead dwarfs the work.
         *  Contrast with an array-backed "find middle": that's trivially O(1) with random access,
         *  which is precisely why real systems favor contiguous storage.
         *
         * Real-world: the runner technique shows up wherever you must find a positional
         *  landmark in a forward-only stream you'd rather not buffer or measure twice —
         *  e.g. splitting a linked structure for a merge-sort midpoint, or "last-N" windows
         *  over a stream. In practice, though, production code rarely hand-rolls linked lists
         *  (cache-unfriendly pointer chasing); you'd reach for an array/deque with O(1)
         *  indexing, and the "middle" problem evaporates. The interview value is the pointer
         *  discipline, not the data structure.
         */
        fun middleNode(head: ListNode?): ListNode? {
            if (head == null) return head
            var slow = head
            var fast = head
            while (fast?.next != null) {
                slow = slow?.next
                fast = fast.next?.next
            }

            return slow
        }

    }
}
