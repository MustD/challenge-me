package leetcode.hash_map

import leetcode.ProblemTest
import leetcode.expectsAnyOrder
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 49. Group Anagrams  (https://leetcode.com/problems/group-anagrams/)
 *
 * Given an array of strings `strs`, group together the strings that are anagrams
 * of each other. Return the groups in any order.
 *
 * Constraints:
 * - 1 <= strs.length <= 10^4
 * - `0 <= strs[i].length <= 100`
 * - `strs[i]` consists of lowercase English letters only
 */
typealias I0049 = (Array<String>) -> List<List<String>>

class I0049GroupAnagrams {

    @Nested
    inner class Solution : ProblemTest<I0049> {

        // Note: the `[""]` -> `[[""]]` case is omitted. The parser strips quotes before the
        // empty-row check, so `[[""]]` collapses to `[[]]` (empty inner list), which can't
        // represent a group holding a single empty string.
        override val cases = testCases<I0049>(
            """["eat","tea","tan","ate","nat","bat"]""" expectsAnyOrder """[["bat"],["nat","tan"],["ate","eat","tea"]]""",
            """["a"]""" expectsAnyOrder """[["a"]]""",
        )

        @Test
        fun test() = check(::solution1, ::referenceSolution, ::referenceSolutionSorted)

        private fun solution1(strs: Array<String>): List<List<String>> {
            val index = strs.map { str ->
                val counter = mutableMapOf<Char, Int>()
                str.forEach { counter[it] = counter.getOrDefault(it, 0) + 1 }
                counter to str
            }

            val result = index.groupBy({ it.first }, { it.second })


            return result.values.toList()
        }

        /**
         * Pattern: **canonical key + hash map bucketing** ("group by a normal form").
         *
         * Intuition. Anagram-ness is an equivalence relation: two words are anagrams
         * iff they have the exact same multiset of letters. Whenever you must group by
         * an equivalence relation, don't compare pairs (that's O(n²)) — find a
         * *canonical form* (a fingerprint) that is identical for every member of a
         * group and different across groups, then use it as a hash-map key. One pass,
         * and the map does all the grouping for you. The same trick powers
         * "group by shifted sequence", "group shapes of islands", "group by sorted
         * digits", etc.
         *
         * Two canonical forms work here:
         *   1. the sorted characters of the word — `"eat"`, `"tea"`, `"ate"` → `"aet"`;
         *   2. the 26-letter frequency signature — `"eat"` → `a1 e1 t1`.
         * Form (2) avoids the per-word sort, which matters when words are long.
         *
         * Approach (frequency signature, this function):
         *   - For each word, count letters into an `IntArray(26)`.
         *   - Serialize that count vector into a `String` key. **Use a separator**
         *     (`#`) between counts: without it `a11 b0…` and `a1 b1…` would collide.
         *   - `getOrPut(key) { mutableListOf() }.add(word)`.
         *   - Return `map.values.toList()` — order is irrelevant, hence
         *     `expectsAnyOrder` in the test cases.
         *
         * Complexity: let n = number of words, k = max word length, A = 26.
         *   - This version: O(n * (k + A)) time, O(n * k) space for the output/keys.
         *   - Sorted-key version: O(n * k log k) time, same space.
         *
         * Pitfalls:
         *   - Forgetting the delimiter in the serialized signature (count collision).
         *   - Using a *mutable* object as a map key and then mutating it — the hash
         *     is captured at insertion time, so the entry becomes unreachable. Safe
         *     here only because a fresh key is built per word.
         *   - Assuming a fixed output order; LeetCode accepts any order of groups and
         *     any order within a group.
         *   - Only lowercase a–z is guaranteed *by this problem*. `IntArray(26)` with
         *     `c - 'a'` would break (index out of bounds) on mixed case or Unicode —
         *     use a `HashMap<Char, Int>` or sorting if the alphabet is open.
         *   - Empty strings are legal input (`strs[i].length` may be 0); they all share
         *     the all-zero signature and form one group.
         *
         * On your `solution1`: it is correct and it's the same idea — a
         * `Map<Char, Int>` *is* a canonical form, and Kotlin/Java maps have structural
         * `equals`/`hashCode`, so `groupBy` buckets them properly. Two things to note:
         * (a) hashing a whole `HashMap` per word is far more expensive (allocation +
         * per-entry hashing) than one small `String`/`IntArray` key, so it's the
         * "right answer, heavy key" variant; (b) `counter[it] = counter.getOrDefault(it, 0) + 1`
         * can be tightened to `counter.merge(it, 1, Int::plus)`.
         */
        fun referenceSolution(strs: Array<String>): List<List<String>> {
            val groups = HashMap<String, MutableList<String>>()
            for (word in strs) {
                val counts = IntArray(26)
                for (c in word) counts[c - 'a']++
                val key = buildString {
                    for (count in counts) {
                        append('#')
                        append(count)
                    }
                }
                groups.getOrPut(key) { mutableListOf() }.add(word)
            }
            return groups.values.toList()
        }

        /**
         * Same pattern, canonical form #1: the sorted characters of the word.
         * Shorter to write and the one to reach for in an interview unless words are
         * long or the alphabet is small and fixed.
         *
         * Complexity: O(n * k log k) time, O(n * k) space.
         */
        fun referenceSolutionSorted(strs: Array<String>): List<List<String>> =
            strs.groupBy { it.toCharArray().sorted().joinToString("") }
                .values
                .toList()

    }
}
