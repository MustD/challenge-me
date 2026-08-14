package leetcode.graph_bfs

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 127. Word Ladder  (https://leetcode.com/problems/word-ladder/)
 *
 * A transformation sequence from `beginWord` to `endWord` using dictionary `wordList` is a sequence
 * of words `beginWord -> s1 -> s2 -> ... -> sk` such that every adjacent pair of words differs by a
 * single letter, and every `si` (for `1 <= i <= k`) is in `wordList`. Note that `beginWord` itself
 * does **not** need to be in `wordList`, but `sk == endWord` does. Given `beginWord`, `endWord` and
 * `wordList`, return the **number of words** in the shortest such transformation sequence, or `0`
 * if no sequence exists.
 *
 * Constraints:
 * - 1 <= beginWord.length <= 10
 * - endWord.length == beginWord.length
 * - 1 <= wordList.length <= 5000
 * - wordList[i].length == beginWord.length
 * - beginWord, endWord and wordList[i] consist of lowercase English letters
 * - beginWord != endWord
 * - All the words in wordList are unique
 */
typealias I0127 = (String, String, List<String>) -> Int

class I0127ladderLength {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0127> {

        override val cases = leetcode.testCases<I0127>(
            // Example 1: "hit" -> "hot" -> "dot" -> "dog" -> "cog" is 5 words long.
            leetcode.args("hit", "cog", """["hot","dot","dog","lot","log","cog"]""") expects 5,
            // Example 2: endWord "cog" is not in wordList, so there is no valid sequence.
            leetcode.args("hit", "cog", """["hot","dot","dog","lot","log"]""") expects 0,
            // Edge: single transformation - beginWord differs from endWord by one letter.
            leetcode.args("a", "c", """["a","b","c"]""") expects 2,
            // Edge: endWord present but unreachable (no single-letter bridge exists).
            leetcode.args("hot", "dog", """["hot","dog"]""") expects 0,
            // Long single chain (9 hops = 10 words): exercises deeper levels and, for the
            // bidirectional solution, a meeting point in the middle rather than at the frontier.
            leetcode.args(
                "aaa", "ddd",
                """["aab","abb","bbb","bbc","bcc","ccc","ccd","cdd","ddd"]""",
            ) expects 10,
        )

        @Test
        fun test() = check(::ladderLength, ::referenceSolution, ::referenceSolution2)

        /**
         * ## Pattern: BFS over an *implicit* graph (state-space search)
         *
         * Words are nodes; an edge exists between words differing in exactly one letter. All edges
         * cost 1, so "shortest transformation sequence" == "fewest edges" == BFS. The adjacency list
         * is never materialised — `nextTransitions` *generates* neighbours on demand by rewriting
         * each position with each of the 26 letters and keeping the ones that exist in the
         * dictionary. Same shape as 433 (Minimum Genetic Mutation), only the alphabet differs.
         *
         * Counting gotcha handled correctly here: LeetCode asks for the number of **words** (nodes),
         * not hops (edges). The queue is seeded with `beginWord to 0` and returns `lvl + 1`, which is
         * the equivalent of seeding with `1` and returning `lvl`.
         *
         * ## Time
         *
         * Let `N` = dictionary size, `L` = word length, `A` = 26.
         *
         * - Per *expansion* of one word, `nextTransitions` does `L * A` iterations, and each
         *   iteration is `O(L)`: `w.toCharArray()` copies, `joinToString("")` rebuilds, and both set
         *   lookups hash the whole string. So one expansion costs `O(L² * A)`.
         * - An *ideal* BFS expands each word at most once, giving `O(N * L² * A)` — with `N = 5000`,
         *   `L = 10` that is ~13M cheap operations, comfortably fast.
         * - **But this implementation can expand a word many times.** `visited.add(node)` happens on
         *   *dequeue*, while the filter `option !in visited` is checked at *enqueue*. Because a node
         *   only enters `visited` once it is popped — after the entire previous level has been
         *   drained — every queue entry on level `d` that is adjacent to node `v` on level `d + 1`
         *   enqueues its own copy of `v`. Unrolling that recurrence:
         *   `entries(v) == number of distinct shortest paths from beginWord to v`.
         *
         *   Concrete blow-up inside the stated constraints: `beginWord = "aaaaaaaaaa"`,
         *   `endWord = "bbbbbbbbbb"`, `wordList` = all 1024 words over `{a,b}` of length 10
         *   (`N = 1024 <= 5000`, `L = 10`). Every one of the `10! = 3.6M` orderings of "which letter
         *   to flip next" is a distinct shortest path, so the queue peaks at ~3.6M entries and total
         *   expansions are `Σ(d=0..9) 10!/(10-d)! ≈ 6.2M` instead of 1024 — roughly 6000x the
         *   necessary work, plus the memory to hold the queue. Simulating this algorithm verbatim on
         *   the same `{a,b}` family confirms the growth is factorial, not linear:
         *
         *   | `L` | words | expansions (this code) | expansions (fixed) | peak queue |
         *   |-----|-------|------------------------|--------------------|------------|
         *   | 4   | 16    | 41                     | 15                 | 24         |
         *   | 5   | 32    | 206                    | 31                 | 120        |
         *   | 6   | 64    | 1 237                  | 63                 | 720        |
         *   | 7   | 128   | 8 660                  | 127                | 5 040      |
         *
         *   The current LeetCode test set does not contain such a case, so this passes there; it is
         *   still the one real defect.
         *
         *   One-line fix, no restructuring: mark on **enqueue**, not dequeue —
         *   `visited.add(beginWord)` before the loop, then `if (visited.add(option)) queue.add(...)`
         *   ([MutableSet.add] returns `false` when already present, so it tests and marks in a single
         *   hash probe). That restores the textbook `O(N * L² * A)` and makes the queue `O(N)`.
         *
         * ## Space
         *
         * - `wordListIndex`: `O(N * L)` — unavoidable, it is the dictionary.
         * - `visited`: `O(N * L)` worst case (references, not copies, since keys are existing words).
         * - `queue`: **`O(shortest-path count)`** as written, vs. `O(N)` after the fix above. This is
         *   the dominant term in the pathological case, and the reason the bug hurts memory as much
         *   as time.
         * - `nextTransitions` result list: `O(L * A)` transient per call, plus `L * A` freshly
         *   allocated `L`-char arrays and strings — GC churn, see "polish" below.
         * - No recursion, so no stack depth to account for; output is a single `Int`.
         *
         * ## Correctness notes
         *
         * - Sound because BFS on an unweighted graph dequeues nodes in non-decreasing distance
         *   order, so the *first* dequeue of `endWord` is via a shortest path. The duplicate-enqueue
         *   flaw above costs performance but **not** correctness: duplicates all sit at the same
         *   level, so the minimum is still found first.
         * - `endWord !in wordListIndex -> 0` is the essential early exit: `endWord` must be in the
         *   dictionary, `beginWord` need not be.
         * - Subtle ordering dependency worth naming: `visited.add(node)` executes *before*
         *   `nextTransitions(node)`, which is the only thing stopping the `l == w[i]` candidate
         *   (the word itself) from being re-enqueued. Swap those two lines and it self-loops.
         * - `if (beginWord == endWord) return 0` is dead code — the constraints guarantee
         *   `beginWord != endWord`. Harmless, but note it would also be the *wrong* answer if it
         *   were reachable (a single-word sequence has length 1, not 0).
         * - Handles the "no path" case by falling out of the drained queue and returning 0.
         *
         * ## Polish (micro, not asymptotic)
         *
         * - `(option in wordListIndex) and (option !in visited)`: `and` is the non-short-circuiting
         *   [Boolean] function; `&&` skips the second hash lookup when the first fails. Cheap habit.
         * - `w.toCharArray()` is inside the *letter* loop, so it allocates `L * A` arrays per word
         *   instead of `L`. Hoist it out of the inner loop and restore `candidate[i] = w[i]` after
         *   trying the 26 letters.
         * - `String(candidate)` is cheaper than `candidate.joinToString("")` (which routes through a
         *   [StringBuilder] with a separator check per element).
         *
         * ## Alternative approaches
         *
         * 1. **Wildcard-bucket index** (the standard optimisation): pre-map every pattern
         *    `"h*t" -> ["hot", "hit"]` in `O(N * L²)`, then a word's neighbours are `L` map lookups
         *    instead of `L * A` generate-and-test. Total `O(N * L²)` — drops the factor of 26 —
         *    at the cost of `O(N * L²)` extra memory for the index.
         * 2. **Bidirectional BFS**: expand from both ends, always advancing the *smaller* frontier,
         *    and stop when the frontiers intersect. Branching factor `b` over depth `d` becomes
         *    `O(b^(d/2))` instead of `O(b^d)`; on this problem it is typically the single biggest
         *    practical win. Trickier bookkeeping: the answer is `levelFromStart + levelFromEnd + 1`,
         *    and the intersection test must run against the *other* side's frontier.
         * 3. **Explicit adjacency list via pairwise comparison**: `O(N² * L)` to build
         *    (2.5e8 char comparisons at `N = 5000`) — strictly worse than generating neighbours, and
         *    the reason the implicit graph is the right call here.
         * 4. **A\*** with `h = hammingDistance(word, endWord)`: admissible, since one move can fix at
         *    most one letter, so it never overestimates. Legal and it does prune, but a priority
         *    queue's constant factors rarely beat bidirectional BFS at these input sizes.
         * 5. **DFS / backtracking**: wrong tool. It finds *a* path, not the shortest, unless it
         *    enumerates all of them — which is exactly the exponential path count discussed above.
         *
         * Verdict: the chosen technique is the right one and is asymptotically optimal up to the
         * constant `A` (you must at least read every word, `Ω(N * L)`). Fix the visited-marking and
         * it is a textbook-quality solution; add wildcard buckets or bidirectional search only if
         * you want the constant factors.
         *
         * ## Parallelism / multithreading
         *
         * Level-synchronous BFS is *the* classic parallel graph algorithm: within one frontier all
         * expansions are independent, so you could shard the frontier across threads with a
         * concurrent visited set (`ConcurrentHashMap.newKeySet()`, whose `add` is still an
         * atomic test-and-mark) and per-thread output buffers merged at a barrier between levels.
         * Honest verdict for *this* problem: **not worth it.** Total work is ~13M trivial operations
         * (single-digit milliseconds); thread pool dispatch plus one barrier per level costs more
         * than it saves, and by Amdahl's law those `d` sequential barriers cap the speedup while
         * frontiers are only hundreds of nodes wide. The parallel version starts winning at
         * Graph500 scale (billions of edges), where the refinement is *direction-optimising* BFS —
         * switching between top-down (scan the frontier) and bottom-up (scan unvisited nodes for a
         * visited parent) once the frontier gets large. SIMD is a poor fit for the generate-and-test
         * formulation, but if you had built adjacency by pairwise comparison it would vectorise
         * beautifully: pack `L <= 16` chars into one register, XOR against the target, and
         * `popcount` the non-zero lanes to get the Hamming distance in a couple of instructions.
         * Bidirectional BFS *looks* like two parallel tasks, but its speedup comes from the
         * sequential policy of always growing the smaller side, not from running both at once.
         *
         * ## Real-world experience
         *
         * - **Unweighted shortest path is everywhere**: degrees of separation in social graphs
         *   (LinkedIn's "3rd degree", Facebook's friend paths — bidirectional BFS over sharded
         *   adjacency with hard depth caps), hop counts in network/mesh routing, dependency graph
         *   depth in package managers and Terraform/Kubernetes, grid pathfinding in games (which
         *   graduates to A-star or Dijkstra the moment edges gain weights).
         * - **This exact neighbour trick ships in production**: the wildcard-bucket index is what
         *   SymSpell and Levenshtein-automaton spell checkers precompute; fuzzy name matching and
         *   "did you mean" suggestions are word-ladder graphs with a distance budget instead of a
         *   target word.
         * - **Implicit graphs are the model-checker / puzzle-solver pattern** (Rubik's cube, sliding
         *   puzzles, SPIN/TLA+ state exploration). There the *visited set* is the binding constraint,
         *   not time, which is why real systems reach for Bloom filters or hash compaction —
         *   knowingly trading a small false-positive rate (and thus possibly missing states) for
         *   memory — or spill the frontier to disk and do partitioned BFS.
         * - **How production diverges from the interview version**: the graph is unbounded and
         *   mutating under you, and neighbours live behind a service or database, so you *batch*
         *   neighbour lookups per level instead of one word at a time (the N+1 query problem in
         *   disguise). Visited sets that outgrow memory become distributed BFS — Pregel / Giraph /
         *   Spark GraphX supersteps are literally level-synchronous BFS. Latency budgets make
         *   "no path within 4 hops" an acceptable answer, so depth caps are standard. And the string
         *   churn in `nextTransitions` is the kind of thing that shows up as GC pressure in a
         *   profile: real code interns words to dense `Int` ids up front, BFSes over ints, and lets
         *   `visited` become a bitset.
         * - **Why "optimal" sometimes loses**: for small `L` and `N`, plain generate-and-test beats a
         *   `HashMap` of pattern buckets on cache locality and allocation count. The index only pays
         *   off when many queries hit the same fixed dictionary so the build amortises — which is
         *   precisely the real-world shape, and precisely *not* the interview shape of one query and
         *   exit.
         */
        fun ladderLength(beginWord: String, endWord: String, wordList: List<String>): Int {

            val wordListIndex = wordList.toSet()
            if (endWord !in wordListIndex) return 0
            if (beginWord == endWord) return 0

            val letters = ('a'..'z')
            val visited = mutableSetOf<String>()

            fun nextTransitions(w: String): List<String> {
                val result = mutableListOf<String>()
                for (i in w.indices) {
                    for (l in letters) {
                        val candidate = w.toCharArray()
                        candidate[i] = l
                        val option = candidate.joinToString("")
                        if ((option in wordListIndex) && (option !in visited)) result.add(option)
                    }
                }
                return result
            }

            val queue = ArrayDeque<Pair<String, Int>>()

            queue.add(beginWord to 0)
            while (queue.isNotEmpty()) {
                val (node, lvl) = queue.removeFirst()
                if (node == endWord) return lvl + 1

                visited.add(node)
                val options = nextTransitions(node)
                options.forEach { queue.add(it to lvl + 1) }
            }

            return 0
        }

        /**
         * # Reference solution 1 — level-synchronous BFS, mark on **enqueue**
         *
         * ## Restatement
         *
         * Start at `beginWord`. One move = change exactly one letter, and the word you land on must
         * be in the dictionary. Reach `endWord` in as few moves as possible and report how many
         * **words** the whole chain contains (`moves + 1`), or `0` if it cannot be done.
         *
         * ## Pattern: BFS over an implicit graph
         *
         * Words are nodes, an edge joins two words differing in one letter, every edge costs 1 —
         * so "shortest chain" is literally "shortest path in an unweighted graph", which is BFS.
         * "Implicit" means the adjacency list is never built: neighbours are *generated* by
         * rewriting each of the `L` positions with each of the 26 letters and keeping the
         * candidates present in the dictionary set. Building edges by comparing all pairs would
         * cost `O(N² * L)` (2.5e8 comparisons at `N = 5000`); generating costs `O(L² * A)` per word.
         * DFS is the wrong tool here — it finds *a* chain, not the shortest one.
         *
         * ## Approach
         *
         * 1. Put the dictionary in a `HashSet` for `O(L)` membership tests. If `endWord` is not in
         *    it, return 0 immediately — no chain can end on a word outside the dictionary.
         *    (`beginWord`, by contrast, need *not* be in the list.)
         * 2. Seed the queue with `beginWord` and `steps = 1` (we count words, not hops).
         * 3. Drain the queue one **whole level** at a time. Every word in the current level is at
         *    the same distance, so `steps` is a per-level value and does not need to travel inside
         *    the queue as a `Pair`.
         * 4. For each word, try all `L * 26` single-letter edits. A candidate that equals `endWord`
         *    ends the search right there (`steps + 1`); any other dictionary word that has not been
         *    seen goes into the next level.
         * 5. Queue empties without hitting `endWord` -> no path -> 0.
         *
         * ## The one difference from the attempt above
         *
         * `visited` is updated when a word is **enqueued**, not when it is dequeued, and
         * [MutableSet.add] does the test-and-mark in a single hash probe (it returns `false` if the
         * element was already there). Marking on dequeue lets *every* neighbour on the current
         * level enqueue its own copy of the same next-level word, so a word can be enqueued once
         * per distinct shortest path leading to it — factorial blow-up in the worst case (see the
         * table in the analysis above `ladderLength`). Correctness survives, since duplicates all
         * sit at the same level; only time and memory suffer. **Mark on enqueue is the invariant to
         * internalise: a node enters `visited` exactly once, therefore it is expanded exactly once.**
         *
         * Two smaller points: the `chars` array is allocated once per word and the mutated position
         * is restored after the 26 letters (the attempt re-allocates it `L * 26` times), and
         * `String(chars)` avoids `joinToString`'s [StringBuilder] + separator check per element.
         *
         * ## Complexity
         *
         * With `N` = dictionary size, `L` = word length, `A = 26`:
         * - **Time `O(N * L² * A)`** — each word is expanded at most once; one expansion tries
         *   `L * A` candidates and each candidate costs `O(L)` to build and hash.
         * - **Space `O(N * L)`** — the dictionary set, `visited`, and a frontier that can hold at
         *   most every word once.
         *
         * ## Pitfalls
         *
         * - Returning hops instead of words (off by one). Seed with 1, or seed with 0 and return
         *   `+ 1`; do not mix the two.
         * - Forgetting the `endWord !in dict` guard: without it the search burns the whole graph
         *   before returning 0.
         * - Marking `visited` on dequeue (above), or forgetting `visited` altogether — the word
         *   graph is cyclic, so an unguarded BFS never terminates.
         * - Skipping `l == chars[i]` matters only for speed, but *forgetting to restore* `chars[i]`
         *   after the letter loop is a real correctness bug: the rest of the edits would then be
         *   applied on top of a corrupted word.
         */
        fun referenceSolution(beginWord: String, endWord: String, wordList: List<String>): Int {
            val dict = wordList.toHashSet()
            if (endWord !in dict) return 0

            val visited = hashSetOf(beginWord)
            var frontier = ArrayDeque(listOf(beginWord))
            var steps = 1

            while (frontier.isNotEmpty()) {
                val next = ArrayDeque<String>()
                for (word in frontier) {
                    val chars = word.toCharArray()
                    for (i in chars.indices) {
                        val original = chars[i]
                        for (letter in 'a'..'z') {
                            if (letter == original) continue
                            chars[i] = letter
                            val candidate = String(chars)
                            if (candidate == endWord) return steps + 1
                            if (candidate in dict && visited.add(candidate)) next.add(candidate)
                        }
                        chars[i] = original
                    }
                }
                frontier = next
                steps++
            }

            return 0
        }

        /**
         * # Reference solution 2 — bidirectional BFS (the practical speed-up)
         *
         * A BFS frontier grows like `b^d` (branching factor `b`, depth `d`). Searching from *both*
         * ends and meeting in the middle turns that into `2 * b^(d/2)` — for `b = 10, d = 6` that is
         * ~2 000 nodes touched instead of ~1 000 000. Legal here because the "differs by one letter"
         * relation is symmetric, so the reversed graph is the same graph.
         *
         * Mechanics worth remembering, all three of which are easy to get wrong:
         * - Each side keeps its **own** `visited` set. Sharing one set lets one side swallow a word
         *   the other side still needs as a meeting point.
         * - The meeting test is `candidate in otherFrontier`, not `in otherVisited` — the frontier
         *   is the set whose exact distance you currently know.
         * - Always expand the **smaller** frontier (the `swap` below). That policy is where the
         *   saving comes from; it is a sequential trick, not concurrency.
         *
         * Word count: `steps` starts at 1 and is incremented once per expanded level *on either
         * side*, so while expanding a word at forward depth `df` against a backward frontier at
         * depth `db` we have `steps == 1 + df + db`, and the joined chain has `df + db + 2` words —
         * hence `steps + 1`.
         *
         * Same asymptotic bound as reference 1 in the worst case (`O(N * L² * A)` time,
         * `O(N * L)` space); dramatically fewer expansions in practice.
         */
        fun referenceSolution2(beginWord: String, endWord: String, wordList: List<String>): Int {
            val dict = wordList.toHashSet()
            if (endWord !in dict) return 0

            var head = hashSetOf(beginWord)
            var tail = hashSetOf(endWord)
            var headVisited = hashSetOf(beginWord)
            var tailVisited = hashSetOf(endWord)
            var steps = 1

            while (head.isNotEmpty() && tail.isNotEmpty()) {
                if (head.size > tail.size) {
                    head = tail.also { tail = head }
                    headVisited = tailVisited.also { tailVisited = headVisited }
                }

                val next = hashSetOf<String>()
                for (word in head) {
                    val chars = word.toCharArray()
                    for (i in chars.indices) {
                        val original = chars[i]
                        for (letter in 'a'..'z') {
                            if (letter == original) continue
                            chars[i] = letter
                            val candidate = String(chars)
                            if (candidate in tail) return steps + 1
                            if (candidate in dict && headVisited.add(candidate)) next.add(candidate)
                        }
                        chars[i] = original
                    }
                }
                head = next
                steps++
            }

            return 0
        }

    }
}
