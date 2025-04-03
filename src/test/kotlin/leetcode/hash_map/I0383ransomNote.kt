package leetcode.hash_map

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0383ransomNote {
    data class Case(
        val ransomNote: String,
        val magazine: String,
        val output: Boolean,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (String, String) -> Boolean> {

        override val cases: List<Case> = listOf(
            Case("a", "b", false),
            Case("aa", "ab", false),
            Case("aa", "aab", true),
        )
        override val solutions: List<Pair<String, (String, String) -> Boolean>> = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
            ::solutionCommunity.name to ::solutionCommunity,
        )

        override fun Case.check(solution: (String, String) -> Boolean): Pair<Boolean, Any> {
            val result = solution(ransomNote, magazine)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

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
