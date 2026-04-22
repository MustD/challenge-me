package leetcode.linked_list

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0025 = (ListNode?, Int) -> ListNode?

/**
 * https://leetcode.com/problems/reverse-nodes-in-k-group/description
 *
 * Given the head of a linked list, reverse the nodes k at a time and return the modified list.
 * If fewer than k nodes remain at the tail, leave them as-is.
 *
 * Example: [1,2,3,4,5], k=2  →  [2,1,4,3,5]
 * Example: [1,2,3,4,5], k=3  →  [3,2,1,4,5]   (last 2 nodes stay unchanged)
 *
 * ─────────────────────────────────────────────────────────────────
 * ALGORITHM: Iterative group-by-group reversal with a sentinel node
 * ─────────────────────────────────────────────────────────────────
 *
 * Key insight: treat the list as a sequence of k-node windows.
 * Within each window we do a standard in-place reversal, then stitch
 * the reversed segment back between the previous tail and the next window.
 *
 * Visualisation for [1→2→3→4→5], k=2:
 *
 *   dummy → 1 → 2 → 3 → 4 → 5
 *   ^^^^
 *   groupPrev (sentinel, never changes position — only advances)
 *
 *   Pass 1 – locate kth=2, nextGroupHead=3
 *             reverse segment [1→2]: prev starts at 3, curr starts at 1
 *             result: 2 → 1 → 3 (1.next was set to 3 during reversal)
 *             wire:   dummy.next = 2,  groupPrev advances to 1
 *   dummy → 2 → 1 → 3 → 4 → 5
 *                ↑groupPrev
 *
 *   Pass 2 – locate kth=4, nextGroupHead=5
 *             reverse [3→4]: same technique
 *   dummy → 2 → 1 → 4 → 3 → 5
 *                        ↑groupPrev
 *
 *   Pass 3 – only 1 node (5) remains, k=2 → getKth returns null → stop
 *
 *   Return dummy.next = 2  →  [2,1,4,3,5] ✓
 *
 * Time  O(n)  – every node is visited twice (once by getKth, once by reversal)
 * Space O(1)  – only a constant number of pointers
 */
class I0025reverseNodesInKGroup {

    @Nested
    inner class Solution : ProblemTest<I0025> {

        // args() creates a multi-argument TestInput so both parameters are passed separately
        override val cases = testCases<I0025>(
            args("[1,2,3,4,5]", 2) expects "[2,1,4,3,5]",
            args("[1,2,3,4,5]", 3) expects "[3,2,1,4,5]",
        )

        @Test
        fun test() = check(::solution1)


        // ── Main solution ─────────────────────────────────────────────────────
        fun solution1(head: ListNode?, k: Int): ListNode? {
            // ── Helper ────────────────────────────────────────────────────────────
            // Walk k steps from `node`. Returns the node we land on, or null if the
            // list ends before we complete k steps (meaning this group is incomplete).
            fun getKth(node: ListNode, k: Int): ListNode? {
                var curr: ListNode? = node
                repeat(k) { curr = curr?.next }
                return curr
            }

            // Sentinel node: gives groupPrev a valid starting point so we never
            // have to special-case the very first group.
            val dummy = ListNode(0).apply { next = head }

            // groupPrev: the node immediately BEFORE the current k-group.
            // After reversing a group it advances to the original group head
            // (which is now the group's TAIL after reversal).
            var groupPrev: ListNode = dummy

            while (true) {
                // ── Step 1: check whether k nodes are available ──────────────
                // getKth walks k steps from groupPrev. If the chain is too short
                // it returns null, meaning the remaining nodes stay unchanged.
                val kth = getKth(groupPrev, k) ?: break

                // The node right after this k-group — we'll need it to reconnect
                // the reversed segment to the rest of the list.
                val nextGroupHead = kth.next

                // ── Step 2: reverse the k-node segment [groupPrev.next .. kth] ─
                //
                // Standard three-pointer reversal, but we initialise prev to
                // nextGroupHead so the last node of the reversed segment
                // automatically points to the remainder of the list.
                //
                // Before:  groupPrev → [A → B → … → kth] → nextGroupHead
                // After:   groupPrev   [kth → … → B → A] → nextGroupHead
                //
                val groupHead = groupPrev.next!! // save original head — it becomes the new tail
                var prev: ListNode? = nextGroupHead
                var curr: ListNode? = groupHead
                while (curr != nextGroupHead) {
                    val nxt = curr!!.next   // save next before overwriting the pointer
                    curr.next = prev        // reverse the pointer
                    prev = curr             // advance prev
                    curr = nxt              // advance curr
                }
                // `prev` now points to `kth`, which is the new head of the group.

                // ── Step 3: stitch the reversed group back into the list ──────
                groupPrev.next = prev       // connect predecessor → new group head (kth)
                // groupHead.next already == nextGroupHead (was set in the loop above)

                // ── Step 4: advance groupPrev for the next iteration ──────────
                // groupHead is the original first node — after reversal it is the
                // group's TAIL. The next group starts at nextGroupHead.
                groupPrev = groupHead
            }

            return dummy.next
        }
    }
}
