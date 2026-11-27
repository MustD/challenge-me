package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 20. Valid Parentheses  (https://leetcode.com/problems/valid-parentheses/)
 *
 * Given a string `s` containing just the characters `'('`, `')'`, `'{'`, `'}'`, `'['` and `']'`, determine if the
 * input string is valid.
 *
 * An input string is valid if:
 * 1. Open brackets must be closed by the same type of brackets.
 * 2. Open brackets must be closed in the correct order.
 * 3. Every close bracket has a corresponding open bracket of the same type.
 *
 * Constraints:
 * - 1 <= s.length <= 10^4
 * - s consists of parentheses only '()[]{}'.
 */
typealias I0020 = (String) -> Boolean

class I0020isValid {

    @Nested
    inner class Solution : ProblemTest<I0020> {

        override val cases = testCases<I0020>(
            "()" expects true,
            "()[]{}" expects true,
            "(]" expects false,
            "([])" expects true,
            "([)]" expects false,
            "(" expects false,              // n = 1, lone opener
            "]" expects false,              // closer on an empty stack
            "((" expects false,             // all openers, leftovers at the end
            "(){}}{" expects false,         // extra closer mid-string, then a fresh opener
            "{[()()]}" expects true,        // deep nesting with siblings
            "[({})]([]){}" expects true,    // nested groups in sequence
            "[[[]" expects false,           // odd count, partially closed
        )

        @Test
        fun test() = check(::isValid)

        /**
         * ## Analysis (lt-check) — verified: 5 official + 7 added cases
         *
         * **Time: O(n).** One pass over `s`; each character does an O(1) map lookup (3-entry `mapOf`, effectively
         * constant) and at most one amortized O(1) `addLast` / `removeLastOrNull` on the `ArrayDeque`. Best case
         * exits early on the first mismatched closer (e.g. `"]..."` returns at index 0).
         *
         * **Space: O(n) auxiliary.** Worst case is all openers (`"(((("`): the deque holds n chars. The map is O(1).
         *
         * **Optimal: O(n) time / O(n) space** — reached. Every char must be read (any unread char could break validity),
         * and a stack of depth up to n/2 is unavoidable in general for three bracket kinds.
         *
         * **Correctness notes**
         * - The invariant: the deque holds the still-open brackets, innermost on top. A closer is legal only if it
         *   matches the top — that enforces both "same type" and "correct order" in a single check.
         * - The empty-stack closer (`"]"`, `"(){}}{"`) is handled neatly: `removeLastOrNull()` yields `null`, which never
         *   equals a `Char`, so it falls into `return false` with no separate guard.
         * - `return queue.isEmpty()` catches unclosed leftovers (`"("`, `"(("`, `"[[["`). Forgetting this final check is
         *   the classic bug in this problem.
         * - The `else` branch treats "anything that is not a closer" as an opener — safe only because the constraints
         *   guarantee the alphabet is `()[]{}`.
         * - Naming nit: the variable is called `queue`, but it is used strictly LIFO — it is a **stack**. Naming it
         *   `stack` makes the invariant self-documenting.
         *
         * **Pattern: stack for matching nested structure** (balanced delimiters). Push on open, pop-and-compare on close,
         * require empty at the end. The closer→opener map is the usual trick to avoid a `when` per bracket type.
         *
         * **Alternatives**
         * - *Push the expected closer instead:* on `'('` push `')'`, etc.; on a closer just compare with the pop. Same
         *   complexity, avoids the map lookup on the close path.
         * - *Odd-length early exit:* `if (s.length % 2 == 1) return false` — O(1) shortcut, doesn't change Big-O.
         * - *Counters instead of a stack:* O(1) space, but only correct for a **single** bracket type; with three types
         *   counters can't detect `"([)]"`, which is exactly why the stack is needed here.
         * - *Repeated `replace("()", "")` etc. until fixpoint:* simple to write but O(n²) — a good example of a
         *   "correct but quadratic" approach.
         *
         * **Parallelism:** the input is at most 10^4 chars, so threads would cost more than they save. Theoretically the
         * problem is parallelizable: each chunk reduces to an irreducible "unmatched closers + unmatched openers" pair,
         * and pairs combine associatively (a parallel reduce / prefix-scan), giving O(n/p + log p) — that is how
         * GPU / SIMD JSON validators handle huge inputs. Here it is pure overhead.
         *
         * **Real world:** compilers' and IDEs' bracket matching, JSON/XML/HTML well-formedness checks (tag stack),
         * expression parsers (shunting-yard), and undo/call stacks. Production parsers also track *positions* on the
         * stack to report "unclosed `{` at line 12" rather than just `false`.
         */
        fun isValid(s: String): Boolean {
            val queue = ArrayDeque<Char>()
            val map = mapOf(')' to '(', '}' to '{', ']' to '[')
            s.forEach { ch ->
                if (map.contains(ch)) {
                    if (queue.removeLastOrNull() != map[ch]) return false
                } else {
                    queue.addLast(ch)
                }
            }
            return queue.isEmpty()
        }

    }
}
