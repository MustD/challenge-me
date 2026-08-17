package leetcode.trie

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 208. Implement Trie (Prefix Tree)  (https://leetcode.com/problems/implement-trie-prefix-tree/)
 *
 * A trie (pronounced "try") / prefix tree is a tree data structure used to efficiently store and
 * retrieve keys in a set of strings, with applications in autocomplete and spell-checking.
 * Implement the `Trie` class:
 *
 * - `Trie()`                     initializes the trie object.
 * - `insert(word: String)`       inserts `word` into the trie.
 * - `search(word: String)`       returns true iff `word` was previously inserted (a *complete*
 *                                word match, not merely a prefix).
 * - `startsWith(prefix: String)` returns true iff some previously inserted word has `prefix` as
 *                                a prefix.
 *
 * Worked example (each call operates on the same trie instance):
 *   insert("apple")
 *   search("apple")     -> true
 *   search("app")       -> false   // "app" was never inserted as a whole word
 *   startsWith("app")   -> true    // but "apple" starts with "app"
 *   insert("app")
 *   search("app")       -> true
 *
 * Constraints:
 * - 1 <= word.length, prefix.length <= 2000
 * - word and prefix consist only of lowercase English letters (a-z).
 * - At most 3 * 10^4 calls in total across insert, search, and startsWith.
 *
 * NOTE ON THE HARNESS: this is a *design* problem — a stateful class with several methods, not a
 * single pure function. The `ProblemTest` / `testCases` DSL models one function signature per
 * problem and can't drive a multi-method class, so (like the other design problems in this repo:
 * `I0155MinStack`, `I0146lruCache`, `I0211designAddAndSearchWordsDataStructure`) it is written as
 * a plain class with no test harness. Verify your implementation against the worked example above
 * by hand, or add your own `@Test` driver that replays a call sequence.
 *
 */
@Suppress("unused")
class I0208implementTrieAttempt2 {


    /**
     * ANALYSIS (verified — the `test apple` driver passes: search/startsWith behave per the spec).
     *
     * Pattern: **Trie / prefix tree, array-backed children.** Each node fans out over the fixed
     * 26-letter alphabet via `Array(26) { null }`, and a boolean `isWord` flag marks nodes that
     * terminate a *complete* inserted word (which is what lets `search` distinguish "app" the word
     * from "app" the mere prefix of "apple"). `findNode` factors out the shared descent used by both
     * `search` and `startsWith`; the only difference is what you check at the landing node —
     * `isWord` vs. mere existence. That factoring is the clean part of this attempt.
     *
     * Time — let L = length of the argument string:
     *   - insert(word)       O(L): one loop iteration per char; index is O(1) arithmetic
     *                         (`char - 'a'`), get-or-create child is O(1) array access. No hashing.
     *   - search(word)       O(L): `findNode` descent + O(1) flag read.
     *   - startsWith(prefix) O(L): `findNode` descent + O(1) null check.
     *   Independent of how many words are already stored — that is the whole point of a trie over a
     *   `Set<String>` scan.
     *
     * Space — total structure O(26 · K) where K = number of distinct trie nodes, and
     *   K <= 1 + (sum of lengths of all inserted words). Every node eagerly allocates 26 pointers
     *   even when it has one child, so a chain like "apple" costs 5 mostly-empty 26-slot arrays.
     *   That is the main cost of the array representation (see alternatives). Per-operation auxiliary
     *   space is O(1): all three methods are iterative — no recursion, so no call-stack growth even
     *   for the 2000-char max input (a recursive trie would use O(L) stack here).
     *
     * Correctness notes / edge cases:
     *   - Get-or-create idiom `node.children[idx] ?: run { … }` is correct: it inserts the new node
     *     into the array *and* returns it as the loop's next `node`. Clean.
     *   - `findNode` guards `word.isBlank()` → null. Given the constraints (length >= 1, only a-z)
     *     the empty/blank string never actually arrives, so this guard is dead code in practice. It
     *     is also slightly *inconsistent* with `insert`: `insert("")` would set `root.isWord = true`,
     *     yet `search("")` returns false because of the guard. Not reachable under the stated
     *     constraints, but if you ever relaxed them, `search` and `insert` would disagree about the
     *     empty word. The "trie-pure" behavior is to drop the guard and let root double as the
     *     empty-string terminal.
     *   - No overflow / off-by-one risk: `char - 'a'` is always 0..25 for valid input.
     *
     * Alternatives / trade-offs:
     *   - `HashMap<Char, Node>` children instead of `Array(26)`: saves memory when the alphabet is
     *     large or sparse (Unicode), but here (dense a-z) the array wins — no hashing, no boxing,
     *     better cache locality. Your choice is the right one for the constraints.
     *   - Plain `HashSet<String>` for words: gives O(L) `search` too (via hashing), but `startsWith`
     *     degrades — you'd either scan every stored word (O(total chars) per query) or pre-store all
     *     prefixes (O(sum of L²) space). The trie shares prefixes and answers both in O(L). This is
     *     why the trie is essentially optimal for the *prefix-query* half of the problem.
     *   - Radix / PATRICIA trie: compress single-child chains into one edge → far fewer nodes and
     *     less wasted memory, at the cost of edge-splitting logic. Same O(L) queries, better
     *     constants and footprint. This is what production systems actually use (below).
     *
     * Parallelism: not worth it here. Each operation is a strictly sequential root-to-leaf walk — a
     *   hard data dependency (you can't visit depth i+1 before resolving depth i), so a single call
     *   has no internal parallelism. Across *concurrent* calls the structure is shared mutable state:
     *   real concurrent tries need per-node locks or lock-free CAS on the children slots (and readers
     *   tolerating in-progress inserts). At LeetCode scale (<=3·10^4 calls) the synchronization
     *   overhead would dwarf any gain.
     *
     * Real-world: tries back autocomplete/typeahead, spellcheck/T9, and — as radix/PATRICIA tries —
     *   IP routing (longest-prefix match) and in-memory key-value indexes. Two things differ from the
     *   interview version: (1) memory dominates, so production uses compressed tries, double-array
     *   tries, or HAT-tries for cache efficiency rather than 26-pointer nodes; (2) input is often
     *   streaming/unbounded and concurrent, so persistence and thread-safety matter more than the raw
     *   O(L) bound, which is already as good as it gets.
     */
    class Trie {

        class Node(var isWord: Boolean = false) {
            val children: Array<Node?> = Array(26) { null }
        }

        val root = Node()

        fun insert(word: String) {
            val chars = word.toCharArray()
            var node = root
            for (char in chars) {
                val idx = char - 'a'
                val subNode = node.children[idx] ?: run {
                    val newNode = Node()
                    node.children[idx] = newNode
                    newNode
                }
                node = subNode
            }
            node.isWord = true
        }

        private fun findNode(word: String): Node? {
            if (word.isBlank()) return null

            val chars = word.toCharArray()
            var node = root
            for (char in chars) {
                val idx = char - 'a'
                val sub = node.children[idx] ?: return null
                node = sub
            }
            return node
        }

        fun search(word: String): Boolean {
            return findNode(word)?.isWord ?: false
        }

        fun startsWith(prefix: String): Boolean {
            return findNode(prefix) != null
        }

    }

    @Test
    fun `test apple`() {
        val trie = Trie()
        with(trie) {
            insert("apple")
            assertTrue { search("apple") }
            assertFalse { search("app") }
            assertTrue { startsWith("app") }
            insert("app")
            assertTrue { search("app") }
        }
    }
}
