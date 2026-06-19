package leetcode.hash_map

import leetcode.ProblemTest
import leetcode.expectsAnyOrder
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0049 = (Array<String>) -> List<List<String>>

class I0049GroupAnagrams {

    @Nested
    inner class Solution : ProblemTest<I0049> {

        // Note: the `[""]` -> `[[""]]` case is omitted. The parser strips quotes before the
        // empty-row check, so `[[""]]` collapses to `[[]]` (empty inner list), which can't
        // represent a group holding a single empty string. (This case was already failing on
        // the old harness for the same reason.) See utils/todo.md.
        override val cases = testCases<I0049>(
            """["eat","tea","tan","ate","nat","bat"]""" expectsAnyOrder """[["bat"],["nat","tan"],["ate","eat","tea"]]""",
            """["a"]""" expectsAnyOrder """[["a"]]""",
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(strs: Array<String>): List<List<String>> {
            val index = strs.map { str ->
                val counter = mutableMapOf<Char, Int>()
                str.forEach { counter[it] = counter.getOrDefault(it, 0) + 1 }
                counter to str
            }

            val result = index.groupBy({ it.first }, { it.second })


            return result.values.toList()
        }


    }
}
