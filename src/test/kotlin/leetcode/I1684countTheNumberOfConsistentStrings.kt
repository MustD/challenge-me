package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I1684 = (String, Array<String>) -> Int

class I1684countTheNumberOfConsistentStrings {

    @Nested
    inner class Solution : ProblemTest<I1684> {
        override val cases = testCases<I1684>(
            args("ab", """["ad","bd","aaab","baa","badab"]""") expects 2,
            args("abc", """["a","b","c","ab","ac","bc","abc"]""") expects 7,
            args("cad", """["cc","acd","b","ba","bac","bad","ac","d"]""") expects 4,
            args(
                "fstqyienx", """["n","eeitfns","eqqqsfs","i","feniqis","lhoa","yqyitei","sqtn","kug","z","neqqis"]"""
            ) expects 8,
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(allowed: String, words: Array<String>): Int {
            val index = allowed.associateWith { 1 }
            var result = words.size
            words.forEach word@{
                it.forEach { char ->
                    if (index[char] == null) {
                        result--
                        return@word
                    }
                }
            }
            return result
        }


    }
}
