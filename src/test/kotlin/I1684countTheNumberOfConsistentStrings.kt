import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I1684countTheNumberOfConsistentStrings {

    data class Case(
        val allowed: String,
        val words: Array<String>,
        val output: Int,
    )

    val parseCase = { s: String, list: String, expected: Int ->
        val string2list = list.replace("[", "").replace("]", "").replace("\"", "").split(",")
        Case(s, string2list.toTypedArray(), expected)
    }

    @Nested
    inner class Solution : AproblemTest<Case, (String, Array<String>) -> Int> {
        override val cases: List<Case> = listOf(
//            parseCase("ab", """["ad","bd","aaab","baa","badab"]""", 2),
//            parseCase("abc", """["a","b","c","ab","ac","bc","abc"]""", 7),
//            parseCase("cad", """["cc","acd","b","ba","bac","bad","ac","d"]""", 4),
            parseCase(
                "fstqyienx", """["n","eeitfns","eqqqsfs","i","feniqis","lhoa","yqyitei","sqtn","kug","z","neqqis"]""", 8
            )
        )
        override val solutions: List<Pair<String, (String, Array<String>) -> Int>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: (String, Array<String>) -> Int): Pair<Boolean, Any> {
            val result = solution(allowed, words)
            return (result == output) to result
        }

        @Test
        fun test() = check()

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