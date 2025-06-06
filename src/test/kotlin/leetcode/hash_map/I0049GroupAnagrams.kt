package leetcode.hash_map

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.array2arraySplit
import leetcode.utils.ArrayUtils.arraySplit
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias T = (Array<String>) -> List<List<String>>

class I0049GroupAnagrams {
    data class Case(
        val strs: List<String>,
        val output: List<List<String>>,
    )

    val prepareCase = { strs: String, output: String ->
        Case(strs.arraySplit(), output.array2arraySplit())
    }


    @Nested
    inner class Solution : AproblemTest<Case, T> {

        override val cases: List<Case> = listOf(
            prepareCase("""["eat","tea","tan","ate","nat","bat"]""", """[["bat"],["nat","tan"],["ate","eat","tea"]]"""),
            prepareCase("""[""]""", """[[""]]"""),
            prepareCase("""["a"]""", """[["a"]]"""),
        )
        override val solutions: List<Pair<String, T>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: T): Pair<Boolean, Any> {
            val result = solution(strs.toTypedArray())
            val sortedA = { a: List<List<String>> -> a.map { it.sorted() }.sortedBy { it.size } }
            val isCorrect = sortedA(result) == sortedA(output)
            return isCorrect to result
        }

        @Test
        fun test() = check()

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
