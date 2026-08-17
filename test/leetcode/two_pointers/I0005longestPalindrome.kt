package leetcode.two_pointers

import leetcode.ProblemTest
import leetcode.expects
import leetcode.expectsAnyOf
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 5. Longest Palindromic Substring  (https://leetcode.com/problems/longest-palindromic-substring/)
 *
 * Given a string `s`, return the longest substring of `s` that is a palindrome. A substring is a
 * contiguous, non-empty slice of the string; a palindrome reads the same forwards and backwards.
 * If several palindromic substrings tie for the longest length, returning any one of them is
 * accepted.
 *
 * Constraints:
 * - 1 <= s.length <= 1000
 * - `s` consists only of digits and English letters (both cases) — no spaces, so a single
 *   character is always a valid answer and the result is never empty.
 * - Ties are allowed: "babad" may return "bab" or "aba".
 */
typealias I0005 = (String) -> String

class I0005longestPalindrome {

    @Nested
    inner class Solution : ProblemTest<I0005> {

        // Several inputs have more than one longest palindrome of equal length, so those cases use
        // `expectsAnyOf(...)` — any listed candidate passes. Cases written with plain `expects`
        // have a single unique longest palindrome.
        override val cases = testCases<I0005>(
            "babad".expectsAnyOf("bab", "aba"),        // odd-length centers, tie
            "cbbd" expects "bb",                       // even-length center
            "a" expects "a",                           // shortest possible input
            "ac".expectsAnyOf("a", "c"),               // no palindrome longer than 1
            "bb" expects "bb",
            "abb" expects "bb",                        // answer not anchored at index 0
            "abba" expects "abba",                     // whole string, even length
            "racecar" expects "racecar",               // whole string, odd length
            "aaaaa" expects "aaaaa",                   // all identical chars
            "forgeeksskeegfor" expects "geeksskeeg",   // long even palindrome in the middle
            "abcde".expectsAnyOf("a", "b", "c", "d", "e"),
        )

        @Test
        fun test() = check(::longestPalindrome, ::referenceSolution, ::referenceSolutionManacher)

        /**
         * ## Verdict: **passes all 11 cases.**
         *
         * The `//not work` note above is stale — the second loop *is* the "no middle" (even-length)
         * case, so between the two passes this covers both parities. The name is also a misnomer:
         * O(n²) / O(1) is the standard interview-optimal answer here, not a fallback.
         *
         * ## Pattern: expand around center
         *
         * A palindrome is fully determined by its center, and there are only `2n - 1` centers
         * (`n` single chars + `n - 1` gaps between chars). Enumerate every center, push two pointers
         * outward while the characters mirror, and keep the longest. This inverts the brute force:
         * instead of picking a substring and *verifying* it, you *grow* one that is a palindrome by
         * construction, so every character comparison either extends the answer or ends a center.
         *
         * Loop 1 handles odd centers (`middle - shift` .. `middle + shift`, length `2*shift + 1`);
         * loop 2 handles even centers, where `lIdx = left - shift + 1` and `rIdx = left + shift`
         * expand around the gap after `left` (`shift = 1` gives the adjacent pair `left, left + 1`).
         *
         * ## Complexity
         *
         * - **Time — O(n²).** Both loops are `s.indices` × `shift in 1..s.length`, so 2n centers ×
         *   up to n expansion steps. The `break` on a character mismatch or an out-of-range
         *   `getOrNull` is what keeps typical input far below the bound; the worst case is a string
         *   of identical characters (`"aaaaa"`, case 9) where no center ever breaks early. The
         *   `s.substring(...)` inside the loop is itself O(n), but it only runs when the answer
         *   improves, and `result.length` never decreases, so those copies total O(n²) as well —
         *   they do not add a factor.
         * - **Space — O(1) auxiliary.** Only `result` plus a handful of `Int`s; iterative, so no
         *   recursion stack. `result` is output space (O(n)), not working space. Copying eagerly
         *   does churn the allocator — tracking `bestStart`/`bestLen` as ints and calling
         *   `substring` once after both loops would make the space claim strictly true.
         *
         * ## Correctness notes
         *
         * - **Seeding `result = s.substring(0..0)` is load-bearing.** Both loops start at
         *   `shift = 1`, so a lone character is never *produced* by the loops — the seed is the only
         *   thing that makes `"a"`, `"ac"` and `"abcde"` (cases 3, 4, 11) return a length-1 answer
         *   instead of `""`. It also fixes the tie-break to the leftmost character, which is why
         *   those cases needed `expectsAnyOf`.
         * - **`getOrNull(...) ?: break` is the right bounds guard.** Expansion is the classic
         *   off-by-one trap in this problem; returning `null` at the edge and breaking removes the
         *   whole class of index errors (contrast with `longestPalindrome` below, which indexes
         *   raw and walks off the front of the string).
         * - **The even-loop threshold is off by one, but benignly so.** A match at `shift` produces
         *   a substring of length `2*shift`, yet the guard is `result.length < (shift * 2) + 1`,
         *   i.e. it also fires when the new palindrome merely *ties* the current best. It can never
         *   shorten `result` (new length `2*shift` ≥ `result.length`), so the output stays correct —
         *   the only effect is redundant copies and a bias toward the last-found even palindrome on
         *   a tie. `result.length < shift * 2` is what was meant. The odd loop's `< (shift * 2) + 1`
         *   is exact, since there the new length really is `2*shift + 1`.
         * - **`isBlank()` vs `isEmpty()`.** `" "` would return `""`, though `" "` is a palindrome.
         *   Harmless under the alphanumeric constraint, but `isEmpty()` is the honest guard — and
         *   with `1 <= s.length` guaranteed, neither is reachable.
         * - The two loops are the same expansion with different starting offsets; folding them into
         *   one `expand(l, r)` helper called as `expand(i, i)` and `expand(i, i + 1)` would halve
         *   the code without changing the complexity.
         *
         * ## Alternatives
         *
         * - **Brute force — O(n³) / O(1):** every `(l, r)` pair, verify each in O(n). What
         *   `longestPalindrome` below is reaching for. Strictly dominated.
         * - **DP table — O(n²) / O(n²):** `dp[i][j] = s[i] == s[j] && dp[i+1][j-1]`, filled by
         *   increasing length. Same time as this solution but n² space, so it is a *worse* trade,
         *   not a better one. Worth writing once because the recurrence generalizes (palindrome
         *   partitioning, LC 132/647), but never the answer to reach for on this problem.
         * - **Manacher's algorithm — O(n) / O(n):** the only asymptotic improvement. Transform to
         *   `^#a#b#a#$` so every palindrome is odd-length, then reuse the mirror radius already
         *   computed inside the current rightmost palindrome to skip re-comparisons the way
         *   KMP reuses its failure function. Each character is scanned O(1) amortized times. So
         *   this solution is **not** optimal — but at n ≤ 1000 the O(n²) version is ~10⁶ char
         *   comparisons (sub-millisecond) and is what interviewers actually expect; Manacher is
         *   notoriously easy to get subtly wrong under time pressure.
         *
         * ## Parallelism
         *
         * This is the rare LeetCode problem that genuinely parallelizes: the `2n - 1` centers are
         * **completely independent**, so it is a textbook map/reduce — shard the centers across
         * threads, each keeps a local best `(start, len)`, then a max-reduce at the end. No locks,
         * no shared mutable state (the input string is read-only), no data dependencies.
         *
         * Two honest caveats:
         * - **Not worth it at n ≤ 1000.** Total work is under a millisecond; thread pool dispatch
         *   and the final reduction would cost more than the whole computation. Amdahl's law is not
         *   even the binding constraint — fixed overhead is.
         * - **Load imbalance at scale.** Expansion cost per center varies from O(1) to O(n), and
         *   the expensive centers cluster inside long palindromic regions. Static striping skews
         *   badly; you want work-stealing or dynamic chunking.
         *
         * The interesting contrast: the **O(n²) algorithm parallelizes trivially, the O(n) one does
         * not**. Manacher's radius array is inherently sequential — each entry is seeded from an
         * earlier one — so a 16-thread expand-around-center can beat single-threaded Manacher on
         * short-palindrome inputs while losing badly on adversarial ones. Classic case of a worse
         * asymptotic algorithm winning on real hardware for the wrong-looking reason.
         * SIMD applies too: compare 16/32-byte blocks of the forward and reversed spans at once,
         * which cuts the constant on each expansion without changing the O(n²).
         *
         * ## Real-world
         *
         * The direct application is bioinformatics: DNA restriction sites and RNA hairpin loops are
         * *reverse-complement* palindromes (A↔T, C↔G rather than character identity), which is the
         * same expansion with a different match predicate. There the constraints invert everything
         * — inputs are megabases not 1000 chars, so O(n²) is fatal and you use Manacher, a suffix
         * automaton, or an Eertree (palindromic tree, O(n) and incremental for streaming input).
         * You would also reach for an existing library (Biopython, EMBOSS `palindrome`) rather than
         * hand-rolling.
         *
         * More broadly, the *pattern* transfers further than the problem does: "grow a validated
         * region outward from a seed" is how two-pointer palindrome checks (LC 125/680), longest
         * common substring via diagonal expansion, and diff/compression match-extension all work.
         * And the practical note that matters most in production: at these sizes cache behavior
         * beats asymptotics — expand-around-center walks memory in two tight sequential streams and
         * is very cache-friendly, whereas the DP table's O(n²) random-ish access misses constantly.
         * The "worse" algorithm frequently wins the benchmark.
         */
        fun longestPalindrome(s: String): String {
            if (s.isBlank()) return ""
            var result = s.substring(0..0)

            for (middle in s.indices) {
                for (shift in (1..s.length)) {
                    val leftChar = s.getOrNull(middle - shift) ?: break
                    val rightChar = s.getOrNull(middle + shift) ?: break
                    if (leftChar != rightChar) break
                    if (result.length < (shift * 2) + 1) result = s.substring(middle - shift..middle + shift)
                }
            }

            for (left in s.indices) {
                for (shift in (1..s.length)) {
                    val lIdx = left - shift + 1
                    val rIdx = left + shift
                    val leftChar = s.getOrNull(lIdx) ?: break
                    val rightChar = s.getOrNull(rIdx) ?: break
                    if (leftChar != rightChar) break
                    if (result.length < (shift * 2) + 1) result = s.substring(lIdx..rIdx)
                }
            }

            return result
        }

        /**
         * # Reference 1 — expand around center, folded into one helper
         *
         * ## Restatement
         *
         * Find the longest contiguous slice of `s` that reads the same both ways. Ties may be
         * broken arbitrarily. `1 <= s.length <= 1000`, alphanumeric only, so the answer is never
         * empty — a single character is always a palindrome.
         *
         * ## Pattern: expand around center
         *
         * The key reframing: *don't enumerate substrings, enumerate centers.* There are O(n²)
         * substrings but only `2n - 1` centers — `n` characters (odd-length palindromes) and
         * `n - 1` gaps between adjacent characters (even-length ones). From a center you grow
         * outward while the mirrored characters match; whatever you have at any moment is a
         * palindrome *by construction*, so there is nothing to verify. That is what turns the
         * O(n³) brute force into O(n²): each character comparison either extends the answer or
         * kills the center for good.
         *
         * The odd/even split is the thing people trip on. `"aba"` has a middle character; `"abba"`
         * does not. Handling both is not two algorithms — it is the same expansion started from
         * `(i, i)` versus `(i, i + 1)`, which is why the two duplicated loops in
         * `longestPalindromeNotOptimal` collapse into two calls to one `expand` helper here.
         *
         * ## Approach
         *
         * 1. Track the best answer as `(bestStart, bestLen)` — integers, not a copied `String`.
         * 2. `expand(l, r)`: walk `l--` / `r++` while both are in range and `s[l] == s[r]`.
         * 3. On exit, `l` and `r` are each one step *past* the palindrome, so its length is
         *    `r - l - 1` and it starts at `l + 1`. Getting this off-by-one right is the whole
         *    trick; deriving it once beats guessing.
         * 4. For every `i`, call `expand(i, i)` and `expand(i, i + 1)`. Slice once at the end.
         *
         * Note `expand(i, i + 1)` needs no special-casing at `i == s.length - 1`: `r` is out of
         * range, the `while` guard fails immediately, and `len = r - l - 1 = 0`.
         *
         * ## Complexity
         *
         * - **Time — O(n²).** `2n - 1` centers × up to O(n) expansion steps each. Worst case is a
         *   run of identical characters (`"aaaaa"`), where no center ever breaks early.
         * - **Space — O(1) auxiliary.** Four `Int`s; the single `substring` at the end is output.
         *
         * ## Pitfalls
         *
         * - Forgetting even-length centers — the classic wrong answer, `"cbbd"` returns `"c"`.
         * - The `r - l - 1` off-by-one, because the loop always overshoots by one on both sides.
         * - Seeding `bestLen = 0` and returning `""` on a no-match input; `1` (or letting the
         *   first `expand(0, 0)` set it) is correct since a single char always qualifies.
         * - Copying substrings inside the loop instead of tracking indices — correct, just wasteful.
         */
        fun referenceSolution(s: String): String {
            if (s.isEmpty()) return ""

            var bestStart = 0
            var bestLen = 1

            fun expand(left: Int, right: Int) {
                var l = left
                var r = right
                while (l >= 0 && r < s.length && s[l] == s[r]) {
                    l--
                    r++
                }
                // `l` and `r` are one past the palindrome on each side.
                val len = r - l - 1
                if (len > bestLen) {
                    bestStart = l + 1
                    bestLen = len
                }
            }

            for (i in s.indices) {
                expand(i, i)      // odd-length center at i
                expand(i, i + 1)  // even-length center in the gap after i
            }

            return s.substring(bestStart, bestStart + bestLen)
        }

        /**
         * # Reference 2 — Manacher's algorithm, O(n)
         *
         * The only asymptotic improvement over expand-around-center. Not expected in an interview
         * at `n <= 1000`, but it is the answer to "can this be done in linear time?" and it shows
         * off a reuse trick that recurs elsewhere (KMP's failure function, Z-algorithm).
         *
         * ## Trick 1: kill the odd/even split
         *
         * Interleave separators: `"aba"` becomes `"^#a#b#a#$"`. Now *every* palindrome in the
         * transformed string has odd length, because an even-length palindrome of the original is
         * centered on a `#`. The `^` and `$` sentinels differ from everything else and from each
         * other, so expansion always stops on its own — no bounds checks in the inner loop at all.
         *
         * ## Trick 2: reuse the mirror
         *
         * `p[i]` = radius of the longest palindrome centered at `i` in the transformed string.
         * Keep the palindrome with the furthest right edge seen so far, as `(center, right)`.
         *
         * If `i < right`, then `i` lies *inside* a known palindrome, so its mirror
         * `mirror = 2 * center - i` sits at the reflected position — and the region around `i` is a
         * mirror image of the region around `mirror`. Therefore `p[i]` is at least `p[mirror]`,
         * capped at `right - i` (past the known palindrome's edge the mirror tells us nothing).
         * That cap is the subtle part: without it you would claim characters you have never
         * compared. Then expand naively from that head start.
         *
         * The amortized argument: every naive expansion step strictly increases `right`, and
         * `right` only moves forward across the whole run, so total expansion work is O(n). Same
         * shape of argument as the two-pointer sliding window.
         *
         * ## Mapping back
         *
         * In the transformed string, `p[i]` *equals* the length of the palindrome in the original,
         * and it starts at `(i - p[i]) / 2`. Convenient, and worth verifying by hand once rather
         * than memorizing.
         *
         * ## Complexity
         *
         * - **Time — O(n).** Amortized, per the `right`-monotonicity argument above.
         * - **Space — O(n).** The `2n + 3` transformed string plus the parallel radius array. This
         *   is the trade: linear time costs linear space, whereas expand-around-center is O(1).
         *
         * ## Pitfalls
         *
         * - Using the same character for both sentinels (or reusing `#`) — expansion then runs off
         *   the ends.
         * - Dropping the `min(right - i, p[mirror])` cap, or comparing `i <= right` instead of
         *   `i < right`.
         * - Forgetting to update `(center, right)` only when `i + p[i] > right`.
         */
        fun referenceSolutionManacher(s: String): String {
            if (s.isEmpty()) return ""

            // "aba" -> "^#a#b#a#$" : every palindrome is now odd-length, sentinels stop expansion.
            val t = buildString(2 * s.length + 3) {
                append("^#")
                for (c in s) {
                    append(c)
                    append('#')
                }
                append('$')
            }

            val p = IntArray(t.length)   // p[i] = palindrome radius at i (== length in original s)
            var center = 0
            var right = 0

            for (i in 1 until t.length - 1) {
                val mirror = 2 * center - i
                if (i < right) p[i] = minOf(right - i, p[mirror])
                // Sentinels guarantee this terminates without index checks.
                while (t[i + p[i] + 1] == t[i - p[i] - 1]) p[i]++
                if (i + p[i] > right) {
                    center = i
                    right = i + p[i]
                }
            }

            var bestCenter = 0
            for (i in p.indices) if (p[i] > p[bestCenter]) bestCenter = i

            val len = p[bestCenter]
            val start = (bestCenter - len) / 2
            return s.substring(start, start + len)
        }


    }
}
