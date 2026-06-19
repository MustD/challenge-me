package leetcode.db_multi

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

// LeetCode accepts ANY longest palindromic substring (e.g. "babad" -> "bab" or "aba"), so the
// original AproblemTest used `output.contains(result)` against a list of valid answers. The
// ProblemTest harness compares against a single expected value, so each expected below is pinned
// to the *deterministic* output `solution1` actually emits. If you add a solution that emits a
// different (still valid) palindrome of the same length, update the expected or special-case it.
typealias I0005 = (String) -> String

class I0005longestPalindromicSubstring {

    @Nested
    inner class Solution : ProblemTest<I0005> {

        override val cases = testCases<I0005>(
            "babad" expects "bab",
            "cbbd" expects "bb",
            "bb" expects "bb",
            "abibkejokjfi" expects "bib",
            "abb" expects "bb",
            longString expects "cxrxc",
        )

        @Test
        fun test() = check(::solution1)

        fun solution1(s: String): String {
            if (s.isEmpty()) return ""
            if (s.length == 1) return s

            val isP = { check: String ->
                if (check.isEmpty()) true
                else (0..check.length / 2).all { check[it] == check[check.length - 1 - it] }
            }
            if (isP(s)) return s

            var result = ""
            val checkSet = { it: String -> if ((result.length < it.length) and isP(it)) result = it }
            s.indices.forEach { i ->
                s.indices.forEach { j ->
                    val start = if (i - j < 0) 0 else i - j
                    val end = if (i + j > s.length) s.length else i + j
                    s.substring(start, i).let(checkSet)
                    s.substring(i, end).let(checkSet)
                    s.substring(start, end).let(checkSet)
                }
            }

            return result
        }

    }

    val longString = "rfvtmdqjppztlvotnstyqeildrnevqkcoiqndxxncftlhdychrutvzkcxjnduhssfia" +
            "tzisxioyuqmxqpdiouixfhyjlnfsjupwjztuyklrweuqmkuygndrqfhhcxrxcwdwcwg" +
            "sknxxmxiwqxjbbljnckdgofehoarikioabmisfuzraxcaryjzsjetrvvpavbhbajrsnvrf" +
            "jorjzpcjmkoekaipinfzhuaegaxzzvlwclbzhqzbtvxtgfhojqhcnokzqbedusoywsfsgbwxbvr" +
            "qgmzojdmhlmzwtcvvmhnytqqlkpwyesbztabhyfukhpbchhvtzoegykvbzrywjcntrmsyokklsn" +
            "zwkpjdcdcayfauuxcydiubnonpumcogiwqsqzagxhwbvkcxojfvewzqjdbbwbudxndyvubbumrk" +
            "tckrgxtbanatsfsxtckueqqtldfnxeznozegxnzntynlfavlmdfgpwgebylkromwrwxflgylbrtbyj" +
            "blvgrxlkuhwnjmzqkngdghjvrsgtppkgsmpxrsahxlakadliwxmnbztfadwoglocbvwvhgcmgnkdt" +
            "lbnqbfepbupfticejjxjoogutfvckdvztgjuklczwiimstpstbreffkvcmvofanuxndahhftbvsbf" +
            "goagwptvszptjatyoemevxnpqxboiycubeoqfenopwcbzbfnqtixtqrpzatq"
}
