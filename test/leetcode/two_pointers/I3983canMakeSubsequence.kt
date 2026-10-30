package leetcode.two_pointers

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 3983. Subsequence After One Replacement  (https://leetcode.com/problems/subsequence-after-one-replacement/)
 *
 * You are given two strings `s` and `t` consisting of lowercase English letters.
 *
 * You may choose at most one index in `s` and replace the character at that index with any lowercase English letter.
 *
 * Return `true` if it is possible to make `s` a subsequence of `t`; otherwise, return `false`.
 *
 * Constraints:
 * - 1 <= s.length, t.length <= 10^5
 * - `s` and `t` consist only of lowercase English letters.
 */
typealias I3983 = (String, String) -> Boolean

class I3983canMakeSubsequence {

    @Nested
    inner class Solution : ProblemTest<I3983> {

        override val cases = testCases<I3983>(
            args("cat", "chat") expects true,
            args("plane", "apple") expects false,
            args("a", "ab") expects true,
            args("abc", "ab") expects false,       // s longer than t — impossible regardless
            args("abc", "abc") expects true,       // zero replacements needed ("at most one")
            args("z", "a") expects true,           // single char: always fixable if t non-empty
            args("xbc", "abc") expects true,       // replacement at the first index
            args("abx", "abc") expects true,       // replacement at the last index
            args("xyc", "abc") expects false,      // would need two replacements
            args("acb", "abc") expects false,      // order trap: right letters, wrong order
            args("aaab", "aaaa") expects true,     // all-equal t, mismatch at the end
            args("axbyc", "aqbzc") expects false,  // two mismatches spread apart, equal lengths
            args("b", "b") expects true,           // minimum size, already a subsequence
            args("aa", "a") expects false,         // s longer than t by exactly one
            args("axc", "ac") expects false,       // wildcard needs its own slot: prefix and suffix touch
            args("axc", "abbc") expects true,      // replacement in the middle, with a gap to spare
            args("ab", "ba") expects false,        // reversed pair: one fix can't repair the order
            args("xab", "abab") expects true,      // wildcard at the front shifts the rest of the match
            args("ba", "aab") expects true,        // replace s[0] to fit a run of equal chars in t
        )

        @Test
        fun test() = check(
            ::canMakeSubsequence_attemptOn2,
            ::referenceSolution,
        )

        /**
         * ## Analysis (lt-check) — verified: 3 official + 16 added cases
         *
         * **Approach.** Try every position `i` as the replaced one: rebuild `s` with a `'*'` wildcard at `i`,
         * then run the classic greedy subsequence scan over `t`, where `'*'` matches any character. Return
         * `true` on the first `i` whose template is fully consumed.
         *
         * ### Time complexity — O(n · (n + m))
         * - Outer loop: `n` candidate indices.
         * - Per index: `subList` + `plus` + `plus` + `ArrayDeque(...)` copy `n` chars → O(n); the scan of `t`
         *   is O(m). It always walks all of `t` (`forEach` can't break), even after the template is empty.
         * - Best case O(n + m) (fits with the wildcard at `i = 0`). Worst case, any `false` answer, is the
         *   full `n · (n + m)`. At the limits `n = m = 10^5` that's about 2·10^10 steps, well past the time
         *   limit. The added cases are small, so they don't show this.
         *
         * ### Space complexity — O(n) auxiliary
         * `str` plus one template at a time (several intermediate lists per iteration, all O(n) and
         * garbage-collected). No recursion.
         *
         * Optimal: O(n + m) time / O(1) space. Yours is correct but not optimal (an extra factor of `n`).
         *
         * ### Correctness notes
         * - For a *fixed* wildcard position, greedy leftmost matching is optimal by the usual exchange argument
         *   (LC 392). Matching a pattern char as early as possible never removes options for the rest.
         *   `'*'` also takes the earliest free slot, which leaves the most room for the suffix.
         * - "Zero replacements" is covered for free. A template with `'*'` accepts everything the original
         *   accepts, so if `s` already fits, the `i = 0` template fits too.
         * - `|s| > |t|` fails naturally, because a template longer than `t` can never be emptied.
         * - Relies on `'*'` never appearing in `t`. That's guaranteed, since `t` is lowercase letters only.
         *
         * ### Pattern
         * **Enumerate the single edit + re-verify.** It's the brute-force form of the "at most one
         * change/skip" family (LC 680 Valid Palindrome II, LC 161 One Edit Distance). The general
         * speed-up is to stop re-verifying from scratch, either by precomputing prefix/suffix facts once or
         * by carrying a "used the edit" flag through a single pass.
         *
         * ### Alternatives
         * - **Prefix/suffix greedy (see `referenceSolution`)**: O(n + m) time, O(n) space. `pre[i]` is the
         *   earliest end of `s[0..i)` and `suf[i+1]` the latest start of `s(i..n)`. Replacing `s[i]` works iff
         *   `pre[i] < suf[i+1]`. This removes the outer loop's per-index rescan.
         * - **Two-state greedy, O(m) time / O(1) space**: one pass over `t` tracking `a` (chars of `s` matched
         *   with no replacement) and `b` (max chars matched with the replacement used):
         *   ```
         *   for c in t: b = max(b + (b < n && s[b] == c), if (a < n) a + 1 else b); if (a < n && s[a] == c) a++
         *   answer: max(a, b) >= n
         *   ```
         *   Keeping only the *largest* matched count per state is safe because a longer matched prefix
         *   dominates a shorter one (the remaining suffix is a subsequence of the longer remainder). This is
         *   the same idea as a DP over (position in t, edits used) compressed to its best frontier.
         *
         * ### Parallelism
         * Your version does parallelize trivially: the `n` candidate indices are independent, so P cores
         * give close to a P× speedup. But that only divides a quadratic cost by a constant, and the linear
         * algorithm beats it by orders of magnitude. The linear scans themselves are sequential (each step
         * depends on the previous pointer). You could split `t` into chunks and compose "pointer in → pointer
         * out" transfer functions, but for 10^5 chars the thread overhead dominates.
         *
         * ### Real-world experience
         * "Subsequence with k errors" is approximate matching. It shows up in fuzzy file finders (fzf, IDE
         * "go to file") and in edit-tolerant search in spell checkers and bioinformatics read alignment
         * (k-mismatch matching). Those systems never enumerate edit positions. They carry an error budget in
         * the state (Bitap/Shift-Or, banded DP), which is the two-state greedy above generalized to k.
         */
        fun canMakeSubsequence_attemptOn2(s: String, t: String): Boolean {
            val str = s.toList()
            str.indices.forEach { index ->

                val beginning = str.subList(0, index)
                val ending = str.subList(index + 1, str.lastIndex + 1)

                val template = ArrayDeque(beginning.plus('*').plus(ending))
                t.forEach {
                    val letter = template.firstOrNull()
                    if (letter == '*' || letter == it) template.removeFirst()
                }
                if (template.isEmpty()) return true

            }
            return false
        }

        /**
         * ## Reference — prefix/suffix greedy matching, O(n + m)
         *
         * **Restatement.** Can `s` become a subsequence of `t` if we may overwrite at most one character of `s`
         * with any letter we like? Put another way, can all of `s` except possibly one position be embedded
         * in order into `t`, with some spare character of `t` left for the free position?
         *
         * **Pattern: prefix/suffix decomposition around one wildcard.** When an answer allows exactly one
         * "free" position, split the string there: the part on the left must fit as early as possible, the
         * part on the right as late as possible, and the wildcard needs just one spare slot between them.
         * Precompute both sides once and every candidate split becomes an O(1) check.
         *
         * ### Intuition
         * The replaced letter can be *anything*, so it never has to match a particular character. It only
         * needs **some** unused position of `t` between the prefix's match and the suffix's match. To leave
         * the most room, match `s[0..i)` greedily from the left (end as early as possible) and `s(i..n)`
         * greedily from the right (start as late as possible). If there's a gap of at least one character,
         * put the wildcard there. Once you see this, the 26-letter loop disappears.
         *
         * ### Core concepts
         * - **Greedy subsequence matching**: match each pattern char to the earliest possible text char.
         *   An exchange argument shows this never loses (LC 392). Here it gives the *tightest* prefix
         *   embedding.
         * - **Reverse greedy (latest start)**: the same greedy run from the right finds the *latest* index
         *   where a suffix can still begin. It is the mirror image and gives the tightest suffix embedding.
         * - **Prefix/suffix arrays**: precompute a per-position fact about `s[0..i)` and `s[i..n)` so that
         *   a question about every split point costs O(1) each instead of O(n + m) each.
         * - **Wildcard ⇒ gap test**: a position that may hold any symbol is satisfied by any single free slot,
         *   so the check is a comparison of indices (`pre[i] < suf[i + 1]`), not of characters.
         *
         * ### Approach
         * 1. `pre[i]` = number of `t` chars the greedy match of `s[0..i)` consumes, i.e. the first `t` index
         *    still free after it. Set it to `m + 1` (∞) if the prefix doesn't fit.
         * 2. `suf[i]` = the latest `t` index where the greedy right-to-left match of `s[i..n)` begins
         *    (`suf[n] = m`). Set it to `-1` (−∞) if the suffix doesn't fit.
         * 3. For each `i`, replacing `s[i]` works iff `pre[i] < suf[i + 1]`: then `t[pre[i]]` is a free slot
         *    strictly between the two embeddings. The sentinels make "doesn't fit" fail the comparison on
         *    its own.
         * 4. "Zero replacements" needs no separate check. If `s` already fits, then the split at
         *    `i = n - 1` passes too (the char can be replaced by itself).
         *
         * ### Complexity
         * - **Time O(n + m)**: each greedy pass moves one pointer monotonically over `t`, plus one sweep over `i`.
         * - **Space O(n)**: the two arrays of size `n + 1`.
         *
         * ### Common pitfalls
         * - **Trying all 26 letters.** It's unnecessary: the replacement can always copy whatever `t` has in the gap.
         * - **Using `<=` instead of `<`.** The wildcard needs its *own* `t` position. `pre[i] == suf[i + 1]` means
         *   the prefix ends exactly where the suffix starts, with nothing left over.
         * - **Matching the suffix from the left.** Then it starts as early as possible, which shrinks the gap
         *   and gives false negatives. The suffix has to be anchored as late as possible.
         * - **|s| > |t|.** That's impossible. The sentinels handle it without an explicit guard.
         * - **Forgetting the sentinel for a failed prefix/suffix.** Leaving the default `0` in `pre` (or a stale
         *   value in `suf`) after the match runs out would make an impossible split look valid.
         */
        fun referenceSolution(s: String, t: String): Boolean {
            val n = s.length
            val m = t.length

            // pre[i]: first free index of t after greedily matching s[0..i); m + 1 if it doesn't fit.
            val pre = IntArray(n + 1) { m + 1 }
            pre[0] = 0
            var j = 0
            for (i in 0 until n) {
                while (j < m && t[j] != s[i]) j++
                if (j == m) break
                pre[i + 1] = ++j
            }

            // suf[i]: latest start index in t of a match of s[i..n); -1 if it doesn't fit.
            val suf = IntArray(n + 1) { -1 }
            suf[n] = m
            j = m - 1
            for (i in n - 1 downTo 0) {
                while (j >= 0 && t[j] != s[i]) j--
                if (j < 0) break
                suf[i] = j--
            }

            // Wildcard at i fits iff a free slot of t lies strictly between the prefix and the suffix.
            return (0 until n).any { i -> pre[i] < suf[i + 1] }
        }

    }
}
