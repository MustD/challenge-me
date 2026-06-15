package leetcode.backtracking

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 22. Generate Parentheses  (https://leetcode.com/problems/generate-parentheses/)
 *
 * Given `n` pairs of parentheses, generate all combinations of well-formed (balanced)
 * parentheses strings.
 *
 * Constraints:
 * - 1 <= n <= 8
 *
 * Note on ordering: the harness compares List<String> order-sensitively. The expected
 * values below use the standard backtracking traversal order (always try '(' before ')'),
 * which is the order LeetCode lists them in. If you build the strings a different way,
 * you may need to match this ordering.
 */
typealias I0022 = (Int) -> List<String>

class I0022generateParenthesis {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0022> {

        override val cases = leetcode.testCases<I0022>(
            1 expects """["()"]""",
            2 expects """["(())","()()"]""",
            3 expects """["((()))","(()())","(())()","()(())","()()()"]""",
        )

        @Test
        fun test() = check(::generateParenthesis)

        /**
         * Analysis — verified against the test cases above (all pass).
         *
         * Pattern: **backtracking with a validity constraint** (a.k.a. constrained DFS).
         * Instead of generating all 2^(2n) raw strings and filtering for balance, the
         * recursion only ever extends a *prefix that can still become valid*. Two invariants
         * enforce this:
         *   - `open < n`     → we may still open a new pair (cap on '(' count).
         *   - `open > close` → a ')' is only legal if there is an unmatched '(' to close.
         * A leaf (`state.length == 2 * n`) is therefore always a complete, well-formed string,
         * so no post-filtering is needed. The single shared `StringBuilder` is mutated on the
         * way down and restored on the way up (`deleteCharAt(lastIndex)`) — the canonical
         * make-move / recurse / undo-move structure.
         *
         * Correctness notes:
         *   - The two `if`s try '(' before ')', which is exactly the lexicographic / LeetCode
         *     traversal order — this is why the order-sensitive harness equality passes without
         *     a sort.
         *   - n >= 1 (per constraints) so the empty-input case never arises; if n == 0 it would
         *     still correctly return [""] since the length check fires immediately.
         *   - No integer-overflow risk: n <= 8, counts stay tiny.
         *   - The shared mutable `StringBuilder` is safe because every append is paired with an
         *     undo before the function returns, leaving `state` unchanged for the caller.
         *
         * Time complexity: **O(n * Catalan(n)) = O(4^n / sqrt(n))**.
         *   The number of valid strings is the n-th Catalan number C(n) ~ 4^n / (n^1.5 * sqrt(pi)).
         *   The pruned recursion tree visits O(C(n)) leaves with effectively constant branching
         *   waste, and producing each leaf costs O(n) for `state.toString()` (copies 2n chars).
         *   Hence O(n) * C(n). This is optimal in the asymptotic sense — you cannot enumerate
         *   C(n) strings of length 2n in less than the total output size.
         *
         * Space complexity: **O(n) auxiliary** (excluding the output list).
         *   - Recursion stack depth is at most 2n (one frame per appended char).
         *   - `state` holds at most 2n chars.
         *   The output itself is O(n * C(n)) but that is required result space, not working space.
         *
         * Alternative approaches:
         *   - **Closure-number / divide-and-conquer (Catalan recurrence):** every valid string is
         *     "(" + A + ")" + B where A uses i pairs and B uses n-1-i pairs. Build results for
         *     each split and combine. Same O(4^n/sqrt(n)) output-bound time, but typically more
         *     allocation (intermediate lists per subproblem) and harder to keep in LeetCode order.
         *     The backtracking version here is leaner and the more idiomatic interview answer.
         *   - **Brute force:** enumerate all 2^(2n) strings, keep the balanced ones. O(2^(2n) * n)
         *     — strictly worse, and pointless given the constraint already prunes perfectly.
         *   - A micro-optimization on *this* solution: pass the prefix as an immutable String to
         *     avoid the explicit undo, trading the deleteCharAt for per-call allocation — cleaner
         *     but a touch more garbage. The StringBuilder + undo here is the more efficient choice.
         *
         * Parallelism / multithreading: **not worth it.** With n <= 8 the entire search finishes
         * in microseconds and C(8) is only 1430 strings; thread-spawn overhead would dwarf the work.
         * In principle the search *is* embarrassingly parallel — you could fork independent subtrees
         * (e.g. one task per choice of the first split point in the closure-number formulation) since
         * the subproblems share no mutable state. But the shared-StringBuilder design here is
         * deliberately sequential; a parallel version would need per-task buffers. The honest takeaway:
         * the bottleneck is output size, not compute, so parallelism gives near-zero real speedup.
         *
         * Real-world experience: the *shape* of this problem — generating all valid structures under
         * grammatical constraints — shows up in parser/expression-grammar fuzzing, generating valid
         * test inputs for compilers, and balanced-bracket / matching-delimiter checks in editors and
         * linters. In production you almost never enumerate *all* valid strings (the count explodes);
         * you either validate a single string in O(len) with a counter/stack, or sample/generate
         * lazily. The interview version asks for the full Catalan set; real systems lean on streaming
         * validation or grammar-driven generators (e.g. property-based testing libraries) that yield
         * structures on demand rather than materializing the whole space.
         */
        fun generateParenthesis(n: Int): List<String> {
            val result = mutableListOf<String>()
            val state = StringBuilder()

            fun backtrack(open: Int = 0, close: Int = 0) {
                if (state.length == 2 * n) {
                    result.add(state.toString())
                    return
                }

                if (open < n) {
                    state.append('(')
                    backtrack(open + 1, close)
                    state.deleteCharAt(state.lastIndex)
                }


                if (open > close) {
                    state.append(')')
                    backtrack(open, close + 1)
                    state.deleteCharAt(state.lastIndex)
                }


            }

            backtrack()
            return result
        }


    }
}
