package leetcode.divide_conquer

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test

/**
 * 23. Merge k Sorted Lists  (https://leetcode.com/problems/merge-k-sorted-lists/)
 *
 * You are given an array of `k` linked lists, each sorted in ascending order. Merge all of them
 * into a single sorted linked list and return its head.
 *
 * Constraints:
 * - k == lists.length, 0 <= k <= 10^4
 * - 0 <= lists[i].length <= 500, and each lists[i] is sorted ascending
 * - -10^4 <= lists[i][j] <= 10^4
 * - the sum of all lists[i].length will not exceed 10^4
 *
 * Edge cases worth handling:
 * - empty outer array (`[]`) -> empty result (null head)
 * - an array of empty lists (`[[]]`, `[[],[]]`) -> null head
 * - some lists empty, others not
 */
typealias I0023 = (Array<ListNode?>) -> ListNode?

class I0023mergeKLists {

    @Nested
    inner class Solution : ProblemTest<I0023> {

        override val cases = testCases<I0023>(
            "[[1,4,5],[1,3,4],[2,6]]" expects "[1,1,2,3,4,4,5,6]",
            "[]" expects "[]",
            "[[]]" expects "[]",
            "[[],[]]" expects "[]",
            "[[1],[0]]" expects "[0,1]",
        )

        @Test
        fun test() = check(::mergeKLists, ::referenceSolution)

        /**
         * Reference (alternative): **min-heap / k-way merge**.
         *
         * Restatement: given `k` already-sorted linked lists, splice them into one sorted list.
         *
         * Intuition / why a heap fits: at any moment the next node of the global result must be
         * the smallest among the *current heads* of the lists that still have nodes. A min-heap
         * is exactly the data structure that hands you "the smallest of k things" in `O(log k)` and
         * lets you cheaply replace it with the head's successor. So the loop is just: pop the
         * smallest head, append it, push its `next`. This is the canonical **k-way merge** pattern
         * (vs. the divide & conquer in `mergeKLists` above — both are `O(N log k)`; this one keeps
         * the `k` "frontier" pointers in an explicit heap instead of an implicit recursion tree).
         *
         * Approach:
         *   1. Seed the heap with every non-null list head (skip nulls — handles `[[]]`, `[[],[]]`).
         *   2. Pop the min head, link it onto a `dummy` tail; if it has a `next`, push that.
         *   3. Repeat until the heap drains; return `dummy.next`.
         *
         * Complexity:
         *   - Time **O(N log k)**: each of `N` nodes is pushed and popped exactly once, every heap op
         *     is `O(log k)` because the heap never holds more than `k` elements (one frontier node
         *     per list).
         *   - Space **O(k)**: the heap holds at most one node per list. (Result reuses input nodes.)
         *
         * Pitfalls:
         *   - Forgetting to skip null heads when seeding -> NPE / wrong size. Filtered here.
         *   - Comparator must compare on `.val`, not object identity, or you don't get sorted output.
         *   - Empty outer array and arrays of empty lists both yield an empty heap -> `dummy.next` is
         *     null, which is the correct empty result.
         */
        fun referenceSolution(lists: Array<ListNode?>): ListNode? {
            val heap = PriorityQueue<ListNode>(compareBy { it.`val` })
            lists.forEach { head -> if (head != null) heap.add(head) }

            val dummy = ListNode(0)
            var current = dummy
            while (heap.isNotEmpty()) {
                val node = heap.poll()
                current.next = node
                current = node
                node.next?.let { heap.add(it) }
            }
            return dummy.next
        }

        /**
         * Approach: **divide & conquer** (pairwise merge via recursion).
         * Split the `k` lists in half, recursively merge each half, then do a standard
         * two-pointer merge of the two sorted results. Recursion tree is `log k` deep.
         *
         * Pattern: "merge sort over a collection of lists" — same shape as the merge step
         * of merge sort, applied to k sequences instead of array halves. The reusable idea
         * is *pairing up* sub-results so each element is touched once per level, not once
         * per list.
         *
         * Let `N` = total number of nodes across all lists, `k` = number of lists.
         *
         * Time:  **O(N log k)**.
         *   - The recursion has `log k` levels. At each level, every one of the `N` nodes is
         *     visited exactly once across all the merges on that level (the work is partitioned
         *     by `sliceArray`, no node merged twice within a level). `log k` levels x `O(N)`
         *     work each = `O(N log k)`. This beats naive sequential merging (merge list 2 into
         *     1, then 3, ...) which is `O(N k)` because early nodes get re-scanned every round.
         *   - Minor caveat in *this* implementation: when one side runs out, the
         *     `current.next = first ?: second` branch (lines ~54-57) advances only one node per
         *     loop iteration instead of splicing the whole remaining tail in O(1). It still
         *     walks the tail node-by-node, so it stays linear — no change to the asymptotic
         *     bound, just a few extra pointer writes. Splicing the rest (`current.next = first ?: second; break`)
         *     would be marginally cheaper.
         *
         * Space: **O(log k)** auxiliary (recursion-stack depth = height of the divide tree),
         *   plus the `sliceArray` copies: each level allocates new arrays totaling `O(k)` slots,
         *   so `O(k)` transient array space across the live recursion path. Output list is built
         *   by re-linking existing nodes (the `dummy`/`current` splice), so no per-node allocation
         *   for the result — that's working space `O(1)` beyond the stack. Distinguishing: output
         *   space is reused input nodes (not counted as extra); the real extra is the stack + slices.
         *
         * Correctness / edge cases:
         *   - `lists.isEmpty()` -> null (handles `[]`).
         *   - `size == 1` returns that single (possibly-null) head -> handles `[[]]` since the
         *     element itself is null.
         *   - Lists that are individually empty (null heads) flow through the merge as
         *     "already exhausted" and contribute nothing -> `[[],[]]` yields null. Verified by tests.
         *   - Tie-breaking: on equal values the `else` branch takes `second`, so equal nodes from
         *     the right half precede those from the left. Doesn't affect sorted validity; only
         *     matters if stability across original-list order were required (it isn't here).
         *
         * Alternatives:
         *   - **Min-heap (priority queue)** of the `k` current heads: pop the min, push its `next`.
         *     Time `O(N log k)` (same as this), space `O(k)` for the heap. Trades the recursion
         *     stack for an explicit heap; often preferred when lists arrive as a stream/iterator
         *     since it doesn't need them all up front.
         *   - **Sequential pairwise merge** (fold-left, merge into an accumulator): simple,
         *     `O(N k)` time — strictly worse; good only as a baseline.
         *   This divide & conquer is already asymptotically optimal: every node must be inspected
         *   at least once and comparison-based ordering of `k` sources costs `log k` per element,
         *   so `O(N log k)` is the lower bound for the comparison model.
         *
         * Parallelism: the divide step is genuinely parallelizable — the two recursive
         *   `mergeKLists` calls operate on disjoint slices with no shared mutable state, so they
         *   could run on separate threads (fork/join), and a parallel merge sort is the textbook
         *   example. Realistic speedup ceiling is low here, though: with `N <= 10^4` the merge work
         *   is pointer-chasing dominated by memory latency, and thread/fork overhead dwarfs the
         *   per-node cost. By Amdahl's law the sequential two-pointer merge at the root (which must
         *   touch all `N` nodes) caps the win. Worth it only at much larger `N` with cache-friendly
         *   layouts — not for this input size.
         *
         * Real-world: this is exactly **k-way merge**, the backbone of external merge sort and
         *   LSM-tree compaction (RocksDB/LevelDB/Cassandra merge sorted SSTable runs), and of
         *   merging sorted shards from distributed queries. At real scale the heap variant wins
         *   because the runs are streamed from disk/network (you can't materialize them as arrays),
         *   and you'd reach for a library loser-tree / `java.util.PriorityQueue` rather than hand-
         *   rolled recursion. Linked lists themselves are rare in hot paths (poor cache locality);
         *   production code merges iterators/byte streams instead.
         */
        fun mergeKLists(lists: Array<ListNode?>): ListNode? {
            if (lists.isEmpty()) return null
            if (lists.size == 1) return lists.first()

            val half = lists.size / 2
            var first = mergeKLists(lists.sliceArray(0 until half))
            var second = mergeKLists(lists.sliceArray(half..lists.lastIndex))

            val dummy = ListNode(0)
            var current = dummy
            while (first != null || second != null) {

                if (first == null || second == null) {
                    current.next = first ?: second
                    first?.let { first = first.next }
                    second?.let { second = second.next }
                } else {
                    if (first.`val` < second.`val`) {
                        current.next = first
                        first = first.next
                    } else {
                        current.next = second
                        second = second.next
                    }
                }
                current = current.next!!
            }
            return dummy.next
        }

    }
}
