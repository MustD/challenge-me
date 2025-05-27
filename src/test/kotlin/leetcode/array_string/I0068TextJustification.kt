package leetcode.array_string

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0068 = (Array<String>, Int) -> List<String>

class I0068TextJustification {

    data class Case(
        val words: List<String>,
        val maxWidth: Int,
        val output: List<String>,
    )

    @Nested
    inner class Solution : AproblemTest<Case, I0068> {
        override val cases: List<Case> = listOf(
            Case(
                listOf("This", "is", "an", "example", "of", "text", "justification."),
                16,
                listOf(
                    "This    is    an",
                    "example  of text",
                    "justification.  "
                ),
            ),
            Case(
                listOf("What", "must", "be", "acknowledgment", "shall", "be"),
                16,
                listOf(
                    "What   must   be",
                    "acknowledgment  ",
                    "shall be        "
                ),
            ),
            Case(
                listOf(
                    "Science", "is", "what", "we", "understand", "well", "enough",
                    "to", "explain", "to", "a", "computer.", "Art", "is", "everything",
                    "else", "we", "do"
                ),
                20,
                listOf(
                    "Science  is  what we",
                    "understand      well",
                    "enough to explain to",
                    "a  computer.  Art is",
                    "everything  else  we",
                    "do                  "
                ),
            ),
            Case(
                listOf(
                    "ask", "not", "what", "your",
                    "country", "can", "do", "for",
                    "you", "ask", "what", "you",
                    "can", "do", "for", "your",
                    "country"
                ),
                16,
                listOf(
                    "ask   not   what",
                    "your country can",
                    "do  for  you ask",
                    "what  you can do",
                    "for your country",
                )
            )
        ).last().let { listOf(it) }
        override val solutions: List<Pair<String, I0068>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0068): Pair<Boolean, Any> {
            val result = solution(words.toTypedArray(), maxWidth)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(words: Array<String>, maxWidth: Int): List<String> {
            val lines = mutableListOf<MutableList<String>>()

            var currentLine = mutableListOf<String>()
            var currentLineLength = 0
            for (wordIdx in words.indices) {
                val word = words[wordIdx]

                if (currentLineLength + word.length > maxWidth) {
                    lines.add(currentLine)
                    currentLine = mutableListOf()
                    currentLineLength = 0
                }
                currentLineLength += 1 + word.length
                currentLine.add(word)
            }
            lines.add(currentLine)

            for (lineIdx in lines.indices) {
                val line = lines[lineIdx]

                if (line.size == 1 || lineIdx == lines.lastIndex) {
                    var lastLineLength = 0
                    for (wordIdx in line.indices) {
                        if (wordIdx == line.lastIndex) {
                            lastLineLength += line[wordIdx].length
                            line[wordIdx] += " ".repeat(maxWidth - lastLineLength)

                        } else {
                            line[wordIdx] += " "
                            lastLineLength += line[wordIdx].length
                        }
                    }
                } else {
                    var spaceCount = maxWidth - line.sumOf { it.length }
                    var wordIdx = 0
                    while (spaceCount > 0) {
                        if (wordIdx == line.lastIndex) {
                            wordIdx = 0
                            continue
                        }
                        line[wordIdx] += " "
                        spaceCount--
                        wordIdx++
                    }
                }
            }

            lines.map {
                val str = it.joinToString("")
                println("$str : ${str.length}")
            }
            return lines.map { it.joinToString("") }
        }


    }
}
