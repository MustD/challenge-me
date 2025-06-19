package leetcode.stack

import leetcode.AproblemTest
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test


typealias I0244 = (String) -> Int

class I0244BasicCalculator {

    data class Case(
        val input: String,
        val output: Int,
    )


    @Nested
    inner class Solution : AproblemTest<Case, I0244> {

        override val cases: List<Case> = listOf(
            Case("1 + 1", 2),
            Case(" 2-1 + 2 ", 3),
            Case("(1+(4+5+2)-3)+(6+8)", 23),
            Case("123 + 123", 246),
        )

        override val solutions: List<Pair<String, I0244>> = listOf(
            ::solutionEditorial.name to ::solutionEditorial,
        )

        override fun Case.check(solution: I0244): Pair<Boolean, Any> {
            val result = solution(input)
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solutionEditorial(s: String): Int {
            val stack = Stack<Int?>()
            var result = 0 // For the ongoing result
            var operand = 0 //value before operation
            var sign = 1 // 1 means positive, -1 means negative

            for (ch in s) {
                when {
                    ch.isDigit() -> {
                        // Forming operand, since it could be more than one digit
                        operand = 10 * operand + (ch.code - '0'.code)
                    }

                    ch == '+' -> {
                        // Evaluate the expression to the left,
                        // with result, sign, operand

                        result += sign * operand

                        // Save the recently encountered '+' sign
                        sign = 1

                        // Reset operand
                        operand = 0
                    }

                    ch == '-' -> {
                        result += sign * operand
                        sign = -1
                        operand = 0
                    }

                    ch == '(' -> {
                        // Push the result and sign on to the stack, for later
                        // We push the result first, then sign

                        stack.push(result)
                        stack.push(sign)

                        // Reset operand and result, as if new evaluation begins for the new sub-expression
                        sign = 1
                        result = 0
                    }

                    ch == ')' -> {
                        // Evaluate the expression to the left
                        // with result, sign and operand

                        result += sign * operand

                        // ')' marks end of expression within a set of parenthesis
                        // Its result is multiplied with sign on top of stack
                        // as stack.pop() is the sign before the parenthesis
                        result *= stack.pop()!!

                        // Then add to the next operand on the top.
                        // as stack.pop() is the result calculated before this parenthesis
                        // (operand on stack) + (sign on stack * (result from parenthesis))
                        result += stack.pop()!!

                        // Reset the operand
                        operand = 0
                    }
                }
            }
            return result + (sign * operand)
        }


    }
}
