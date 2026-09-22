package leetcode.array_string

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 387. First Unique Character in a String  (https://leetcode.com/problems/first-unique-character-in-a-string/)
 *
 * Given a string s, find the first non-repeating character in it and return its index. If it does not exist,
 * return -1.
 *
 * Constraints:
 * - 1 <= s.length <= 10^5
 * - s consists of only lowercase English letters.
 */
typealias I0387 = (String) -> Int

class I0387firstUniqChar {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0387> {

        override val cases = leetcode.testCases<I0387>(
            "leetcode" expects 0,
            "loveleetcode" expects 2,
            "aabb" expects -1,
        )

        @Test
        fun test() = check(::firstUniqChar, ::referenceSolution)

        fun firstUniqChar(s: String): Int {
            val charCount = s.groupingBy { it }.eachCount()
            s.forEachIndexed { index, ch ->
                if (charCount[ch] == 1) return index
            }
            return -1
        }

        /**
         * ## Restatement
         * Return the index of the first character that occurs exactly once in `s`, or -1 if every character repeats.
         *
         * ## Pattern
         * **Frequency counting (hash map / fixed-size count array) with two passes.** "First X such that a property
         * of the whole string holds" cannot be decided in one left-to-right scan, because a character you have seen
         * once might still repeat later. So: pass 1 gathers global facts, pass 2 walks in original order and asks
         * the question.
         *
         * ### Intuition
         * Uniqueness is a *global* property (needs the full count), while "first" is an *ordering* property (needs
         * the original positions). Separate the two concerns: counts answer "is it unique?", the second scan in
         * index order answers "which one is first?". The first index whose count is 1 is the answer.
         *
         * ### Core concepts
         * - **Frequency table**: a map from element to occurrence count. Here the alphabet is only 26 lowercase
         *   letters, so an `IntArray(26)` replaces a hash map: O(1) indexing, no hashing, no boxing.
         * - **Two-pass scan**: first pass collects aggregate information, second pass uses it while preserving the
         *   original order. Recognise this whenever a decision depends on data that lies *ahead* of the cursor.
         * - **Bounded alphabet => O(1) space**: the table size depends on the alphabet, not on the input length.
         *   (For Unicode input you would switch to a `HashMap<Char, Int>`; space becomes O(k) distinct chars.)
         *
         * ## Approach
         * 1. Create `count = IntArray(26)`.
         * 2. For each char `c` in `s`, do `count[c - 'a']++`.
         * 3. Scan `s` again by index; return the first `i` with `count[s[i] - 'a'] == 1`.
         * 4. If none is found, return -1.
         *
         * ## Complexity
         * - Time: O(n) - two linear passes over the string.
         * - Space: O(1) - a fixed 26-slot array.
         *
         * ## Common pitfalls
         * - Returning the first char seen *once so far* in a single pass: it may repeat later.
         * - Returning the character instead of its **index**.
         * - Forgetting the -1 result when all characters repeat (e.g. `"aabb"`).
         * - Using `indexOf(c) == lastIndexOf(c)` per character: correct, but O(n * 26) worst case here and O(n^2)
         *   for a larger alphabet; fine as a sanity check, not as the intended solution.
         * - Iterating the count array for the first 1 instead of iterating the string: that gives the
         *   alphabetically first unique letter, not the positionally first one.
         */
        fun referenceSolution(s: String): Int {
            val count = IntArray(26)
            for (c in s) count[c - 'a']++
            for (i in s.indices) {
                if (count[s[i] - 'a'] == 1) return i
            }
            return -1
        }

    }
}
