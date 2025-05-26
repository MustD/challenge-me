package leetcode.array_string

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0028 = (String, String) -> Int

class I0028findTheIndexOfTheFirstOccurrenceInAString {

    data class Case(
        val haystack: String,
        val needle: String,
        val output: Int,
    )


    @Nested
    inner class Solution : AproblemTest<Case, I0028> {
        override val cases: List<Case> = listOf(
            Case("sadbutsad", "sad", 0),
            Case("leetcode", "leeto", -1),
        )
        override val solutions: List<Pair<String, I0028>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionEditorial.name to ::solutionEditorial
        )

        override fun Case.check(solution: I0028): Pair<Boolean, Any> {
            val result = solution(haystack, needle)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(haystack: String, needle: String): Int {
            return haystack.indexOf(needle)
        }

        fun solutionEditorial(haystack: String, needle: String): Int {
            for (i in 0..haystack.lastIndex - needle.lastIndex) {
                for (j in 0..needle.lastIndex) {
                    val match = haystack[i + j] == needle[j]
                    if (!match) break
                    if (j == needle.lastIndex) return i
                }
            }
            return -1
        }


    }
}
