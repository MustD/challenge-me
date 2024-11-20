import common.ArrayUtils.arraySplit
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0017letterCombinationsOfAPhoneNumber {

    data class Case(
        val digits: String,
        val output: List<String>,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (String) -> List<String>> {

        fun prepareCase(digits: String, output: String): Case {
            return Case(digits, output.arraySplit())
        }

        override val cases: List<Case> = listOf(
            prepareCase("23", """["ad","ae","af","bd","be","bf","cd","ce","cf"]"""),
        )
        override val solutions: List<Pair<String, (String) -> List<String>>> = listOf(
            "solution1" to ::solution1,
            "community" to ::community,
        )

        override fun Case.check(solution: (String) -> List<String>): Pair<Boolean, Any> {
            val result = solution(digits)
            return (result.sorted() == output.sorted()) to result
        }

        @Test
        fun test() = check()

        fun solution1(digits: String): List<String> {
            if (digits.isEmpty()) return emptyList()

            val phoneMap = mapOf(
                '2' to "abc", '3' to "def", '4' to "ghi",
                '5' to "jkl", '6' to "mno", '7' to "pqrs",
                '8' to "tuv", '9' to "wxyz"
            )

            fun Map<Char, String>.charAt(char: Char, idx: Int) = this.getValue(char)[idx]
            fun Map<Char, String>.lastIndexAt(char: Char) = this.getValue(char).lastIndex

            fun backtrack(
                result: MutableList<String>,
                charIdx: Int,
                temp: MutableList<Char> = mutableListOf<Char>(),
                digits: List<Char>
            ) {
                if (temp.size == digits.size) {
                    result.add(temp.joinToString(""))
                    return
                }

                (0..phoneMap.lastIndexAt(digits[charIdx])).forEach { i ->
                    temp.add(phoneMap.charAt(digits[charIdx], i))
                    backtrack(result, charIdx + 1, temp, digits)
                    temp.removeAt(temp.lastIndex)
                }

            }

            val result = mutableListOf<String>()
            backtrack(
                result = result,
                charIdx = 0,
                digits = digits.toList()
            )
            return result
        }

        fun community(digits: String): List<String> = digits.takeIf {
            it.isNotEmpty()
        }?.fold(listOf("")) { acc, c ->
            c.letters.flatMap { letter -> acc.map { it + letter } }
        } ?: emptyList()

        private val Char.letters
            get() = when (this) {
                '2' -> listOf('a', 'b', 'c')
                '3' -> listOf('d', 'e', 'f')
                '4' -> listOf('g', 'h', 'i')
                '5' -> listOf('j', 'k', 'l')
                '6' -> listOf('m', 'n', 'o')
                '7' -> listOf('p', 'q', 'r', 's')
                '8' -> listOf('t', 'u', 'v')
                '9' -> listOf('w', 'x', 'y', 'z')
                else -> listOf()
            }

    }
}