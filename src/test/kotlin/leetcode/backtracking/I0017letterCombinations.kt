package leetcode.backtracking

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 17. Letter Combinations of a Phone Number  (https://leetcode.com/problems/letter-combinations-of-a-phone-number/)
 *
 * Given a string containing digits from 2-9 inclusive, return all possible letter combinations that the
 * number could represent (as on a classic telephone keypad). Return the answer in any order.
 *
 * Mapping: 2->abc, 3->def, 4->ghi, 5->jkl, 6->mno, 7->pqrs, 8->tuv, 9->wxyz.
 *
 * Constraints:
 * - 0 <= digits.length <= 4
 * - digits[i] is a digit in the range ['2', '9']
 * - Empty input must return an empty list (NOT a list containing one empty string).
 */
typealias I0017 = (String) -> List<String>

class I0017letterCombinations {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0017> {

        // NOTE: LeetCode allows ANY order. The harness compares List<String> order-sensitively,
        // so the expected values below use the canonical 'digits-major, mapping-order' sequence
        // that a straightforward backtracking solution produces. If your output is correct but in
        // a different order, sort both sides (or reorder your traversal) to match.
        override val cases = _root_ide_package_.leetcode.testCases<I0017>(
            "23" expects """["ad","ae","af","bd","be","bf","cd","ce","cf"]""",
            "" expects "[]",
            "2" expects """["a","b","c"]""",
        )

        @Test
        fun test() = check(
            ::letterCombinations,
            ::solution1,
//            ::community wrong order
        )

        /**
         * ANALYSIS — letterCombinations (backtracking)
         *
         * Pattern: classic **backtracking / DFS over a decision tree** to enumerate a Cartesian
         * product. At depth `i` the choices are the letters mapped to `digits[i]`; we choose a letter
         * (`state.addLast`), recurse to the next digit, then un-choose (`state.removeLast`). Reaching
         * past the last index is a leaf → emit the path. This is the "choose / explore / un-choose"
         * template that transfers to permutations, combinations, subsets, etc.
         *
         * Time:  O(n · 4^n), n = digits.length.
         *   - Each digit branches into 3 or 4 letters (7 and 9 give 4), so the tree has up to 4^n leaves.
         *   - At each of the ≤4^n leaves we do `state.joinToString("")` over n chars → the extra ·n factor.
         *   This is asymptotically optimal: the output alone has up to 4^n strings, so you cannot do better.
         *
         * Space:
         *   - Auxiliary (excluding output): O(n) — recursion stack depth n plus the `state` buffer of size n.
         *     `mapping` is a fixed 8-entry map (O(1)), though it is re-allocated once per top-level call.
         *   - Output: O(n · 4^n) for the materialized result list.
         *
         * Correctness — edge cases:
         *   - Non-empty inputs in ['2'..'9']: correct; traversal order is digits-major, mapping-order,
         *     which is exactly the canonical ordering the order-sensitive harness expects.
         *   - The `?: throw` guards digits outside the map (defensive; constraints rule this out).
         *   - ⚠️ EMPTY INPUT BUG. For digits == "" we have lastIndex == -1, so the very first call hits
         *     `from (0) > -1` → true → it emits one empty string. So the method returns [""], but real
         *     LeetCode (and the constraint on line 17) requires []. The test only goes GREEN because the
         *     expected literal "[ ]" is parsed by the harness's flat-string-list converter as [""], not [].
         *     (arraySplit does `"".split(",")` which yields [""], never an empty list — so a truly empty
         *     List<String> is currently *inexpressible* in a case. The expected value and the bug coincide.)
         *     Fix on the real problem: early-return emptyList() when digits.isEmpty() before backtracking.
         *
         * Alternative approaches (same O(4^n) — output-bound — so none beats this asymptotically):
         *   - Iterative BFS build: start with listOf(""), and for each digit expand every partial by every
         *     mapped letter. No recursion stack, but it holds a full frontier of partials in memory.
         *   - Cartesian product via nested fold / `flatMap`. Cleaner functionally, same cost.
         *   Micro: a top-level `Array<CharArray>` indexed by (digit - '2') avoids rebuilding `mapping`, and
         *   threading a StringBuilder instead of joinToString-per-leaf trims allocations. Both are constant-factor.
         *
         * Parallelism: the subtrees under each first-digit letter are fully independent (embarrassingly
         * parallel — partition by prefix, map-reduce the leaf lists). But with n ≤ 4 there are at most
         * 4^4 = 256 combinations, so thread/fork overhead dwarfs the work — not worth it here. The teaching
         * point: it *is* parallelizable in principle; the tiny constraint bound is what kills the payoff.
         *
         * Real-world: this is T9 predictive text on old phone keypads, and more generally Cartesian-product
         * generation — combinatorial test-matrix expansion, feature-flag/config combinations, search-query
         * expansion, brute-force candidate generation. At scale you would *stream* combinations lazily
         * (a Sequence/generator) rather than materialize all 4^n, and prune against a dictionary (T9 keeps
         * only real words) instead of enumerating blindly.
         */
        fun letterCombinations(digits: String): List<String> {
            if (digits.isEmpty()) return emptyList()
            val result = mutableListOf<String>()
            val mapping = mapOf(
                '2' to listOf('a', 'b', 'c'),
                '3' to listOf('d', 'e', 'f'),
                '4' to listOf('g', 'h', 'i'),
                '5' to listOf('j', 'k', 'l'),
                '6' to listOf('m', 'n', 'o'),
                '7' to listOf('p', 'q', 'r', 's'),
                '8' to listOf('t', 'u', 'v'),
                '9' to listOf('w', 'x', 'y', 'z'),
            )

            val state = mutableListOf<Char>()

            fun backtrack(from: Int = 0) {
                if (from > digits.lastIndex) {
                    result.add(state.joinToString(""))
                    return
                }


                val charMap = mapping[digits[from]] ?: throw IllegalStateException()
                for (j in charMap) {
                    state.addLast(j)
                    backtrack(from + 1)
                    state.removeLast()
                }

            }

            backtrack()
            return result
        }

        fun solution1(digits: String): List<String> {
            if (digits.isEmpty()) return emptyList()

            val phoneMap = mapOf(
                '2' to "abc", '3' to "def", '4' to "ghi",
                '5' to "jkl", '6' to "mno", '7' to "pqrs",
                '8' to "tuv", '9' to "wxyz"
            )

            fun Map<Char, String>.charAt(char: Char, idx: Int) = this.getValue(char)[idx]
            fun Map<Char, String>.lastIndexAt(char: Char) = this.getValue(char).lastIndex

            fun backtrack(
                result: MutableList<String>,
                charIdx: Int,
                temp: MutableList<Char> = mutableListOf<Char>(),
                digits: List<Char>,
            ) {
                if (temp.size == digits.size) {
                    result.add(temp.joinToString(""))
                    return
                }

                (0..phoneMap.lastIndexAt(digits[charIdx])).forEach { i ->
                    temp.add(phoneMap.charAt(digits[charIdx], i))
                    backtrack(result, charIdx + 1, temp, digits)
                    temp.removeAt(temp.lastIndex)
                }

            }

            val result = mutableListOf<String>()
            backtrack(
                result = result,
                charIdx = 0,
                digits = digits.toList()
            )
            return result
        }


        fun community(digits: String): List<String> = digits.takeIf {
            it.isNotEmpty()
        }?.fold(listOf("")) { acc, c ->
            c.letters.flatMap { letter -> acc.map { it + letter } }
        } ?: emptyList()

        private val Char.letters
            get() = when (this) {
                '2' -> listOf('a', 'b', 'c')
                '3' -> listOf('d', 'e', 'f')
                '4' -> listOf('g', 'h', 'i')
                '5' -> listOf('j', 'k', 'l')
                '6' -> listOf('m', 'n', 'o')
                '7' -> listOf('p', 'q', 'r', 's')
                '8' -> listOf('t', 'u', 'v')
                '9' -> listOf('w', 'x', 'y', 'z')
                else -> listOf()
            }

    }
}
