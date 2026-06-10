package leetcode.trie

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0212 = (Array<CharArray>, Array<String>) -> List<String>

/**
 * LeetCode 212 — Word Search II.
 *
 * ## Problem
 * Given an `m x n` board of characters and a list of `words`, return every word that can
 * be spelled by walking sequentially adjacent cells (up/down/left/right). A single cell may
 * not be reused within one word.
 *
 * ## Why a Trie?
 * The naive approach runs Word Search I (LeetCode 79) once per word — for each word we DFS
 * the whole board. With `W` words that is `W` independent board scans, and the searches share
 * nothing: two words starting with "oa..." each re-walk that same prefix from scratch.
 *
 * A Trie flips the loop inside-out. We build one trie from all the words, then DFS the board
 * a single time. At each step we descend the trie in lock-step with the path on the board:
 * - If the current cell's letter is NOT a child of the current trie node, the prefix doesn't
 *   spell any word — we prune that entire branch immediately (this is the big win).
 * - If a trie node is marked as a word end, we've found a match.
 * One board walk now serves all words at once, and common prefixes are explored only once.
 *
 * ## Algorithm
 * 1. Insert every word into a trie. Storing the full `word` on the terminal node lets us emit
 *    it directly on a hit (no need to rebuild the string from the path).
 * 2. From each board cell, DFS while the trie has a matching child:
 *    - mark the cell visited (overwrite with a sentinel like '#') so it can't be reused,
 *    - if the trie node here ends a word, collect it and null it out to dedupe / prune,
 *    - recurse into the 4 neighbours,
 *    - restore the cell on the way back (backtracking).
 *
 * ## Pruning
 * After collecting a word we clear its `word` marker so duplicates aren't reported twice. As an
 * extra optimization, fully-explored leaf nodes can be pulled out of their parent's children,
 * letting the board DFS dead-end sooner — included below.
 *
 * ## Complexity
 * Let `m x n` be the board and `L` the max word length.
 * - Build: O(total characters across all words).
 * - Search: O(m · n · 4^L) in the worst case — from every cell we may branch in (up to) 3 new
 *   directions per step for up to L steps — but the trie prunes nearly all of that in practice.
 * - Space: O(total characters across all words) for the trie, plus O(L) recursion depth.
 */
class I0212wordSearch2 {

    @Nested
    inner class Solution : ProblemTest<I0212> {

        override val cases = testCases<I0212>(
            args(
                """[["o","a","a","n"],["e","t","a","e"],["i","h","k","r"],["i","f","l","v"]]""",
                """["oath","pea","eat","rain"]""",
            ) expects listOf("oath", "eat")
        )

        @Test
        fun test() = check(::findWords)

        fun findWords(board: Array<CharArray>, words: Array<String>): List<String> {
            class Node {
                val children: MutableMap<Char, Node> = mutableMapOf()

                /** Non-null only on a node that terminates a word; holds the word to emit on a hit. */
                var word: String? = null
            }

            // 1. Build the trie from all words.
            val root = Node()
            for (word in words) {
                var node = root
                for (char in word) {
                    node = node.children.getOrPut(char) { Node() }
                }
                node.word = word
            }

            val result = mutableListOf<String>()

            fun dfs(row: Int, col: Int, parent: Node) {
                val char = board[row][col]
                val node = parent.children[char] ?: return // prefix spells nothing — prune

                node.word?.let { // a word ends here
                    result.add(it)
                    node.word = null // dedupe: report each word at most once
                }

                board[row][col] = '#' // mark visited so this cell isn't reused
                val directions = arrayOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
                for ((dr, dc) in directions) {
                    val nr = row + dr
                    val nc = col + dc
                    if (nr in board.indices && nc in board[nr].indices && board[nr][nc] != '#') {
                        dfs(nr, nc, node)
                    }
                }
                board[row][col] = char // backtrack: restore the cell

                // Prune fully-explored leaves so future walks dead-end sooner.
                if (node.children.isEmpty()) parent.children.remove(char)
            }

            for (row in board.indices) {
                for (col in board[row].indices) {
                    dfs(row, col, root)
                }
            }

            return result
        }

    }
}
