package leetcode.array_string

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

private typealias I0068 = (Array<String>, Int) -> List<String>

class I0068TextJustification {

    @Nested
    inner class Solution : ProblemTest<I0068> {
        // Original harness ran only the last case (`.last().let { listOf(it) }`); preserved here.
        override val cases = testCases<I0068>(
            args(
                """["ask","not","what","your","country","can","do","for","you","ask","what","you","can","do","for","your","country"]""",
                16,
            ) expects """["ask   not   what","your country can","do  for  you ask","what  you can do","for your country"]""",
        )

        @Test
        fun test() = check(::solution1)

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

            return lines.map { it.joinToString("") }
        }


    }
}
