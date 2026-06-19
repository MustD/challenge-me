package leetcode.sliding_window

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0030 = (String, Array<String>) -> List<Int>

class I0030SubstringWithConcatenationOfAllWords {

    @Nested
    inner class Solution : ProblemTest<I0030> {

        override val cases = testCases<I0030>(
            args("barfoothefoobarman", """["foo","bar"]""") expects "[0,9]",
            args("wordgoodgoodgoodbestword", """["word","good","best","word"]""") expects "[]",
            args("barfoofoobarthefoobarman", """["bar","foo","the"]""") expects "[6,9,12]",
            args(
                "lingmindraboofooowingdingbarrwingmonkeypoundcake",
                """["fooo","barr","wing","ding","wing"]""",
            ) expects "[13]",
            args("wordgoodgoodgoodbestword", """["word","good","best","good"]""") expects "[8]",
            args("aaaaaaaaaaaaaa", """["aa","aa"]""") expects "[0,1,2,3,4,5,6,7,8,9,10]",
        )

        @Test
        fun test() = check(::solution1)

        private fun solution1(s: String, words: Array<String>): List<Int> {
            val result = mutableListOf<Int>()
            val map = mutableMapOf<String, Int>()

            fun clearM() {
                map.clear()
                words.forEach { word -> map[word] = map.getOrDefault(word, 0) + 1 }
            }

            fun addW(word: String) {
                map[word] = map.getOrElse(word) { throw Exception() } - 1
            }

            fun removeW(word: String) {
                map[word] = map.getOrElse(word) { throw Exception() } + 1
            }

            val wSize = words[0].length
            val wCount = words.size

            fun isValid() = map.all { (_, count) -> count == 0 }
            fun wordIn(word: String) = map.containsKey(word)
            fun getWordAt(i: Int): String = s.substring(i, i + wSize)
            fun getPrevWordAt(i: Int): String = s.substring(i - wSize, i)
            for (j in 0..wSize) {
                var windowSize = 0
                clearM()
                for (i in j..(s.length - wSize) step wSize) {
                    if (wordIn(getWordAt(i)).not()) {
                        windowSize = 0
                        clearM()
                        continue
                    }
                    addW(getWordAt(i))
                    windowSize++

                    if (windowSize > wCount) {
                        removeW(getPrevWordAt(i - (wSize * (wCount - 1))))
                        windowSize--
                    }

                    if (windowSize == wCount && isValid()) {
                        result.add(i - (wSize * (wCount - 1)))
                    }
                }
            }
            return result.distinct().sorted()
        }


    }
}
