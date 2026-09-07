package leetcode.graph_general

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 207. Course Schedule  (https://leetcode.com/problems/course-schedule/)
 *
 * There are `numCourses` courses labeled `0..numCourses - 1`. Each entry
 * `prerequisites[i] = [a, b]` means: you must take course `b` before course `a`.
 * Return `true` if it is possible to finish every course, `false` otherwise.
 *
 * Constraints:
 * - 1 <= numCourses <= 2000
 * - 0 <= prerequisites.length <= 5000  (may be empty — then the answer is trivially true)
 * - prerequisites[i].length == 2
 * - 0 <= a, b < numCourses
 * - all prerequisite pairs are distinct (but a course may still appear in many pairs,
 *   and the graph may be disconnected — don't assume a single component)
 */
typealias I0207 = (Int, Array<IntArray>) -> Boolean

class I0207canFinish {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0207> {

        override val cases = leetcode.testCases<I0207>(
            leetcode.args(2, "[[1,0]]") expects true,
            leetcode.args(2, "[[1,0],[0,1]]") expects false,
            leetcode.args(1, "[]") expects true,
            leetcode.args(5, "[[1,4],[2,4],[3,1],[3,2]]") expects true,
            leetcode.args(4, "[[1,0],[2,1],[3,2],[1,3]]") expects false,
            leetcode.args(3, "[[0,1],[0,2]]") expects true,
            // acyclic, but two starting edges reach node 2 from different paths (diamond)
            leetcode.args(5, "[[0,1],[1,2],[3,4],[4,2]]") expects true,
            // plain chain 3 -> 2 -> 1 -> 0, walked twice from different starting edges
            leetcode.args(4, "[[0,1],[1,2],[2,3]]") expects true,
            leetcode.args(4, "[[0,0]]") expects false,
            // DAG: chain 0 -> 1 -> 2 -> 3 plus the shortcut 1 -> 3; node 3 is re-reached
            // by a longer path *within one traversal*, which the diamond case above misses
            leetcode.args(4, "[[0,1],[1,2],[2,3],[1,3]]") expects true,
        )

        @Test
        fun test() = check(::canFinish, ::referenceSolution, ::referenceSolution2)

        /**
         * Kahn from [referenceSolution]
         */
        fun canFinish(numCourses: Int, prerequisites: Array<IntArray>): Boolean {
            val graph = Array(numCourses) { mutableListOf<Int>() }
            val inDegree = IntArray(numCourses) { 0 }

            prerequisites.forEach { (course, prereq) ->
                graph[prereq].add(course)
                inDegree[course]++
            }

            val queue = ArrayDeque<Int>()
            inDegree.indices.asSequence().filter { inDegree[it] == 0 }.forEach { queue.addLast(it) }

            var processed = 0
            while (queue.isNotEmpty()) {
                val course = queue.removeFirst()
                processed++
                graph[course].asSequence()
                    .onEach { inDegree[it]-- }
                    .filter { inDegree[it] == 0 }
                    .forEach { queue.addLast(it) }
            }

            return numCourses == processed
        }

        /**
         * ## Restatement
         *
         * The courses are the vertices of a directed graph and each pair `[a, b]` is a directed
         * edge. The question "can every course be finished?" is exactly the question
         * "**is this directed graph acyclic?**" — if some set of courses depends on itself in a
         * loop, none of them can ever be started, and otherwise a valid order always exists.
         *
         * ## Pattern
         *
         * **Cycle detection in a directed graph / topological sort.** Two standard templates
         * solve it, and both are worth knowing because problem 210 (Course Schedule II) asks
         * for the order itself, which is what these two produce as a by-product:
         *
         * - **Kahn's algorithm (BFS)** — repeatedly remove a vertex with no remaining incoming
         *   edges. If every vertex can be removed this way, the graph is acyclic.
         * - **DFS with three colours** — walk the graph depth-first and look for an edge that
         *   points back into the path currently on the recursion stack.
         *
         * ### Intuition
         *
         * A course is takeable the moment all of its prerequisites are done. So repeatedly peel
         * off every course whose prerequisite count has dropped to zero, and decrement the
         * counters of the courses it unlocks. Courses trapped in a cycle can never reach a
         * count of zero — each one is waiting on another member of the same cycle — so they are
         * precisely the vertices left over when the peeling stops. Count what you managed to
         * peel: `processed == numCourses` means acyclic.
         *
         * The DFS view of the same fact: a cycle exists iff some DFS finds an edge leading to a
         * vertex that is *still open* on the current path (a back edge). An edge to a vertex
         * that was fully explored earlier is harmless — it is a cross/forward edge, not a cycle.
         *
         * ### Core concepts
         *
         * - **DAG (directed acyclic graph)** — a directed graph with no cycle. Feasibility here
         *   is literally "is the dependency graph a DAG"; recognising a problem as a DAG question
         *   immediately unlocks topological order, longest-path-in-DAG and DP-over-DAG tools.
         * - **Topological order** — a linear ordering of vertices where every edge points
         *   forward. It exists **iff** the graph is a DAG, which is why "can this be scheduled?"
         *   and "is there a topological order?" are the same question.
         * - **In-degree** — the number of incoming edges of a vertex, i.e. how many unmet
         *   prerequisites a course still has. Kahn's algorithm is nothing but "maintain
         *   in-degrees, release a vertex when its in-degree hits 0".
         * - **Back edge** — during DFS, an edge into a vertex that is on the current recursion
         *   stack (grey). A directed graph has a cycle **iff** a DFS finds a back edge. This is
         *   the reason a plain `visited` set is not enough: it cannot tell "on the current path"
         *   from "finished long ago".
         * - **Three-colour marking (white / grey / black)** — unvisited / in progress on the
         *   stack / fully explored. The two distinct marks are what make DFS cycle detection
         *   correct *and* keep it linear, since black vertices are never re-explored.
         *
         * ## Approach (Kahn / BFS — `referenceSolution`)
         *
         * 1. Build an adjacency list in the direction *prerequisite -> course it unlocks*, and
         *    an `inDegree[course]` counter, in one pass over `prerequisites`.
         * 2. Seed a queue with every course whose in-degree is already 0.
         * 3. Pop a course, count it as processed, and for each course it unlocks decrement that
         *    course's in-degree; push it when the counter reaches 0.
         * 4. Return `processed == numCourses`.
         *
         * ## Approach (DFS three-colour — `referenceSolution2`)
         *
         * 1. Build the adjacency list (either direction works; here *course -> its prerequisites*).
         * 2. Keep `state[v]` in {0 = white, 1 = grey/on stack, 2 = black/done}.
         * 3. `dfs(v)`: if grey -> cycle, return false; if black -> return true. Mark grey,
         *    recurse into neighbours, then mark **black** and return true.
         * 4. Run the DFS from every vertex (the graph may be disconnected).
         *
         * ## Complexity
         *
         * Both are **O(V + E)** time — each vertex enters the queue (or is coloured black) at
         * most once and each edge is relaxed exactly once — and **O(V + E)** space for the
         * adjacency list plus the in-degree array / colour array and the queue or recursion
         * stack. With V <= 2000 and E <= 5000 this is trivially fast.
         *
         * ## Common pitfalls
         *
         * - **Edge direction.** `[a, b]` means `b -> a` ("b before a"). Reversing it silently
         *   still detects cycles correctly (a graph has a cycle iff its reverse does), so the
         *   bug only surfaces on problem 210 where the emitted order matters — but be deliberate.
         * - **A single `visited` set in DFS.** Marking a node visited on entry and never
         *   distinguishing "on the current path" from "already finished" reports false cycles;
         *   *removing* the mark on exit without a second "done" mark is correct but degenerates
         *   to exponential time on a diamond/shortcut-heavy DAG. You need both marks.
         * - **Disconnected graphs.** Kahn must seed *all* zero-in-degree vertices and DFS must
         *   be started from every vertex; a single traversal from node 0 misses components.
         * - **Self-loop `[a, a]`** is a cycle of length 1 — check that your marking catches it.
         * - **Empty `prerequisites`** — the answer is `true`; make sure the loops handle it.
         * - **Recursion depth.** With V <= 2000 a recursive DFS is safe here, but on larger
         *   dependency graphs prefer Kahn's iterative version to avoid a stack overflow.
         */
        fun referenceSolution(numCourses: Int, prerequisites: Array<IntArray>): Boolean {
            val graph = Array(numCourses) { mutableListOf<Int>() }
            val inDegree = IntArray(numCourses)

            for ((course, prereq) in prerequisites) {
                graph[prereq].add(course)   // finishing `prereq` unlocks `course`
                inDegree[course]++
            }

            val queue = ArrayDeque<Int>()
            for (course in 0 until numCourses) {
                if (inDegree[course] == 0) queue.addLast(course)
            }

            var processed = 0
            while (queue.isNotEmpty()) {
                val course = queue.removeFirst()
                processed++
                for (next in graph[course]) {
                    if (--inDegree[next] == 0) queue.addLast(next)
                }
            }

            return processed == numCourses
        }

        /**
         * Same problem, DFS three-colour template. See the KDoc on [referenceSolution] for the
         * full walkthrough; the essential part is the two distinct marks:
         * `1` = grey (on the current recursion stack, an edge into it is a back edge = cycle),
         * `2` = black (fully explored, safe to short-circuit — this is what keeps it linear).
         */
        fun referenceSolution2(numCourses: Int, prerequisites: Array<IntArray>): Boolean {
            val graph = Array(numCourses) { mutableListOf<Int>() }
            for ((course, prereq) in prerequisites) {
                graph[course].add(prereq)   // `course` depends on `prereq`
            }

            val state = IntArray(numCourses)   // 0 = white, 1 = grey, 2 = black

            fun dfs(node: Int): Boolean {
                if (state[node] == 1) return false   // back edge -> cycle
                if (state[node] == 2) return true    // already proven acyclic
                state[node] = 1
                for (dep in graph[node]) {
                    if (!dfs(dep)) return false
                }
                state[node] = 2
                return true
            }

            return (0 until numCourses).all { dfs(it) }
        }
    }
}
