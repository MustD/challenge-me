package leetcode.divide_conquer

import leetcode.expects
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

// LeetCode 148 — Sort List
// Given the head of a linked list, return the list sorted in ascending order.
// Follow-up: O(n log n) time and O(1) (or O(log n)) auxiliary space.
typealias I0148 = (ListNode?) -> ListNode?

class I0148sortList {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0148> {

        override val cases = leetcode.testCases<I0148>(
            "[4,2,1,3]" expects "[1,2,3,4]",
            "[-1,5,3,4,0]" expects "[-1,0,3,4,5]",
            "[]" expects "[]",
            "[1]" expects "[1]",
            "[2,1]" expects "[1,2]",
        )

        @Test
        fun test() = check(::sortList)

        /**
         * Top-down merge sort on a singly linked list.
         *
         * Pattern: divide & conquer (merge sort). The only sort that hits O(n log n)
         * on a linked list without random access — quicksort needs indexed pivots,
         * and merge sort is naturally suited to lists because merging is just pointer
         * relinking (no shifting like in an array).
         *
         * How it works:
         *   1. Find the middle with the slow/fast pointer trick. `slowPar` trails `slow`
         *      so we can sever the list into two halves (`slowPar?.next = null`). This
         *      cut is the load-bearing step — without it the recursion never shrinks.
         *   2. Recurse on each half.
         *   3. Merge the two sorted halves with a dummy-head splice.
         *
         * Time:  O(n log n). log n levels of recursion (list halves each time); each
         *        level does O(n) total work — the getMid traversal + the merge both
         *        walk every node once across all calls at that level.
         *
         * Space: O(log n) auxiliary — the recursion stack depth, one frame per level.
         *        No new nodes are allocated; `dummy` is a single throwaway per merge and
         *        merging only re-points `.next`, so this is genuinely in-place on the data.
         *        (This is the O(log n) variant of the follow-up. True O(1) space needs
         *        the bottom-up / iterative merge sort below.)
         *
         * Correctness / edge cases handled:
         *   - `head?.next == null` base case covers both empty (`null`) and single-node
         *     lists in one guard.
         *   - Two-node list: slow/fast loop runs once, splits cleanly into two singletons.
         *   - `tail.next = left ?: right` attaches the leftover tail in one shot — correct
         *     because at most one of the two lists is non-null when the loop exits.
         *   - Stability: the merge keeps `left` first only on strict `left.val < right.val`
         *     (here written as the inverted `>=` branch taking `right`), preserving the
         *     original relative order of equal keys. Not tested by the cases but worth
         *     knowing the choice of `<` vs `<=` is what makes a merge sort stable.
         *
         * Alternatives:
         *   - Bottom-up merge sort (iterative, doubling sublist width 1,2,4,…): same
         *     O(n log n) time but O(1) space since it drops the recursion stack. The
         *     genuinely optimal answer to the follow-up; trades simplicity for tricky
         *     index/relink bookkeeping.
         *   - Dump values into an array, sort, rebuild: O(n log n) time but O(n) space
         *     and arguably "cheating" — but in real code it's often exactly what you'd do.
         *
         * Parallelism: merge sort is the textbook divide-and-conquer candidate (sort the
         *   two halves on separate threads, then merge). But it does NOT pay off here:
         *   a linked list has no random access, so even splitting it is O(n) sequential,
         *   and the cache-unfriendly pointer chasing dominates. Parallel merge sort wins
         *   on large *arrays* (contiguous memory, parallel partition + parallel merge),
         *   not on lists, and only well past the thread-spawn-overhead crossover.
         *
         * Real world: you essentially never hand-sort a linked list — you'd reach for the
         *   stdlib (`Collections.sort` / `list.sortedBy { }`), which copies to an array,
         *   runs TimSort, and rebuilds. The place this exact algorithm earns its keep is
         *   *external merge sort*: data too big for RAM is sorted in sorted runs on disk
         *   and k-way merged via streaming cursors — the same merge-of-sorted-sequences
         *   idea, where sequential access (the list's strength) is the whole point.
         */
        fun sortList(head: ListNode?): ListNode? {
            if (head?.next == null) return head

            var slow = head
            var fast = head
            var slowPar: ListNode? = null
            while (fast?.next != null) {
                slowPar = slow
                slow = slow?.next
                fast = fast.next?.next
            }
            slowPar?.next = null

            val leftSorted = sortList(head)
            val rightSorted = sortList(slow)

            var left = leftSorted
            var right = rightSorted
            val dummy = ListNode(0)
            var tail = dummy

            while (left != null && right != null) {
                if (left.`val` >= right.`val`) {
                    tail.next = right
                    right = right.next
                } else {
                    tail.next = left
                    left = left.next
                }
                tail = tail.next!!
            }
            tail.next = left ?: right
            return dummy.next
        }


    }
}
