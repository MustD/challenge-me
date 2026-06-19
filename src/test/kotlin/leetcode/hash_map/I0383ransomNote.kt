package leetcode.hash_map

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0383 = (String, String) -> Boolean

class I0383ransomNote {

    @Nested
    inner class Solution : ProblemTest<I0383> {

        override val cases = testCases<I0383>(
            args("a", "b") expects false,
            args("aa", "ab") expects false,
            args("aa", "aab") expects true,
        )

        @Test
        fun test() = check(::solution1, ::solution2, ::solutionCommunity)

        private fun solution1(ransomNote: String, magazine: String): Boolean {
            if (ransomNote.length > magazine.length) return false
            val magazineMap = magazine.groupingBy { it }.eachCount().toMutableMap()
            for (c in ransomNote) {
                val count = magazineMap[c] ?: 0
                if (count <= 0) return false
                magazineMap[c] = count - 1
            }
            return true
        }

        private fun solution2(ransomNote: String, magazine: String): Boolean {
            val magazineMap: MutableMap<Char, Int> = mutableMapOf()
            for (c in magazine) {
                magazineMap[c] = magazineMap.getOrDefault(c, 0) + 1
            }

            for (c in ransomNote) {
                val count = magazineMap[c] ?: 0
                if (count <= 0) return false
                magazineMap[c] = count - 1
            }

            return true
        }

        private fun solutionCommunity(ransomNote: String, magazine: String): Boolean {
            val charCount = IntArray(26)

            for (char in magazine) {
                charCount[char - 'a']++
            }

            for (char in ransomNote) {
                if (charCount[char - 'a'] == 0) {
                    return false
                }
                charCount[char - 'a']--
            }

            return true
        }


    }
}
