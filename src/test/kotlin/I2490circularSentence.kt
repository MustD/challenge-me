import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I2490circularSentence {

    data class Case(
        val input: String,
        val output: Boolean,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (String) -> Boolean> {
        override val cases: List<Case> = listOf(
            Case("leetcode exercises sound delightful", true),
            Case("eetcode", true),
            Case("Leetcode is cool", false),
            Case("ab a", false),
        )
        override val solutions: List<Pair<String, (String) -> Boolean>> = listOf(
            ::solution1.name to ::solution1,
            ::solutionAi.name to ::solutionAi,
            ::solution2.name to ::solution2,
        )

        override fun Case.check(solution: (String) -> Boolean): Pair<Boolean, Any> {
            val result = solution(input)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(sentence: String): Boolean {
            if (sentence.first() != sentence.last()) return false
            val haveMismatch = sentence.split(" ").zipWithNext().any { (curr, next) -> curr.last() != next.first() }
            return haveMismatch.not()
        }

        fun solution2(sentence: String): Boolean {
            if (sentence.isEmpty()) return false
            if (sentence.first() != sentence.last()) return false
            for (i in 1 until sentence.lastIndex) {
                if (sentence[i] != ' ') continue
                if (sentence[i - 1] != sentence[i + 1]) return false
            }
            return true

        }

        fun solutionAi(sentence: String): Boolean {
            if (sentence.isEmpty()) return false
            if (sentence.first() != sentence.last()) return false
            val words = sentence.split(" ")
            for (i in 0 until words.size - 1) {
                if (words[i].last() != words[i + 1].first()) {
                    return false
                }
            }
            return true
        }

    }
}