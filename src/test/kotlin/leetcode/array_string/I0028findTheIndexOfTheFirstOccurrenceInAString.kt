package leetcode.array_string

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0028 = (String, String) -> Int

class I0028findTheIndexOfTheFirstOccurrenceInAString {

    @Nested
    inner class Solution : ProblemTest<I0028> {
        override val cases = testCases<I0028>(
            args("sadbutsad", "sad") expects 0,
            args("leetcode", "leeto") expects -1,
        )

        @Test
        fun test() = check(::solution1, ::solutionEditorial)

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
