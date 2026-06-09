package leetcode.graph_bfs

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0127 = (String, String, List<String>) -> Int

// https://leetcode.com/problems/word-ladder/
//
// ─── The problem as a graph ────────────────────────────────────────────────
// Treat every word as a NODE. Two nodes share an EDGE when the words differ in
// exactly one letter. "Shortest transformation sequence from beginWord to
// endWord" is then "shortest path from the begin node to the end node" in an
// unweighted graph. This is the same shape as Minimum Genetic Mutation (433);
// only the alphabet differs ({a..z} instead of {A,C,G,T}).
//
// ─── Counting WORDS, not edges (the one gotcha) ────────────────────────────
// LeetCode 127 asks for "the number of words in the shortest sequence", i.e.
// the number of NODES on the path, not the number of EDGES. A path of k edges
// visits k+1 words. So beginWord alone is length 1, each hop adds 1, and the
// answer is (edges + 1). That is why we seed the queue with `beginWord to 1`
// here, whereas 433 (which counts mutations = edges) seeds with `... to 0`.
//
// ─── Why BFS, not DFS ──────────────────────────────────────────────────────
// Every edge costs 1, so the fewest-edges path is the shortest path. BFS
// explores in concentric "rings": all words 1 hop away, then 2 hops, and so
// on. The first time BFS dequeues `endWord` it has used the minimum number of
// hops — any shorter route would have surfaced in an earlier ring. DFS finds
// *a* path but not necessarily the shortest, so it is the wrong tool.
//
// ─── The graph is "implicit" ───────────────────────────────────────────────
// We never build an adjacency list. Neighbours are GENERATED on demand: from a
// word, change each position to each of the 26 letters and keep the results
// that exist in the word list. With L = word length, generating the L*26
// candidates and discarding the begin/dead-end ones is cheaper to reason about
// than precomputing every pairwise edge.
//
// ─── The two pillars of correct BFS ────────────────────────────────────────
//  1. A QUEUE (FIFO) — processes nodes ring by ring, in non-decreasing
//     distance order. A stack (LIFO) would degrade this into DFS.
//  2. A VISITED set — the first time we reach a node is always via a shortest
//     path, so re-enqueuing it can never improve the answer; the guard also
//     prevents infinite loops on the cyclic word graph.
//
// ─── Two edge cases that decide the answer ─────────────────────────────────
//  • beginWord need NOT be in wordList — it is the start regardless.
//  • If endWord is not in wordList, no valid sequence can end on it -> return 0.
//    Checking this once up front lets the search bail early.
//
// ─── Complexity ────────────────────────────────────────────────────────────
// Let N = wordList size, L = word length, A = alphabet size (= 26).
// Each dequeued word generates L*A candidates, and building/hashing a candidate
// costs O(L), so the work is O(N * L * L * A). Space is O(N * L) for the word
// set, visited set, and queue.
class I0127wordLadder {

    @Nested
    inner class Solution : ProblemTest<I0127> {

        override val cases = testCases<I0127>(
            // hit -> hot -> dot -> dog -> cog : 5 words in the sequence
            args("hit", "cog", """["hot","dot","dog","lot","log","cog"]""") expects 5,
            // endWord "cog" is missing from the list -> no sequence can end on it
            args("hit", "cog", """["hot","dot","dog","lot","log"]""") expects 0,
            // direct hop: a -> c, both length-1 words, so the answer is 2
            args("a", "c", """["a","b","c"]""") expects 2,
            // beginWord need not be in the list; one hop hot -> dot reaches the end
            args("hot", "dot", """["dot"]""") expects 2,
            // unreachable: the only word differs in two positions from beginWord
            args("hit", "hut", """["hat"]""") expects 0,
        )

        @Test
        fun test() = check(::ladderLength, ::ladderLengthRef)

        fun ladderLength(beginWord: String, endWord: String, wordList: List<String>): Int {
            val alphabet = ('a'..'z').toList()
            val wordSet = wordList.toHashSet()

            val visited = mutableSetOf<String>()
            val queue = ArrayDeque<Pair<String, Int>>()
            queue.addLast(beginWord to 0)

            while (queue.isNotEmpty()) {
                val (word, level) = queue.removeFirst()
                if (word == endWord) return level + 1
                visited.add(word)

                for (i in word.indices) {
                    for (char in alphabet) {
                        val mutation = String(word.toCharArray().also { it[i] = char })
                        if (wordSet.contains(mutation) && visited.contains(mutation).not()) {
                            queue.addLast(mutation to level + 1)
                        }
                    }
                }

            }

            return 0
        }

        /**
         * Breadth-first search over the implicit "differ by one letter = one edge"
         * graph.
         *
         * We expand ring by ring; the depth at which we first dequeue [endWord] is
         * the number of words in the shortest sequence. Distances here count WORDS
         * (nodes), so [beginWord] starts at 1 and each hop adds 1. Returns 0 if
         * [endWord] is unreachable through words in [wordList].
         */
        fun ladderLengthRef(beginWord: String, endWord: String, wordList: List<String>): Int {
            // O(1) membership checks. A word is only a legal stepping stone if it
            // appears in the list; if endWord isn't there, nothing can end on it.
            val valid = wordList.toHashSet()
            if (endWord !in valid) return 0

            val alphabet = 'a'..'z'

            // visited doubles as our "seen" guard: a word enqueued here is never
            // enqueued again, so each word is expanded at most once.
            val visited = hashSetOf(beginWord)

            // Queue entries pair a word with its sequence length (word count) so far.
            val queue = ArrayDeque<Pair<String, Int>>()
            queue.add(beginWord to 1)

            while (queue.isNotEmpty()) {
                val (word, length) = queue.removeFirst()
                // Because BFS dequeues in non-decreasing distance order, this is the
                // shortest sequence that reaches endWord.
                if (word == endWord) return length

                // Generate every one-letter neighbour: for each position, swap in
                // each of the 26 letters. Keep only neighbours that are valid words
                // and haven't been visited yet.
                val chars = word.toCharArray()
                for (i in chars.indices) {
                    val original = chars[i]
                    for (letter in alphabet) {
                        if (letter == original) continue
                        chars[i] = letter
                        val next = String(chars)
                        if (next in valid && visited.add(next)) {
                            queue.add(next to length + 1)
                        }
                    }
                    chars[i] = original // restore before mutating the next position
                }
            }

            // Queue drained without ever reaching endWord -> no valid sequence.
            return 0
        }

    }
}
