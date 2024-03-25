import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * https://leetcode.com/problems/palindrome-number/
 */
class I009palindromeNumber {

    @Test
    fun test() {
        data class Case(
            val num: Int,
            val result: Boolean,
        )

        val solutions = listOf(
            ::solution1.name to ::solution1,
            ::solutionAi.name to ::solutionAi,
        )

        val cases = listOf(
            Case(121, true),
            Case(-121, false),
            Case(1122, false),
            Case(10, false),
        )

        cases.forEach { case ->
            val expected = case.result
            solutions.forEach { (name, fn) ->
                val actual = fn(case.num)
                assertEquals(expected, actual, "Unexpected case($case) solution($name) result")

            }
        }
    }

    private fun solution1(x: Int): Boolean {
        if(x < 0) return false
        if(x < 10) return true
        val string = x.toString()
        (0..string.length / 2).forEach {
            val iStart = it
            val iEnd = string.length - it - 1
            if (iStart == iEnd) return true
            if (string[iStart] != string[iEnd]) return false
        }
        return true
    }

    private fun solutionAi(x: Int): Boolean {
        if (x < 0) return false

        var original = x
        var reversed = 0

        while (original != 0) {
            val digit = original % 10
            reversed *= 10
            reversed += digit
            original /= 10
        }

        return x == reversed
    }
}