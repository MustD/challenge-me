import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

class I005longestPalindromicSubstring {
    data class Case(
        val input: String,
        val output: List<String>,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (String) -> String> {
        override val cases: List<Case> = listOf(
            Case("babad", listOf("bab", "aba")),
            Case("cbbd", listOf("bb")),
            Case("bb", listOf("bb")),
            Case("abibkejokjfi", listOf("bib")),
            Case("abb", listOf("bb")),
            Case(longString, listOf("cxrxc"))
        )
        override val solutions: List<Pair<String, (String) -> String>> = listOf(
            ::solution1.name to ::solution1
        )

        override fun Case.check(solution: (String) -> String): Pair<Boolean, Any> {
            val result = measureTimedValue {
                solution(input)
            }
            if (result.duration * 10 > 1.seconds) return false to result
            return output.contains(result.value) to result
        }

        @Test
        fun test() = check()

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

