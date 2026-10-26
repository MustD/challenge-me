package leetcode.linked_list

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.utils.ListNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 21. Merge Two Sorted Lists  (https://leetcode.com/problems/merge-two-sorted-lists/)
 *
 * You are given the heads of two sorted linked lists `list1` and `list2`.
 *
 * Merge the two lists into one sorted list. The list should be made by splicing together the nodes of the first two
 * lists.
 *
 * Return the head of the merged linked list.
 *
 * Constraints:
 * - The number of nodes in both lists is in the range [0, 50].
 * - -100 <= Node.val <= 100
 * - Both list1 and list2 are sorted in non-decreasing order.
 */
typealias I0021 = (ListNode?, ListNode?) -> ListNode?

class I0021mergeTwoLists {

    @Nested
    inner class Solution : ProblemTest<I0021> {

        override val cases = _root_ide_package_.leetcode.testCases<I0021>(
            args("[1,2,4]", "[1,3,4]") expects "[1,1,2,3,4,4]",
            args("[]", "[]") expects "[]",
            args("[]", "[0]") expects "[0]",
            args("[1]", "[]") expects "[1]",                                    // list2 empty, list1 not
            args("[2]", "[1]") expects "[1,2]",                                 // single nodes, list2 head smaller
            args(
                "[1,2,3]",
                "[4,5,6]"
            ) expects "[1,2,3,4,5,6]",                 // list1 exhausted first, list2 tail spliced
            args(
                "[4,5,6]",
                "[1,2,3]"
            ) expects "[1,2,3,4,5,6]",                 // list2 exhausted first, list1 tail spliced
            args("[1,1,1]", "[1,1]") expects "[1,1,1,1,1]",                     // all equal values (ties every step)
            args(
                "[-100,0,100]",
                "[-100,-50,100]"
            ) expects "[-100,-100,-50,0,100,100]", // value bounds, negatives, ties at ends
            args(
                "[1,3,5,7,9]",
                "[2,4,6,8,10,12,14]"
            ) expects "[1,2,3,4,5,6,7,8,9,10,12,14]", // interleave, then leftover run
        )

        @Test
        fun test() = check(
            ::mergeTwoLists,
            ::referenceSolution
        )

        /**
         * ## Validation — `mergeTwoLists` (your solution)
         *
         * **Verdict: correct.** Passes the 3 official examples plus 7 added cases (one side empty, single nodes,
         * either list exhausted first, all-equal ties, value bounds -100/100, interleave + leftover run).
         *
         * **Pattern: two-pointer merge with a dummy (sentinel) head + tail pointer.** This is the merge step of
         * merge sort on linked lists. Since both inputs are sorted, the global minimum of what's left is always one
         * of the two current heads, so greedily taking the smaller head is always safe.
         *
         * ### Time — O(n + m)
         * - The `while (lH != null && rH != null)` loop advances exactly one of `lH`/`rH` per iteration, so it runs
         *   at most `n + m - 1` times, doing O(1) work (one compare, two pointer writes) each time.
         * - The leftover is attached by a single `pointer?.next = …` assignment, not walked node by node, so the
         *   tail costs O(1). Best case is O(min(n, m)): e.g. `[1,2,3]` + `[4,5,6]` loops 3 times and splices the rest.
         *
         * ### Space — O(1) auxiliary
         * - Only `result` (one sentinel node) and three pointers. The output reuses the input nodes (splicing), so
         *   there is no output allocation either. Iterative, so no recursion stack.
         *
         * ### Correctness notes
         * - **Empty inputs** are handled without special cases: the loop is skipped and the non-null side (or
         *   nothing) is attached to the sentinel; `result.next` is then the answer or `null`.
         * - **`<=` on ties** takes from `list1` first, which keeps the merge *stable* — invisible on plain ints, but
         *   it is what makes merge sort stable on records.
         * - **The sentinel's value (`-1`) is never observed** because you return `result.next` — good; returning
         *   `result` would leak it.
         * - **Inputs are mutated** (their `next` pointers are rewired). That's what "splicing" asks for, but in real
         *   code it means the caller must not keep using `list1`/`list2` as independent lists afterwards.
         *
         * ### Kotlin-level observations (style, not bugs)
         * - `var pointer: ListNode? = result` makes every access `pointer?.…`. It can never actually be null here
         *   (it starts at a node and only moves onto a node just attached), so the `?.` calls are dead safety that
         *   would *silently no-op* instead of failing loudly if a future edit broke the invariant. Typing it as
         *   `ListNode` and advancing with `pointer = pointer.next!!` states the invariant explicitly.
         * - The two trailing `?.let` lines are mutually exclusive (at most one list is non-empty after the loop) and
         *   collapse to one assignment: `pointer.next = lH ?: rH`.
         *
         * ### Alternatives
         * - **Recursive merge** — `if (a.val <= b.val) { a.next = merge(a.next, b); a } else …`. Shorter and
         *   elegant, same O(n + m) time, but O(n + m) call-stack depth; fine at n ≤ 50, a StackOverflowError risk
         *   on long lists. Yours is the better default.
         * - **Copying nodes** (`ListNode(x.val)` per element, as in your older version of this problem) — leaves the
         *   inputs untouched at O(n + m) extra memory. Useful when the inputs are shared/immutable.
         * - **Concatenate + sort** — O((n + m) log(n + m)); throws away the sortedness. Strictly worse.
         * - Your solution is already asymptotically optimal: every node must be placed in the output at least once,
         *   so Ω(n + m) time is a lower bound, and O(1) extra space can't be beaten.
         *
         * ### Parallelism
         * - Not worth it here. Each step depends on the previous one (which head is next), and a linked list has no
         *   random access, so you can't split it into independent chunks without first walking it — which is already
         *   the whole cost. With ≤ 100 nodes, thread start-up dwarfs the work.
         * - On *arrays* merging does parallelize: **parallel merge** (pick the median of the larger array, binary
         *   search its position in the other, recurse on both halves) gives O(log² n) span. That's what parallel
         *   merge sorts (e.g. `Arrays.parallelSort`) build on — but only pays off at hundreds of thousands of elements.
         *
         * ### Real-world experience
         * - **k-way merge** (LC 23) — merging many sorted runs with a min-heap; the core of external sorting, LSM-tree
         *   compaction (RocksDB, Cassandra, LevelDB merge sorted SSTables), and database sort-merge joins.
         * - **Merging sorted streams** — time-ordered log/event streams from several sources, Kafka partitions
         *   re-ordered by timestamp, or `Flow`/reactive merge operators; there the input is unbounded, so you only
         *   ever hold the current heads, exactly like your two pointers.
         * - In production you'd rarely hand-roll it on linked lists (they're cache-unfriendly: each `next` is a
         *   potential cache miss); arrays/`ArrayDeque` plus a library merge or `sortedWith` on a small input often
         *   wins in practice despite worse Big-O.
         */
        fun mergeTwoLists(list1: ListNode?, list2: ListNode?): ListNode? {
            val result = ListNode(-1)

            var pointer: ListNode? = result
            var lH = list1
            var rH = list2

            while (lH != null && rH != null) {

                if (lH.`val` <= rH.`val`) {
                    pointer?.next = lH
                    lH = lH.next
                } else {
                    pointer?.next = rH
                    rH = rH.next
                }
                pointer = pointer?.next

            }
            lH?.let { pointer?.next = lH }
            rH?.let { pointer?.next = rH }
            return result.next
        }

        /**
         * ## Reference solution — Merge Two Sorted Lists
         *
         * **Restatement.** Two singly linked lists are each already sorted ascending. Produce one sorted list that
         * contains every node of both, by re-linking the existing nodes (no copies needed).
         *
         * **Pattern: two-pointer merge with a dummy (sentinel) head.** This is exactly the *merge* step of merge sort,
         * applied to linked lists. Because both inputs are sorted, the smallest remaining element overall is always at
         * the front of one of the two lists — so a greedy "take the smaller head" is always correct.
         *
         * ### Intuition
         * At every step you only need to compare the two current heads; whichever is smaller is the next node of the
         * answer. Once one list runs out, the rest of the other list is already sorted and larger-or-equal than
         * everything placed so far, so it can be attached in one pointer assignment — no need to walk it.
         *
         * ### Core concepts
         * - **Merge of sorted sequences** — combining two sorted runs in O(n + m) by repeatedly taking the smaller
         *   front element. The building block of merge sort, k-way merge (LC 23), and external sorting.
         * - **Dummy / sentinel head** — a throwaway node placed before the real head so that "append to the result"
         *   is uniform even when the result is empty. Removes the special case for choosing the first node; return
         *   `dummy.next`.
         * - **Tail pointer** — keep a reference to the last node of the result so appends are O(1) instead of
         *   re-walking the list.
         * - **Splicing vs. copying** — re-pointing `next` of existing nodes reuses them (O(1) extra space);
         *   allocating `ListNode(x.val)` for each element is correct but costs O(n + m) extra memory.
         * - **Stability** — on ties, taking from `list1` first (`<=`) keeps equal elements in their original relative
         *   order, the property that makes merge sort stable.
         *
         * ### Approach
         * 1. Create `dummy = ListNode(0)` and `tail = dummy`; let `a = list1`, `b = list2`.
         * 2. While both `a` and `b` are non-null: attach the smaller of the two (`a` on ties) to `tail.next`, advance
         *    that list's pointer, and advance `tail`.
         * 3. One list is now exhausted; attach the other one wholesale: `tail.next = a ?: b`.
         * 4. Return `dummy.next`.
         *
         * A recursive variant is also common: `if a.val <= b.val then a.next = merge(a.next, b); return a` — elegant,
         * but it uses O(n + m) call stack.
         *
         * ### Complexity
         * - **Time O(n + m)** — every node is visited at most once, with one comparison per step.
         * - **Space O(1)** — only a few pointers; the nodes themselves are reused (the dummy is constant overhead).
         *
         * ### Common pitfalls
         * - **Empty inputs** (`[]`, `[]` or one side empty) — the dummy head + `a ?: b` handles them without extra
         *   branches; forgetting them leads to NPEs or returning `null` instead of the non-empty list.
         * - **Forgetting to advance `tail`** after attaching a node — the next append overwrites the previous one.
         * - **Walking the leftover list node by node** — unnecessary; a single `tail.next = rest` suffices.
         * - **Returning `dummy` instead of `dummy.next`** — leaks the sentinel `0` into the answer.
         * - **Using `<` instead of `<=`** — still a correct sorted result, but loses stability (matters in merge sort
         *   on records, not on plain ints).
         */
        fun referenceSolution(list1: ListNode?, list2: ListNode?): ListNode? {
            val dummy = ListNode(0)
            var tail = dummy
            var a = list1
            var b = list2
            while (a != null && b != null) {
                if (a.`val` <= b.`val`) {
                    tail.next = a
                    a = a.next
                } else {
                    tail.next = b
                    b = b.next
                }
                tail = tail.next!!
            }
            tail.next = a ?: b
            return dummy.next
        }

    }
}
