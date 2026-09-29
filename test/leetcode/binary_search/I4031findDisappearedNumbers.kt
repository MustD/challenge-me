package leetcode.binary_search

import leetcode.expects
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test

/**
 * 4031. Find All Numbers Disappeared in an Array II  (https://leetcode.com/problems/find-all-numbers-disappeared-in-an-array-ii/)
 *
 * You are given an integer array `nums` and two integers `lower` and `upper`.
 *
 * A **missing integer** is an integer in the inclusive range `[lower, upper]` that does not appear in `nums`.
 *
 * Return a 2D integer array where each element is of the form `[start, end]`, representing a **contiguous** range of
 * missing integers. Return the ranges in **increasing** order. If there are no missing integers, return an empty array.
 *
 * **Note:** Consecutive missing integers should be grouped into a single range.
 *
 * Constraints:
 * - 1 <= nums.length <= 10^5
 * - 1 <= nums[i] <= 10^5
 * - 1 <= lower <= upper <= 10^5
 */
typealias I4031 = (IntArray, Int, Int) -> List<List<Int>>

class I4031findDisappearedNumbers {

    @Nested
    inner class Solution : leetcode.ProblemTest<I4031> {

        override val cases = leetcode.testCases<I4031>(
            leetcode.args("[3,9,7]", 1, 12) expects "[[1,2],[4,6],[8,8],[10,12]]",
            leetcode.args("[1,1]", 5, 7) expects "[[5,7]]",
            leetcode.args("[2,3,5]", 2, 3) expects "[]",
            leetcode.args("[1]", 1, 1) expects "[]", // minimum size, range fully covered
            leetcode.args("[100000]", 1, 1) expects "[[1,1]]", // every num outside the range
            leetcode.args("[100000]", 99999, 100000) expects "[[99999,99999]]", // max values, trims right end
            leetcode.args("[3,1,2]", 1, 3) expects "[]", // unsorted, whole range covered, interval shrinks to nothing
            leetcode.args("[4,4,2,2]", 1, 5) expects "[[1,1],[3,3],[5,5]]", // duplicates hit an already-removed point
            leetcode.args("[3,4]", 1, 6) expects "[[1,2],[5,6]]", // num == from of a NON-first interval
            leetcode.args("[2,4,5]", 1, 6) expects "[[1,1],[3,3],[6,6]]", // left-edge trim after two splits
            leetcode.args("[3,2]", 1, 5) expects "[[1,1],[4,5]]", // num == to of a NON-last interval
            leetcode.args("[2,2,2]", 1, 3) expects "[[1,1],[3,3]]", // repeated split point: lowerEntry lands on a gap
            leetcode.args("[5,4,3,2,1]", 1, 6) expects "[[6,6]]", // strictly decreasing, right-edge trims chain
            leetcode.args("[1,3,5,7,9]", 1, 9) expects "[[2,2],[4,4],[6,6],[8,8]]", // alternating, both range ends hit
            leetcode.args("[50000]", 1, 100000) expects "[[1,49999],[50001,100000]]", // full constraint range
        )

        @Test
        fun test() = check(::findDisappearedNumbers, ::referenceSolution)

        /**
         * ## Analysis (verified: all 15 cases pass)
         *
         * **Pattern:** *interval splitting / "punching holes" in a range set*, using an ordered map keyed by interval
         * start (`TreeMap<Int, Node>`), the same structure as LeetCode 352 / 715 (Range Module) and a disjoint
         * interval set. The idea: start with one interval `[lower, upper]` of "missing" values, and for each `num`
         * find the interval that holds it with a floor lookup, then trim it (num at an edge) or split it (num inside).
         *
         * **Time: O(n log k)**, where k is the number of live intervals (k <= min(n, upper - lower + 1) + 1).
         * - Each `num` does one `index[num]` + one `lowerEntry(num)`: O(log k). `index[num] ?: lowerEntry(num)` is
         *   just `floorEntry(num)` written as two lookups, so a single `floorEntry` would do.
         * - Trim: `remove` + `put` = O(log k). Split (`replace`): one `remove` + two `put` = O(log k).
         * - Final `index.map`: O(k). The TreeMap iterates in key order, so the output is already sorted, which is
         *   the "increasing order" requirement satisfied for free.
         * - Worst case: n = 1e5 distinct values that each split, about 1e5 * 17 comparisons. Fast.
         *
         * **Space: O(k)** working space for the TreeMap entries + `Node` objects (O(n) in the worst case), plus
         * O(k) for the output. No recursion.
         *
         * **Correctness notes**
         * - Invariant: the TreeMap holds exactly the maximal missing intervals seen so far, keyed by `from`, all
         *   disjoint. Every branch preserves it: an interval is removed from `index` *before* its `from` is mutated,
         *   then re-inserted under the new key; an emptied interval (`from > to`) is simply not re-inserted.
         * - Out-of-range values: `num < lower` gives no floor entry (early return); `num > upper`, or a `num` that
         *   falls in a gap already removed (duplicates, e.g. `[2,2,2]`), gets a floor node whose range excludes
         *   it, and `num in node.from..node.to` filters it out. Duplicates are therefore idempotent.
         * - A single-point interval hit by its own value runs both `from++` and `to--`, becoming empty, which is correct.
         *
         * **Dead code worth noticing:** the output is built from `index` alone, so the `prev`/`next` doubly linked
         * list is never read. That's lucky, because `unlink` is a no-op: `prev.next = next?.prev` assigns `node`
         * back to itself (since `next.prev == node`), and then `next.prev = prev?.next` does the same. The
         * list therefore goes stale, but nothing observes it. Takeaway: when an ordered map already gives you
         * neighbours (`lowerEntry`/`higherEntry`), a hand-rolled linked list next to it is redundant state that
         * can quietly disagree with the map. If you drop `prev`/`next`, `replace` and `unlink`, Node becomes a plain
         * `(from, to)` pair.
         *
         * **Alternatives**
         * - *Sort + sweep:* sort `nums`, walk with a cursor `expected = lower`; each time `nums[i] > expected` emit
         *   `[expected, min(nums[i] - 1, upper)]`, then `expected = max(expected, nums[i] + 1)`; finish with
         *   `[expected, upper]`. O(n log n) time, O(1) extra space (in-place sort, but it mutates the input). Simpler, and
         *   no per-interval allocations.
         * - *Presence bitmap (best here):* since values are bounded (<= 1e5), mark `seen[num - lower]` for in-range
         *   nums, then do one pass over `[lower, upper]` grouping runs of `false`. O(n + R) time and O(R) space with
         *   R = upper - lower + 1: linear, branch-light and cache-friendly. This is the asymptotically optimal choice
         *   because you must read every num and emit every range anyway.
         * - Your TreeMap version wins when the range is huge or unbounded (e.g. 1..1e18, where no bitmap fits) and/or
         *   the values arrive *online* and you need the current missing ranges after each insertion. It is the only
         *   one of the three that supports incremental updates.
         *
         * **Parallelism:** the bitmap version is embarrassingly parallel. Partition `nums` across threads to set bits
         * (idempotent writes, or use per-thread bitsets OR-ed together), then partition `[lower, upper]` into chunks,
         * find runs per chunk, and stitch runs that cross chunk borders. At n, R <= 1e5 the thread start-up and
         * merge overhead outweighs the O(n) work, so it isn't worth it at LeetCode scale. The TreeMap version doesn't
         * parallelise: every insertion depends on the map state left by the previous one.
         *
         * **Real world:** this is a "gap detection" problem. It shows up as finding missing sequence numbers in a
         * message stream / TCP SACK blocks, unallocated ID or IP ranges (free-list allocators), missing partitions or
         * dates in a time-series backfill, and Kafka offset gaps. Production tools use exactly your structure for
         * the streaming case: Guava's `TreeRangeSet` (`complement()` gives the missing ranges), roaring bitmaps
         * for dense integer sets, and in SQL the "gaps and islands" `LEAD()`/`ROW_NUMBER()` pattern, which is the
         * sort + sweep solution in declarative form.
         */
        fun findDisappearedNumbers(nums: IntArray, lower: Int, upper: Int): List<List<Int>> {
            data class Node(var from: Int, var to: Int) {
                var prev: Node? = null
                var next: Node? = null
            }

            val first = Node(lower, upper)
            val index = TreeMap<Int, Node>()
            index[first.from] = first

            fun replace(node: Node, left: Node, right: Node) {
                val prev = node.prev
                val next = node.next
                prev?.let { it.next = left }
                next?.let { it.prev = right }
                left.prev = prev
                left.next = right
                right.prev = left
                right.next = next
                index.remove(node.from)
                index[left.from] = left
                index[right.from] = right
            }

            fun unlink(node: Node) {
                val prev = node.prev
                val next = node.next
                prev?.let { it.next = next?.prev }
                next?.let { it.prev = prev?.next }
            }

            nums.forEach { num ->
                val node = index[num] ?: index.lowerEntry(num)?.value ?: return@forEach
                if (num in node.from..node.to) {
                    if (node.from == num || node.to == num) {
                        index.remove(node.from)

                        if (node.from == num) node.from++
                        if (node.to == num) node.to--

                        if (node.from > node.to) unlink(node)
                        else index[node.from] = node

                        return@forEach
                    }

                    val left = Node(node.from, num - 1)
                    val right = Node(num + 1, node.to)
                    replace(node, left, right)
                }

            }

            return index.map { listOf(it.value.from, it.value.to) }
        }

        /**
         * ## Reference solution: presence bitmap + run scan
         *
         * **Restatement.** Take the window `[lower, upper]`, cross out every value that appears in `nums`, and
         * report what is left as maximal runs `[start, end]`, from left to right.
         *
         * **Pattern: bounded-domain presence array ("counting-sort style" marking) + run-length grouping.** When the
         * values are small bounded integers (here <= 1e5), you can skip sorting and ordered maps: a boolean array
         * indexed by value answers "is x present?" in O(1), and a single left-to-right sweep over the domain yields
         * the gaps already sorted. This is the same "gaps and islands" idea as LC 163 (Missing Ranges), only with
         * unsorted input and duplicates.
         *
         * ### Intuition
         * The output is ordered by *value*, not by input position. So iterate over the values in `[lower, upper]`
         * and ask of each one "present or not?". A run of consecutive "not present" values is one output range. The
         * order of `nums`, duplicates, and out-of-range values stop mattering once everything has been marked.
         *
         * ### Core concepts
         * - **Presence array / direct addressing:** `seen[v - offset]` as a set over a small integer domain. It gives
         *   O(1) membership without hashing, and duplicates are idempotent writes. It fits here because R <= 1e5.
         * - **Offset indexing:** storing `v - lower` shrinks the array to the window size R = upper - lower + 1 and
         *   makes "out of range" a single bounds check.
         * - **Run detection (open/close a run):** track `start` of the current run of `false`, and close it when a
         *   `true` appears or the domain ends. It is the core loop of every gaps-and-islands problem.
         * - **Output-sensitive lower bound:** you must read all n inputs and may need to emit up to ~R/2 ranges, so
         *   O(n + R) is optimal.
         *
         * ### Approach
         * 1. `seen = BooleanArray(upper - lower + 1)`.
         * 2. For each `num` in `lower..upper`, set `seen[num - lower] = true` (ignore the others).
         * 3. Sweep `i` over `0 until R`: when `!seen[i]` and no run is open, open one at `i`; when `seen[i]` and a run
         *    is open, emit `[lower + start, lower + i - 1]` and close it.
         * 4. After the loop, if a run is still open, emit `[lower + start, upper]`.
         *
         * ### Complexity
         * - **Time O(n + R):** one pass to mark and one pass over the window, each O(1) per step.
         * - **Space O(R)** for the bitmap (a `BooleanArray` of 1e5 is about 100 KB), plus the output.
         *
         * ### Common pitfalls
         * - **Forgetting the trailing run:** a gap that reaches `upper` is never closed inside the loop (see
         *   `[1,1], 5..7 -> [[5,7]]`).
         * - **Values outside the window:** `nums[i]` can be `< lower` or `> upper`, and indexing `seen[num - lower]`
         *   without a range check throws.
         * - **Duplicates:** harmless with marking, but they break naive "compare with previous" sweeps that do not
         *   sort and dedupe first.
         * - **Empty answer:** return `[]`, not `[[]]`.
         * - **Unbounded domains:** if the window could be 1..1e18, the bitmap is impossible. Fall back to sort +
         *   sweep (O(n log n)) or an ordered interval set.
         */
        fun referenceSolution(nums: IntArray, lower: Int, upper: Int): List<List<Int>> {
            val size = upper - lower + 1
            val seen = BooleanArray(size)
            for (num in nums) if (num in lower..upper) seen[num - lower] = true

            val result = mutableListOf<List<Int>>()
            var start = -1 // -1 = no open run
            for (i in 0 until size) {
                if (!seen[i]) {
                    if (start == -1) start = i
                } else if (start != -1) {
                    result += listOf(lower + start, lower + i - 1)
                    start = -1
                }
            }
            if (start != -1) result += listOf(lower + start, upper)
            return result
        }


    }
}
