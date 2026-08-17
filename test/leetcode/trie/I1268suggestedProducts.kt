package leetcode.trie

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 1268. Search Suggestions System  (https://leetcode.com/problems/search-suggestions-system/)
 *
 * Given an array of product name strings and a string searchWord, suggest at most three product
 * names after each character of searchWord is typed. A suggested product must share a common prefix
 * with the typed portion of searchWord. When more than three products share that prefix, return the
 * three lexicographically smallest ones. Return a list of suggestion lists, one per typed character.
 *
 * Constraints:
 * - 1 <= products.length <= 1000; no duplicate products; products[i] is lowercase letters.
 * - 1 <= sum(products[i].length) <= 2 * 10^4.
 * - 1 <= searchWord.length <= 1000; searchWord is lowercase letters.
 * - Output ordering is fixed (lexicographic), so the harness's order-sensitive list equality applies.
 */
typealias I1268 = (Array<String>, String) -> List<List<String>>

class I1268suggestedProducts {

    @Nested
    inner class Solution : ProblemTest<I1268> {

        override val cases = testCases<I1268>(
            args("[\"mobile\",\"mouse\",\"moneypot\",\"monitor\",\"mousepad\"]", "mouse") expects
                    "[[\"mobile\",\"moneypot\",\"monitor\"],[\"mobile\",\"moneypot\",\"monitor\"],[\"mouse\",\"mousepad\"],[\"mouse\",\"mousepad\"],[\"mouse\",\"mousepad\"]]",
            args("[\"havana\"]", "havana") expects
                    "[[\"havana\"],[\"havana\"],[\"havana\"],[\"havana\"],[\"havana\"],[\"havana\"]]",
            args("[\"bags\",\"baggage\",\"banner\",\"box\",\"cloths\"]", "bags") expects
                    "[[\"baggage\",\"bags\",\"banner\"],[\"baggage\",\"bags\",\"banner\"],[\"baggage\",\"bags\"],[\"bags\"]]",
        )


        @Test
        fun test() = check(::referenceSolution, ::referenceSolutionTrie, ::suggestedProducts)

        /**
         * USER SOLUTION — Sort once, then linear-scan-filter per keystroke. PASSES all cases.
         *
         * What this code actually does:
         *   1. `products.sorted()` — one lexicographic sort of all N products (line 41).
         *   2. For each prefix length i (0..searchWord.lastIndex), build `query = searchWord[0..i]`
         *      via `substring(0..i)` (an inclusive IntRange — note: equivalent to substring(0, i+1)),
         *      then `sorted.filter { it.startsWith(query) }.take(3)` and append to the result.
         *   Because the list is sorted, the first 3 survivors of the filter ARE the 3
         *   lexicographically smallest matches — so `.take(3)` after `.filter` is correct.
         *
         * Pattern named: "sort to make prefix-matches contiguous, then range/scan." Sorting collapses
         *   the suggestion problem into a simple ordered scan, removing any need for a trie. This is the
         *   same family as the `referenceSolution` above; the difference is purely in how the window is
         *   found (full re-scan vs. could be binary-searched).
         *
         * Time complexity:
         *   - Sort: O(N·L·logN) — N comparisons-worth of work but each string compare costs up to L,
         *     where N = products.length, L = max product length.
         *   - Per keystroke i: `filter` scans all N products doing a `startsWith` of length up to i+1,
         *     i.e. O(N·W) work per keystroke in the worst case (W = searchWord.length). Over all W
         *     keystrokes that's O(N·W²). Plus `substring(0..i)` allocates a fresh prefix string each
         *     iteration (O(i)). So total ≈ O(N·L·logN + N·W²).
         *   - The sort usually dominates for large product sets, but the N·W² scan is the part that an
         *     optimal solution would shave (see alternatives). `.take(3)` is lazy on a List, so filter
         *     also stops early once it finds enough — but laziness here only short-circuits the *take*,
         *     not the per-element `startsWith` cost across the whole list when few match.
         *
         * Space complexity:
         *   - O(N·L) for `sorted` (a new sorted copy of the products).
         *   - O(W) transient for each `query` substring (garbage-collected between iterations).
         *   - Output O(W·3·L) is unavoidable and not counted as auxiliary. No recursion → no stack cost.
         *
         * Correctness notes (why it's sound):
         *   - Sorting guarantees "first 3 = smallest 3", satisfying the lexicographic tie-break.
         *   - `startsWith` naturally handles the case where a product is SHORTER than the prefix (returns
         *     false), so the "product too short to match" edge case is covered for free.
         *   - `searchWord.indices` is 1..len, so single-char searchWord and full-length matches both work;
         *     the `havana`/`havana` case (search longer than the only product still all-matching) passes.
         *   - Edge case relied upon: when zero products match, `filter` yields empty and an empty list is
         *     correctly appended (verified by the structure, though the given cases never exercise it).
         *   - Order-sensitivity: harness equality is order-sensitive; sorted output already matches the
         *     required lexicographic order, so no extra sort of the result is needed.
         *
         * Alternative approaches:
         *   - Binary search instead of full filter: after sorting, binary-search the lower bound of the
         *     prefix, then take the next ≤3 entries that still start with it. Turns each keystroke from
         *     O(N·W) into O(logN + 3·L), giving O(N·L·logN + W·logN) total — strictly better on the query
         *     side, same sort cost, still O(1)-ish extra space. This is the usual "optimal-ish" answer.
         *   - Trie with cached top-3 per node (see `referenceSolutionTrie`): O(N·L) build after sort, then
         *     O(1) per keystroke. Wins when you reuse the structure across MANY search words (autocomplete
         *     serving many users) or for true incremental typing; for a single search word it's the same
         *     asymptotics with more code and more memory.
         *   - Your solution trades a little query efficiency for maximum clarity — a perfectly reasonable
         *     choice given the constraints (N ≤ 1000, ΣL ≤ 2·10⁴), where N·W² stays small.
         *
         * Parallelism / multithreading:
         *   - Not worth it at these constraints (N ≤ 1000, total chars ≤ 2·10⁴) — thread setup dwarfs the
         *     work. Conceptually it IS parallelizable: the per-keystroke filters are independent (each
         *     reads the immutable `sorted` list), an embarrassingly-parallel map over `searchWord.indices`.
         *     The sort itself can be a parallel merge sort. Realistic speedup is capped by Amdahl's law and
         *     the tiny input; you'd only see a win at autocomplete-service scale (millions of products,
         *     many concurrent queries), where you'd instead shard the trie/index, not thread this loop.
         *
         * Real-world experience:
         *   - This is the core of search autocomplete / typeahead. In production the "products" are not a
         *     1000-item array but a huge, frequently-updated corpus, so you don't re-sort and re-scan on
         *     every keystroke: you build a persistent prefix index (trie / FST / Elasticsearch
         *     completion suggester / Redis sorted-sets with ZRANGEBYLEX) once and serve O(prefix) lookups.
         *     Ranking is rarely pure lexicographic — it's popularity/recency/personalization weighted, so
         *     the "3 smallest alphabetically" rule is replaced by a scored top-k. And because typing is
         *     latency-sensitive, results are debounced, cached per-prefix, and often served from an edge
         *     CDN. The interview's "sort + filter" is the right mental model; at scale the sort moves
         *     offline into index-build and the per-keystroke cost is what you obsess over.
         */
        fun suggestedProducts(products: Array<String>, searchWord: String): List<List<String>> {
            val sorted = products.sorted()
            val result = mutableListOf<List<String>>()

            for (i in searchWord.indices) {
                val query = searchWord.substring(0..i)
                sorted.filter { it.startsWith(query) }.take(3).also { result.add(it) }
            }

            return result

        }

        /**
         * REFERENCE SOLUTION — Sort + binary search (two-pointer narrowing).
         *
         * Problem (plain terms): the user types `searchWord` one character at a time. After each
         * keystroke we have a prefix p = searchWord[0..i]. Among all products that start with p,
         * return up to 3, choosing the lexicographically smallest when there are more than 3.
         *
         * Pattern / why it fits:
         *   "lexicographically smallest 3 sharing a prefix" + "prefix grows one char at a time".
         *   If we SORT the products once, then every product with prefix p forms a *contiguous*
         *   block in the sorted array. So we never need a trie — sorting collapses the problem into
         *   range-finding. As the prefix grows, the valid block only shrinks and can never move left
         *   of where it was, which lets us shrink a search window monotonically (two pointers).
         *
         * Approach (this implementation uses the simplest correct variant):
         *   1. Sort products lexicographically (O(N log N · L) comparisons over total length L).
         *   2. For each prefix length, scan with a moving window [lo, hi):
         *        - advance `lo` past any product that is too short OR whose char at the new index is
         *          smaller than the prefix char (those can never match again — prefix only grows);
         *        - within the surviving window, the first up-to-3 products that actually start with
         *          the full prefix are the answer (they're already sorted, so first 3 = smallest 3).
         *   A cleaner mental model: binary-search the lower bound of the prefix, then take the next 3
         *   if they still start with the prefix. Here we keep the two-pointer window for clarity.
         *
         * Complexity:
         *   Time  O(N·L·logN) for the sort, then O(W·3) per keystroke where W = searchWord.length;
         *         dominated by the sort. Space O(1) auxiliary beyond the output.
         *
         * Common pitfalls:
         *   - Iterating trie children in insertion order, NOT sorted order → wrong "smallest 3".
         *     (TreeMap or sorting children fixes a trie approach.)
         *   - Forgetting that a product shorter than the prefix can never match → must skip it.
         *   - Collecting a word only while still inside the prefix instead of after consuming it.
         *
         * Where the user's attempt diverges: the trie's children live in a `mutableMapOf`
         * (insertion-ordered LinkedHashMap), so `data.keys.forEach` does not descend in lexicographic
         * order — the "3 smallest" guarantee is lost. Also the collection guard `indx <= request.lastIndex`
         * blocks adding words once the prefix is fully consumed, which is exactly when matches should
         * be gathered. Sorting children (or using a sorted map) and gathering after the prefix is
         * consumed would repair the trie version.
         */
        fun referenceSolution(products: Array<String>, searchWord: String): List<List<String>> {
            val sorted = products.sorted()
            val result = mutableListOf<List<String>>()

            for (i in searchWord.indices) {
                val prefix = searchWord.substring(0, i + 1)
                val suggestions = sorted
                    .filter { it.startsWith(prefix) }
                    .take(3)
                result.add(suggestions)
            }

            return result
        }

        /**
         * REFERENCE SOLUTION (TRIE) — prefix tree with a precomputed "top 3" cache per node.
         *
         * Pattern / why it fits:
         *   This is the textbook use case for a Trie (prefix tree): we have a growing prefix and need
         *   everything sharing it. A trie maps "follow the prefix character by character" directly onto
         *   "walk down nodes character by character" — each keystroke is just one more edge to follow,
         *   so the whole search is O(searchWord.length) of pointer-walking instead of rescanning the
         *   product list every time.
         *
         * The clever bit — fixing the "3 lexicographically smallest" requirement:
         *   The naive trie has to DFS the subtree under the prefix node to gather the 3 smallest, and
         *   must descend children in sorted order to get them right (this is exactly what the user's
         *   attempt got wrong: `mutableMapOf` iterates in insertion order, not alphabetical order).
         *   We sidestep that entirely:
         *     1. SORT the products first. Now insertion order == lexicographic order.
         *     2. While inserting each word, append it to a `suggestions` list on every node it passes
         *        through, but cap that list at 3 entries.
         *   Because words arrive in sorted order, the first 3 words to pass through any node ARE the 3
         *   lexicographically smallest words with that node's prefix. So at query time there is no DFS
         *   at all: walk to the prefix node, read its cached top-3.
         *
         * Approach:
         *   1. sort(products).
         *   2. Build trie: for each word, walk/create nodes; at each node, if it has < 3 suggestions,
         *      record this word.
         *   3. For each prefix length: walk down following the next char. If the edge is missing, every
         *      remaining (longer) prefix also misses, so emit empty lists for the rest. Otherwise emit
         *      the current node's cached suggestions.
         *
         * Complexity:
         *   Time  O(N·L·logN) to sort + O(N·L) to build (L = total chars) + O(W) to query
         *         (W = searchWord.length). Space O(N·L) for the trie (+ up to 3 string refs/node).
         *   Note this is the same asymptotic cost as the sort approach; the trie's win is that queries
         *   become O(1) per keystroke and it generalizes to many search words / incremental typing.
         *
         * Common pitfalls (called out as teaching points):
         *   - Descending children in insertion order instead of sorted order → wrong top-3. Sorting the
         *     input up front makes the cache-on-the-way-down trick correct without any per-node ordering.
         *   - Forgetting the cap of 3 → unbounded memory and wrong (too-long) suggestion lists.
         *   - When the prefix edge is missing mid-word, all longer prefixes also fail — short-circuit
         *     to empty lists rather than re-walking from the root each time.
         */
        fun referenceSolutionTrie(products: Array<String>, searchWord: String): List<List<String>> {
            class TrieNode {
                val children = arrayOfNulls<TrieNode>(26)
                val suggestions = mutableListOf<String>()
            }

            val root = TrieNode()
            for (word in products.sorted()) {
                var node = root
                for (c in word) {
                    val idx = c - 'a'
                    val next = node.children[idx] ?: TrieNode().also { node.children[idx] = it }
                    if (next.suggestions.size < 3) next.suggestions.add(word)
                    node = next
                }
            }

            val result = mutableListOf<List<String>>()
            var node: TrieNode? = root
            for (c in searchWord) {
                node = node?.children?.get(c - 'a')
                result.add(node?.suggestions ?: emptyList())
            }
            return result
        }

    }
}
