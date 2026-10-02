package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 20. Valid Parentheses  (https://leetcode.com/problems/valid-parentheses/)
 *
 * Given a string `s` containing just the characters `'('`, `')'`, `'{'`, `'}'`, `'['` and `']'`,
 * determine if the input string is valid.
 *
 * An input string is valid if:
 * 1. Open brackets must be closed by the same type of brackets.
 * 2. Open brackets must be closed in the correct order.
 * 3. Every close bracket has a corresponding open bracket of the same type.
 *
 * Constraints:
 * - 1 <= s.length <= 10^4
 * - `s` consists of parentheses only `'()[]{}'`.
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
            "(" expects false,            // single element: lone opener, never closed
            ")" expects false,            // single element: closer on an empty stack
            "((" expects false,           // only openers: leftovers at the end
            "{[]}" expects true,          // nested, different types
            "(){}}{" expects false,       // closer before its opener (empty stack mid-string)
            "([{}])[]{()}" expects true,  // deep nesting + siblings
            "[[[]" expects false,         // balanced suffix, unmatched prefix left on stack
            "){" expects false,           // reversed pair: closer first, opener never closed
            "((((((((((()))))))))))" expects true, // deep single-type nesting (stack depth 11)
            "[{]}" expects false,         // counts balance per type, but interleaved order is wrong
            "(}" expects false,           // wrong type on a non-empty stack, length 2
        )

        @Test
        fun test() = check(::isValid, ::referenceSolution)

        /**
         * ## Analysis (validated: all 16 cases pass)
         *
         * **Pattern:** a *matching-brackets stack*, the standard LIFO way to check that pairs are
         * properly nested. The most recently opened bracket has to be the first one closed, and
         * "last in, first out" is exactly what a stack does. The same template is used for HTML/XML
         * tag matching, expression parsing and undo history.
         *
         * **Time: O(n).** `s.forEach` visits each character once. Inside the loop, `it in open`
         * is a set lookup, `map[it]` is a map lookup, and `addLast` / `removeLastOrNull` on
         * `ArrayDeque` cost amortized O(1). Building `map` and `open` is O(1) because there are
         * always 3 pairs. The early `return false` lines only make it faster: best case O(1) for
         * input like `")..."`, worst case O(n) for valid input.
         *
         * **Space: O(n) auxiliary.** The deque grows by one for each unclosed opener. The worst
         * case is all openers, e.g. `"(((("`, which reaches n entries. Note that `ArrayDeque<Char>`
         * boxes each `Char`. `map` and `open` take O(1) space. The output is just a `Boolean`.
         *
         * **Correctness notes:**
         * - It catches all three failure modes:
         *   1. A closer when the stack is empty: `removeLastOrNull()` returns `null`, and `null != b`
         *      makes it return false.
         *   2. A closer of the wrong type: the popped char differs from `b`.
         *   3. Openers that are never closed: the final `stack.isEmpty()` check.
         *   Forgetting #3 is the classic bug (`"(("` would then pass).
         * - `map[it] ?: return false` also rejects any character that isn't a bracket. The
         *   constraints rule those out, but it's a safe default.
         * - The map is keyed **closer → opener**, so a closing char looks up its expected partner
         *   directly. That's the convenient direction for this problem.
         * - Possible micro-optimization: if `s.length` is odd, return false right away, since an
         *   odd-length string can't be balanced. It doesn't change the Big-O.
         *
         * **Alternatives:**
         * - *Push the expected closer:* when you see `(`, push `)`. When you see a closer, pop and
         *   compare it directly. This drops the map lookup on closers. Same O(n)/O(n).
         * - *Use a `CharArray` with an index as the stack:* O(n) space but with no boxing, and
         *   noticeably faster on the JVM. This is what you'd write in a hot path.
         * - *Repeatedly delete `"()"`, `"[]"` and `"{}"` until nothing changes:* simple to read,
         *   but O(n²) time and it allocates a new string on every pass. Avoid it.
         * - *Count each bracket type (one counter per type):* O(1) space, but **wrong** for mixed
         *   types. `"([)]"` and `"[{]}"` balance per type yet are invalid. A single counter is only
         *   enough when there is just one bracket type (e.g. LC 1249 / 921).
         * - The solution is asymptotically optimal. Every character has to be read (Ω(n)), and in
         *   the worst case you need Ω(n) memory to remember which opener types are still waiting.
         *
         * **Parallelism:** in theory, yes. Each chunk reduces to a short "unmatched closers +
         * unmatched openers" summary, and those summaries combine associatively, so you can do a
         * parallel prefix/reduce in O(n/p + log p). That's the basis of GPU and SIMD bracket
         * matching (e.g. simdjson's structural index). For n ≤ 10^4 it isn't worth it: starting
         * threads costs microseconds, while the whole sequential scan takes well under one. The
         * dependency is inherently sequential (each pop depends on every earlier push), so you'd
         * only parallelize this for multi-MB inputs.
         *
         * **Real world:** parsers and lexers (JSON, XML/HTML tag balancing, compilers),
         * editor features like bracket-pair colorizers and auto-indent, and linters. In practice
         * you rarely validate brackets alone. A real parser tracks them as part of its grammar,
         * and it has to deal with brackets inside string literals and comments, which this
         * problem ignores. Streaming validators keep only the stack and never buffer the whole
         * input, and they cap stack depth to guard against nesting-bomb DoS inputs (e.g. Jackson's
         * `maxNestingDepth`).
         */
        fun isValid(s: String): Boolean {
            val stack = ArrayDeque<Char>()
            val map = mapOf(
                '}' to '{',
                ')' to '(',
                ']' to '[',
            )
            val open = map.values.toSet()

            s.forEach {
                if (it in open) {
                    stack.addLast(it)
                } else {
                    val b = map[it] ?: return false
                    if (stack.removeLastOrNull() != b) return false
                }
            }

            return stack.isEmpty()
        }

        /**
         * ## Reference solution: Valid Parentheses
         *
         * **Restatement.** You get a string made only of `()[]{}`. It is valid when every bracket is
         * closed by a bracket of the same type, and pairs never cross. They may nest (`([])`) or sit
         * side by side (`()[]`), but `([)]` is invalid.
         *
         * **Pattern: matching-brackets stack (LIFO).** Nesting means the bracket opened *last* must be
         * closed *first*, and "last in, first out" is exactly what a stack does. Whenever a problem
         * says "most recent unmatched thing" (tags, nested calls, undo), reach for a stack.
         *
         * ### Intuition
         * At any point while scanning, the only closer that is allowed next is the partner of the
         * most recent opener that is still open. So keep the still-open brackets on a stack. A closer
         * must match the top of the stack. When the scan ends, nothing may be left open.
         * An easy shortcut: when you see an opener, push the closer you *expect*. A closer then just
         * pops and compares, so you need no lookup table.
         *
         * ### Core concepts
         * - **Stack / LIFO:** last pushed, first popped. It models "most recent unfinished item",
         *   which is exactly the bracket a closer has to match.
         * - **Proper nesting (well-formedness):** pairs may contain each other but never overlap.
         *   Per-type counters cannot detect overlap (`"([)]"` balances every type), and a stack can.
         * - **Expected-value stack:** push what you will need later (the closer), not what you saw
         *   (the opener). This removes the mapping step when you pop. The same trick works for
         *   tag matching and for decoding nested structures.
         * - **Array-backed stack:** a primitive array plus a `top` index is a stack with no boxing
         *   and no resizing, as long as the maximum depth is known (here at most n).
         *
         * ### Approach
         * 1. If `s.length` is odd, return false: an odd number of brackets cannot form pairs.
         * 2. Allocate `stack = CharArray(n)` and set `top = 0`.
         * 3. For each char `c`:
         *    - For an opener, push its closer: `(`→`)`, `[`→`]`, `{`→`}`.
         *    - For a closer, return false if the stack is empty (`top == 0`) or the popped value is
         *      not `c`.
         * 4. Return `top == 0`. Any brackets still open mean the string is invalid.
         *
         * ### Complexity
         * - **Time O(n):** one pass, with O(1) work per character.
         * - **Space O(n):** the stack holds up to n/2 entries on a valid path. It is allocated as n,
         *   and all-openers input fills it up. Ω(n) is needed in the worst case to remember which
         *   types are still open.
         *
         * ### Common pitfalls
         * - **Forgetting the final emptiness check:** without it, `"(("` would be accepted.
         * - **Popping an empty stack:** a closer that comes first (`")("`) has to return false. It
         *   must not throw.
         * - **Counting instead of stacking:** counters per type accept `"([)]"`. One counter is
         *   enough only when there is a single bracket type.
         * - **Mapping in the wrong direction:** if you store opener→closer but then look up a
         *   closer, you get null everywhere. Pushing the expected closer avoids that mapping at the
         *   use site.
         * - **Repeatedly removing `"()"`/`"[]"`/`"{}"`:** correct, but O(n²) and it creates a new
         *   string on every pass.
         */
        fun referenceSolution(s: String): Boolean {
            if (s.length % 2 == 1) return false
            val stack = CharArray(s.length)
            var top = 0
            for (c in s) {
                when (c) {
                    '(' -> stack[top++] = ')'
                    '[' -> stack[top++] = ']'
                    '{' -> stack[top++] = '}'
                    else -> if (top == 0 || stack[--top] != c) return false
                }
            }
            return top == 0
        }
    }
}
