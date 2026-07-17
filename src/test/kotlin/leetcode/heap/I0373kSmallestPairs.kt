package leetcode.heap

import leetcode.expectsAnyOf
import leetcode.expectsAnyOrder
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test

/**
 * 373. Find K Pairs with Smallest Sums  (https://leetcode.com/problems/find-k-pairs-with-smallest-sums/)
 *
 * You are given two integer arrays nums1 and nums2 sorted in non-decreasing order, and an integer k.
 * A pair (u, v) consists of one element u from nums1 and one element v from nums2.
 * Return the k pairs (u1, v1), (u2, v2), ..., (uk, vk) with the smallest sums (u + v).
 *
 * Constraints:
 * - 1 <= nums1.length, nums2.length <= 10^5
 * - -10^9 <= nums1[i], nums2[i] <= 10^9
 * - nums1 and nums2 are both sorted in non-decreasing order
 * - 1 <= k <= 10^4
 * - k may exceed the total number of pairs (nums1.length * nums2.length) — then return all pairs.
 *   (Note: two 10^9 values sum to 2*10^9 which overflows Int; consider Long when comparing sums.)
 */
typealias I0373 = (IntArray, IntArray, Int) -> List<List<Int>>

class I0373kSmallestPairs {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0373> {

        // The set of k smallest-sum pairs is uniquely determined, but LeetCode accepts them in any
        // order — so cases use `expectsAnyOrder` (multiset compare). If you emit them sorted by sum
        // you'll match too; the any-order compare just means you don't have to.
        override val cases = _root_ide_package_.leetcode.testCases<I0373>(
            _root_ide_package_.leetcode.args("[1,7,11]", "[2,4,6]", 3) expectsAnyOrder "[[1,2],[1,4],[1,6]]",
            _root_ide_package_.leetcode.args("[1,1,2]", "[1,2,3]", 2) expectsAnyOrder "[[1,1],[1,1]]",
            _root_ide_package_.leetcode.args("[1,2]", "[3]", 3) expectsAnyOrder "[[1,3],[2,3]]",
            _root_ide_package_.leetcode.args("[1,2]", "[1,2]", 3) expectsAnyOrder "[[1,1],[1,2],[2,1]]",
            _root_ide_package_.leetcode.args("[1,1]", "[1,2]", 3) expectsAnyOrder "[[1,1],[1,1],[1,2]]",
            _root_ide_package_.leetcode.args("[1,2,3]", "[1,2,3]", 5).expectsAnyOf(
                "[[1,1],[1,2],[2,1],[1,3],[2,2]]", "[[1,1],[1,2],[2,1],[1,3],[3,1]]"
            ),
        )

        @Test
        fun test() = check(::kSmallestPairs, ::referenceSolution)

        /**
         * ANALYSIS — your solution (min-heap / k-way merge). VERIFIED: all 6 cases pass.
         *
         * Pattern: best-first frontier expansion with a min-heap — the same "merge k sorted lists"
         * template. NOTE this is NOT a two-pointer walk (despite what the referenceSolution KDoc below
         * claims about "the kSmallestPairs two-pointer attempt above" — that comment is stale and
         * describes an earlier draft; your current code is a correct heap solution, structurally
         * identical to the reference). Each row i is a sorted stream nums1[i]+nums2[0] <= nums1[i]+nums2[1]
         * <= ...; seeding column 0 of the first min(m,k) rows and only ever advancing rightward reaches
         * every needed cell exactly once, so the frontier branches the way a single lattice path cannot.
         *
         * Correctness: sound.
         *  - Overflow handled — the comparator sums as `nums1[i].toLong() + nums2[j]`, so 2*10^9 never
         *    truncates. (This is THE classic pitfall on this problem; you got it right.)
         *  - k > m*n handled — the `heap.isNotEmpty()` guard drains the frontier and returns all pairs.
         *  - Empty input guarded up front; the `j < nums2.lastIndex` push guard is equivalent to the
         *    reference's `j + 1 < nums2.size` and is safe once the empty check has passed.
         *  - Seeds min(nums1.size, k), not all m rows — right call, since m can be 10^5 while k is 10^4.
         *  - Tie-break `{ (i) -> nums1[i] }` orders equal-sum pairs by the u value; harmless (LeetCode
         *    accepts any order, cases use expectsAnyOrder / expectsAnyOf).
         *
         * Time:  O(k · log(min(m, k))).
         *   - Seeding: min(m,k) inserts × O(log(min(m,k))).
         *   - Main loop: up to k iterations; each poll+push is O(log(heap size)). The heap size is
         *     invariant at <= min(m,k) — every pop removes one entry and pushes at most one, and the
         *     seed already caps it — so log(min(m,k)) per op. Total is dominated by the k pops.
         *   - Minor constant-factor note: the comparator recomputes `toLong() + …` on every comparison
         *     (O(log h) times per element) rather than caching the sum in the heap entry, and Pair<Int,Int>
         *     boxes both ints. Storing the precomputed sum (e.g. a Triple or an IntArray+parallel Long)
         *     trims constants but not the asymptotics.
         *
         * Space: O(min(m, k)) auxiliary (the heap) + O(k) output. Distinct from the output list, the
         *   working set is just the frontier.
         *
         * Alternatives & trade-offs:
         *  - Brute force: build all m·n sums, sort, take k → O(mn log(mn)) time / O(mn) space. Fine for
         *    tiny inputs, but m·n can be 10^10 here — infeasible. Your heap avoids materializing the grid.
         *  - Seed the SMALLER array: you seed rows (nums1). If nums1 is the long array and nums2 short,
         *    swapping so you seed along min(m,n) shrinks the heap to O(min(m, n, k)) and swaps the emitted
         *    pair order back. Same asymptotic class, smaller heap in the lopsided case — a clean micro-win.
         *  - Binary search on the answer: binary-search the k-th smallest sum value, counting pairs with
         *    sum <= mid via a per-row two-pointer, then collect. ~O((m+n)·log(valueRange) + k). Wins when k
         *    approaches m·n (heap degrades toward O(mn log)); more code and fiddlier to collect exactly k.
         *  For k << m·n (the stated constraints: k <= 10^4), your heap approach is the standard optimal
         *  choice — no asymptotically better general method for small k.
         *
         * Parallelism: essentially none to gain here. The heap is a strict data dependency — each pop
         *  determines what gets pushed next — so the frontier walk is inherently sequential, and at
         *  k <= 10^4 thread/coordination overhead would dominate anyway. The one variant that DOES
         *  parallelize is the binary-search alternative: its "count pairs with sum <= mid" step is an
         *  embarrassingly parallel per-row map-reduce (independent counts summed), a realistic SIMD/thread
         *  target only at much larger scale.
         *
         * Real-world: this is textbook k-way merge / top-k. It's exactly how external-sort merge phases
         *  and LSM-tree compaction (RocksDB, Cassandra, Lucene segment merges) combine many pre-sorted
         *  runs, and how a "top-k over sharded, individually-sorted sources" query is served. In
         *  production the streams are unbounded/on-disk rather than two in-memory arrays, so the win is
         *  that the heap only ever holds one frontier element per source — you never load a source fully.
         *  The best-first expansion is the same idea underneath Dijkstra/A* frontier search.
         */
        fun kSmallestPairs(nums1: IntArray, nums2: IntArray, k: Int): List<List<Int>> {
            val result = mutableListOf<List<Int>>()
            if (nums1.isEmpty() || nums2.isEmpty()) return result

            val heap = PriorityQueue<Pair<Int, Int>>(
                compareBy(
                    { (i, j) -> nums1[i].toLong() + nums2[j] },
                    { (i) -> nums1[i] }
                )
            )

            (0 until minOf(nums1.size, k)).forEach { i -> heap.add(i to 0) }

            while (result.size < k && heap.isNotEmpty()) {
                val (i, j) = heap.poll()
                result.add(listOf(nums1[i], nums2[j]))
                if (j < nums2.lastIndex) heap.add(i to j + 1)
            }

            return result
        }

        /**
         * REFERENCE SOLUTION — min-heap / k-way merge (best-first frontier).
         *
         * Intuition: model pairs as a matrix M[i][j] = nums1[i] + nums2[j], non-decreasing along
         * every row and column. The k smallest sums form a *branching* frontier, so we can't walk a
         * single lattice path (that is exactly why the `kSmallestPairs` two-pointer attempt above is
         * wrong — see the ANALYSIS block). Instead treat each row i as a sorted stream
         * (nums1[i]+nums2[0] <= nums1[i]+nums2[1] <= ...) and do a k-way merge with a min-heap keyed
         * by sum, the same pattern as "merge k sorted lists".
         *
         * Approach:
         *  1. Seed the heap with one candidate per row: (i, 0) for i in 0 until min(m, k). We never
         *     need more than k rows since we only pop k times.
         *  2. Pop the smallest-sum (i, j) k times, appending [nums1[i], nums2[j]] to the result.
         *  3. On each pop, push its right neighbour (i, j+1) if it exists. Column 0 of every row is
         *     already seeded, so advancing only rightward reaches every cell exactly once — the
         *     frontier expands in both directions the single-path walk could not.
         *
         * Complexity: Time O(k log(min(m, k))) — k pops/pushes on a heap capped at min(m, k) entries.
         *             Space O(min(m, k)) heap + O(k) output.
         *
         * Pitfalls:
         *  - Overflow: nums1[i] + nums2[j] can be 2*10^9 > Int.MAX, so the comparator sums as Long.
         *  - k may exceed m*n: the `heap.isNotEmpty()` guard stops early and returns all pairs.
         *  - Seed min(m, k), not all m rows (m can be 10^5 while k is 10^4).
         *
         */
        fun referenceSolution(nums1: IntArray, nums2: IntArray, k: Int): List<List<Int>> {
            val result = mutableListOf<List<Int>>()
            if (nums1.isEmpty() || nums2.isEmpty()) return result

            // Heap of (rowIndex, colIndex) ordered by nums1[i] + nums2[j] (as Long to avoid overflow),
            // with a secondary tie-break on the row index i. Any-order is accepted by LeetCode, but the
            // tie-break makes the output deterministic — sorted by (sum, then nums1 value) — which is
            // exactly what the order-sensitive `expectsAnyOf` case in this file expects.
            val heap = PriorityQueue<IntArray>(
                compareBy({ nums1[it[0]].toLong() + nums2[it[1]] }, { it[0] })
            )
            for (i in 0 until minOf(nums1.size, k)) heap.add(intArrayOf(i, 0))

            while (result.size < k && heap.isNotEmpty()) {
                val (i, j) = heap.poll()
                result.add(listOf(nums1[i], nums2[j]))
                if (j + 1 < nums2.size) heap.add(intArrayOf(i, j + 1))
            }

            return result
        }

    }
}
