package leetcode.graph_bfs

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0433 = (String, String, Array<String>) -> Int

// https://leetcode.com/problems/minimum-genetic-mutation/
//
// ─── The problem as a graph ────────────────────────────────────────────────
// Think of every valid gene string as a NODE. Two nodes are connected by an
// EDGE when they differ in exactly one character (one single mutation). The
// question "minimum number of mutations from start to end" is then literally
// "shortest path (fewest edges) from the start node to the end node" in an
// unweighted graph.
//
// ─── Why BFS, not DFS ──────────────────────────────────────────────────────
// In an UNWEIGHTED graph every edge has cost 1, so the shortest path is the
// one with the fewest edges. BFS explores the graph in concentric "rings":
// first all nodes 1 mutation away, then all nodes 2 mutations away, and so on.
// The first time BFS reaches `endGene`, it is guaranteed to have used the
// minimum number of mutations — because any shorter path would have been found
// in an earlier ring. DFS gives *a* path but not necessarily the shortest, so
// it's the wrong tool here.
//
// ─── The graph is "implicit" ───────────────────────────────────────────────
// We never build an adjacency list up front. Instead we GENERATE neighbours on
// demand: from a gene, try changing each of the 8 positions to each of the 4
// letters {A, C, G, T}, and keep only the results that exist in the bank. This
// is the classic "word ladder" pattern (LeetCode 127 is the same shape).
//
// ─── The two pillars of correct BFS ────────────────────────────────────────
//  1. A QUEUE (FIFO) — guarantees nodes are processed ring by ring, in
//     non-decreasing distance order. A stack (LIFO) would turn this into DFS.
//  2. A VISITED set — prevents re-enqueuing a node we've already reached. The
//     first time we see a node is always via a shortest path, so revisiting it
//     can never improve the answer; skipping it also stops infinite loops.
//
// ─── Complexity ────────────────────────────────────────────────────────────
// Let B = bank size, L = gene length (= 8), A = alphabet size (= 4).
// Generating neighbours costs O(L * A) per node, and a string comparison /
// hash costs O(L). With a HashSet bank, total work is O(B * L * A) ≈ O(B),
// since L and A are tiny constants. Space is O(B) for the bank set, the
// visited set, and the queue.
class I0433minimumGeneticMutation {

    @Nested
    inner class Solution : ProblemTest<I0433> {

        override val cases = testCases<I0433>(
            // start == end, no mutation needed
            args("AACCGGTT", "AACCGGTT", """[]""") expects 0,
            // one mutation, target is the only bank entry
            args("AACCGGTT", "AACCGGTA", """["AACCGGTA"]""") expects 1,
            // two mutations; the intermediate gene must also be in the bank
            args("AACCGGTT", "AAACGGTA", """["AACCGGTA", "AACCGCTA", "AAACGGTA"]""") expects 2,
            // endGene is not in the bank -> unreachable
            args("AACCGGTT", "AACCGGTA", """["AACCGCTA"]""") expects -1,
            // empty bank -> unreachable (unless start already equals end, handled above)
            args("AACCGGTT", "AACCGGTA", """[]""") expects -1,
        )

        @Test
        fun test() = check(::minMutation, ::minMutationRef)

        fun minMutation(startGene: String, endGene: String, bank: Array<String>): Int {
            if (bank.isEmpty()) return if (startGene == endGene) 0 else -1

            fun String.isMutableTo(other: String): Boolean {
                var diff = 0
                forEachIndexed { index, ch ->
                    if (other[index] != ch) diff++
                    if (diff > 1) return false
                }
                return diff == 1
            }

            val graph = (bank + startGene).associateWith { gene -> bank.filter { gene.isMutableTo(it) } }

            val visited = mutableSetOf(startGene)
            val queue = ArrayDeque<Pair<String, Int>>()
            queue.add(startGene to 0)
            while (queue.isNotEmpty()) {
                val (current, mutations) = queue.removeFirst()
                if (current == endGene) return mutations
                for (next in graph[current] ?: emptyList()) {
                    if (visited.add(next)) queue.add(next to mutations + 1)
                }
            }

            return -1
        }

        /**
         * Breadth-first search over the implicit "one mutation = one edge" graph.
         *
         * We expand the search ring by ring; the depth at which we first dequeue
         * [endGene] is the minimum number of mutations. Returns -1 if [endGene]
         * is unreachable through valid (in-bank) genes.
         */
        fun minMutationRef(startGene: String, endGene: String, bank: Array<String>): Int {
            // O(1) membership checks: a gene is only a legal stepping stone if
            // it's recorded in the bank.
            val valid = bank.toHashSet()
            val alphabet = charArrayOf('A', 'C', 'G', 'T')

            // visited doubles as our "seen" guard: a node enqueued here is never
            // enqueued again, so each gene is expanded at most once.
            val visited = hashSetOf(startGene)

            // Queue entries pair a gene with its distance (mutation count) from start.
            val queue = ArrayDeque<Pair<String, Int>>()
            queue.add(startGene to 0)

            while (queue.isNotEmpty()) {
                val (gene, mutations) = queue.removeFirst()
                // Found it. Because BFS dequeues in non-decreasing distance order,
                // this is the fewest mutations possible.
                if (gene == endGene) return mutations

                // Generate every one-mutation neighbour: for each position, swap in
                // each of the four bases. Keep only neighbours that exist in the bank
                // and haven't been visited yet.
                val chars = gene.toCharArray()
                for (i in chars.indices) {
                    val original = chars[i]
                    for (base in alphabet) {
                        if (base == original) continue
                        chars[i] = base
                        val next = String(chars)
                        if (next in valid && visited.add(next)) {
                            queue.add(next to mutations + 1)
                        }
                    }
                    chars[i] = original // restore before mutating the next position
                }
            }

            // Queue drained without ever reaching endGene -> no valid mutation path.
            return -1
        }
    }
}
