package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I2490 = (String) -> Boolean

class I2490circularSentence {

    @Nested
    inner class Solution : ProblemTest<I2490> {
        override val cases = testCases<I2490>(
            "leetcode exercises sound delightful" expects true,
            "eetcode" expects true,
            "Leetcode is cool" expects false,
            "ab a" expects false,
        )

        @Test
        fun test() = check(::solution1, ::solutionAi, ::solution2)

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
