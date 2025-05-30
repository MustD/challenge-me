package leetcode.sliding_window

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0030 = (String, Array<String>) -> List<Int>

class I0030SubstringWithConcatenationOfAllWords {
    data class Case(
        val s: String,
        val words: List<String>,
        val output: List<Int>,
    )


    @Nested
    inner class Solution : AproblemTest<Case, I0030> {

        override val cases: List<Case> = listOf(
            Case("barfoothefoobarman", listOf("foo", "bar"), listOf(0, 9)),
            Case("wordgoodgoodgoodbestword", listOf("word", "good", "best", "word"), listOf()),
            Case("barfoofoobarthefoobarman", listOf("bar", "foo", "the"), listOf(6, 9, 12)),
            Case( "lingmindraboofooowingdingbarrwingmonkeypoundcake", listOf("fooo", "barr", "wing", "ding", "wing"), listOf(13) ),
            Case("wordgoodgoodgoodbestword", listOf("word", "good", "best", "good"), listOf(8)),
            Case("aaaaaaaaaaaaaa", listOf("aa", "aa"), listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
        )

        override val solutions: List<Pair<String, I0030>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0030): Pair<Boolean, Any> {
            val result = solution(s, words.toTypedArray())
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

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
                    print("$i of ${s.length}, ")
                    print(map)
                    println(" word: ${getWordAt(i)}")
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
                        println("valid at $i, map: $map")
                        result.add(i - (wSize * (wCount - 1)))
                    }
                }
            }
            println("------------- ${result.distinct().sorted()}")
            return result.distinct().sorted()
        }


    }
}
