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
 * Given the `head` of a singly linked list, reverse the list, and return the reversed list.
 *
 * Constraints:
 * - The number of nodes in the list is the range [0, 5000].
 * - -5000 <= Node.val <= 5000
 *
 * Follow up: A linked list can be reversed either iteratively or recursively. Could you implement both?
 */
typealias I0206 = (ListNode?) -> ListNode?

class I0206reverseList {

    @Nested
    inner class Solution : ProblemTest<I0206> {

        override val cases = testCases<I0206>(
            "[1,2,3,4,5]" expects "[5,4,3,2,1]",
            "[1,2]" expects "[2,1]",
            "[]" expects "[]",
            "[1]" expects "[1]", // single node: loop runs once, head.next must become null
            "[1,2,3]" expects "[3,2,1]", // odd length: middle node keeps its position
            "[-5000,0,5000]" expects "[5000,0,-5000]", // value bounds, negatives and zero
            "[3,1,3,2]" expects "[2,3,1,3]", // duplicate values, non-palindromic (a no-op would fail)
            "[7,7,7]" expects "[7,7,7]", // all equal: only the structure changes, must not cycle
            """[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,
                36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,
                69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100]"""
                    expects """[100,99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,
                70,69,68,67,66,65,64,63,62,61,60,59,58,57,56,55,54,53,52,51,50,49,48,47,46,45,44,43,42,41,40,39,38,
                37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1]""", // longer list
        )

        @Test
        fun test() = check(::reverseList, ::referenceSolution, ::referenceSolution2)

        /**
         * ## Validation & analysis of your solution (iterative)
         *
         * **Verdict:** correct. It passes all 9 cases: the 3 originals plus single node, odd length, value bounds,
         * duplicates, all-equal, and a 100-node list.
         *
         * ### Time complexity: O(n)
         * The `while (curr != null)` loop runs exactly once per node, because `curr = next` advances one node per
         * iteration and nothing ever moves it back. The body is four reference assignments, each O(1). There is
         * no best/worst split: every node must be touched, since every node's `next` changes.
         *
         * ### Space complexity: O(1) auxiliary
         * Three references (`curr`, `prev`, `next`), no allocation and no recursion. The output is the *same*
         * nodes rewired in place, so it adds no output space either. Note that this means the input is
         * destroyed: after the call, `head` is the tail of the result.
         *
         * ### Correctness notes
         * - **Loop invariant:** before each iteration, `prev` heads the fully reversed list of the nodes already
         *   visited, and `curr` heads the untouched remainder. The two lists are disjoint and together hold all
         *   nodes. When `curr == null` the remainder is empty, so `prev` is the answer.
         * - **Save before overwrite:** `val next = curr.next` comes before `curr.next = prev`. Swapping those two
         *   lines would orphan the rest of the list. This ordering is the whole trick.
         * - **`prev = null` initialisation** makes the old head's `next` become `null` with no special case, so
         *   no `1 <-> 2` cycle forms. The all-equal and single-node cases rely on this.
         * - **Empty list:** the loop runs 0 times and returns `prev == null`, which is correct with no guard.
         * - Values are never read, so the value range (negatives, duplicates) is irrelevant. There are no
         *   overflow concerns.
         *
         * ### Pattern: in-place linked-list reversal (the prev / curr / next three-pointer walk)
         * This is a building block rather than a one-off. It appears inside Palindrome Linked List (234: reverse
         * the second half), Reverse Linked List II (92: reverse a sub-range), Reverse Nodes in k-Group (25),
         * Reorder List (143), and Add Two Numbers II (445). Recognising "I need to walk a list backwards, but it
         * is singly linked" should trigger it.
         *
         * ### Alternative approaches
         * - **Recursive** (`referenceSolution2`): also O(n) time, but O(n) stack. On the JVM, roughly 10^4 to
         *   10^5 frames overflow the default thread stack. n ≤ 5000 is safe, but the approach does not scale.
         *   Elegant, but strictly worse in space.
         * - **Stack / array of nodes, then relink:** O(n) time, O(n) extra space. Easier to reason about, but
         *   no benefit over yours.
         * - **Copy into a new reversed list** (prepend a fresh `ListNode` per value): O(n) time, O(n) space.
         *   Use it when the input must stay intact (a persistent or immutable list). It is how
         *   `List.reversed()` works on immutable data.
         * - **Your solution is asymptotically optimal.** Every one of the n `next` pointers has to change, so
         *   the lower bound is Ω(n) time, and you already reach O(1) extra space.
         *
         * ### Parallelism / multithreading
         * Not worth it. A linked list is inherently sequential: node k+1 can be reached only through node k, so
         * you cannot split the list into chunks without first walking it, and that walk is already the whole
         * O(n) cost. You could collect the nodes into an array and then rewire the chunks in parallel
         * (`arr[i].next = arr[i-1]` is embarrassingly parallel). However, the sequential collection pass
         * dominates (Amdahl's law), and pointer-chasing is bound by memory latency, not CPU. Threads would only
         * add overhead. The lesson is that pointer-based structures resist parallelism; arrays are what
         * parallelise well.
         *
         * ### Real-world experience
         * - You rarely reverse a linked list in production code. Most collections are array-backed
         *   (`ArrayList`, `ArrayDeque`), and reversal there is a two-pointer swap or just iterating backwards.
         *   `java.util.LinkedList` is doubly linked, so it offers `descendingIterator()` with no rewiring.
         * - The pattern does show up in low-level code: Linux kernel and allocator free-lists, and undo/redo
         *   logs. Classic examples are a garbage collector's mark stack, and Schorr–Waite pointer reversal,
         *   which traverses a graph with no extra stack by temporarily reversing pointers. Building a list by
         *   prepending (O(1)) and reversing once at the end is a standard idiom in functional languages
         *   (`foldLeft` + `reverse` in Scala, Haskell and Erlang), because prepending is the cheap operation
         *   on a cons list.
         * - In-place mutation is the trade-off to watch. If other code still holds `head`, it now sees a
         *   one-node list. In shared or concurrent code, prefer the copy variant.
         */
        fun reverseList(head: ListNode?): ListNode? {
            var curr = head
            var prev: ListNode? = null
            while (curr != null) {
                val next = curr.next
                curr.next = prev
                prev = curr
                curr = next
            }
            return prev
        }

        /**
         * ## Reference solution — iterative in-place pointer reversal
         *
         * **Restatement.** A singly linked list is a chain of arrows `1 -> 2 -> 3 -> null`. Reversing it means
         * flipping every arrow so the chain reads `null <- 1 <- 2 <- 3`, and returning the old tail (`3`) as the
         * new head. No new nodes are needed — only the `next` pointers change.
         *
         * **Pattern: in-place linked-list reversal ("prev / cur / next" three-pointer walk).** This is one of
         * the core linked-list building blocks. It fits because each node's new `next` is exactly the node
         * visited just before it — so a single left-to-right walk that remembers "the previous node" has all
         * the information needed to rewire every arrow.
         *
         * ### Intuition
         * Picture the list split into two parts: an already-reversed prefix (headed by `prev`) and the
         * untouched suffix (headed by `cur`). Each step moves one node from the front of the suffix onto the
         * front of the reversed prefix — like popping from one stack and pushing onto another. The only
         * danger: the moment you overwrite `cur.next`, you lose your only reference to the rest of the
         * suffix. So stash `cur.next` *before* rewriting it.
         *
         * ### Core concepts
         * - **Pointer rewiring** — changing a node's `next` rather than copying values; lets structural
         *   transformations run in O(1) extra space. Here it is the whole algorithm.
         * - **Save-before-overwrite** — whenever a mutation destroys your only path to data you still need,
         *   capture that path in a temp first. Here: `val next = cur.next` before `cur.next = prev`.
         * - **Loop invariant** — a statement true before every iteration: "`prev` heads a correctly reversed
         *   list of all nodes already visited; `cur` heads the untouched rest". When `cur == null` the invariant
         *   says `prev` is the full answer.
         * - **`null` as a sentinel** — starting `prev = null` means the original head's `next` automatically
         *   becomes `null`, making it a proper tail with no special case.
         *
         * **Approach.**
         * 1. `prev = null`, `cur = head`.
         * 2. While `cur != null`: `next = cur.next` (save), `cur.next = prev` (flip), `prev = cur`,
         *    `cur = next` (advance both).
         * 3. Return `prev` — the new head.
         *
         * **Complexity.** Time O(n) — each node is visited and rewired once. Space O(1) — three references,
         * no allocation.
         *
         * **Common pitfalls.**
         * - Flipping `cur.next` before saving it — the rest of the list is orphaned (the #1 bug).
         * - Returning `cur` or `head` instead of `prev` — at loop exit `cur` is `null` and `head` is now the tail.
         * - Forgetting that the old head must end with `next = null`; starting `prev = null` handles it —
         *   otherwise you get a cycle `1 <-> 2`.
         * - Empty list and single node: no special case needed — the loop runs 0 or 1 times and still
         *   returns the right thing.
         */
        fun referenceSolution(head: ListNode?): ListNode? {
            var prev: ListNode? = null
            var cur = head
            while (cur != null) {
                val next = cur.next // save before overwrite
                cur.next = prev     // flip the arrow
                prev = cur          // grow the reversed prefix
                cur = next          // shrink the untouched suffix
            }
            return prev
        }

        /**
         * ## Reference solution 2 — recursive (the follow-up)
         *
         * **Idea.** Trust the recursion: `reverse(head.next)` returns the head of the reversed tail, and after
         * that call `head.next` still points at the node that is now the *last* node of that reversed tail.
         * So `head.next.next = head` appends `head` to the end, and `head.next = null` makes it the new tail.
         * The new head (old tail) is found at the bottom of the recursion and passed back up unchanged.
         *
         * **Base case.** `head == null` (empty list) or `head.next == null` (last node — it is the new head).
         *
         * **Complexity.** Time O(n). Space O(n) — call-stack depth equals list length. With n <= 5000 this is
         * fine on the JVM, but this is why the iterative version is preferred in production: a very long list
         * would throw `StackOverflowError`.
         *
         * **Pitfall.** Omitting `head.next = null` leaves a 2-cycle between the first two nodes of the original
         * list; printing or comparing the result then loops forever.
         */
        fun referenceSolution2(head: ListNode?): ListNode? {
            if (head?.next == null) return head
            val newHead = referenceSolution2(head.next)
            head.next!!.next = head
            head.next = null
            return newHead
        }

    }
}
