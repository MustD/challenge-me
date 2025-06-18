package leetcode.stack

import leetcode.AproblemTest
import leetcode.utils.ArrayUtils.arraySplit
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test

typealias I0150 = (Array<String>) -> Int

class I0150EvaluateReversePolishNotation {

    data class Case(
        val input: List<String>,
        val output: Int,
    )

    fun prepareCase(input: String, output: Int) = Case(input.arraySplit(), output)

    @Nested
    inner class Solution : AproblemTest<Case, I0150> {

        override val cases: List<Case> = listOf(
            prepareCase("""["2","1","+","3","*"]""", 9),
            prepareCase("""["4","13","5","/","+"]""", 6),
            prepareCase("""["10","6","9","3","+","-11","*","/","*","17","+","5","+"]""", 22),
            prepareCase("""["4","3","-"]""", 1)

        )
        override val solutions: List<Pair<String, I0150>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: I0150): Pair<Boolean, Any> {
            val result = solution(input.toTypedArray())
            val isCorrect = result == output
            return isCorrect to result
        }

        @Test
        fun test() = check()

        private fun solution1(tokens: Array<String>): Int {
            val stack = Stack<Int>()
            for (token in tokens) {
                when (token) {
                    "+" -> stack.add(stack.pop() + stack.pop())
                    "-" -> {
                        val a = stack.pop()
                        val b = stack.pop()
                        stack.add(b - a)
                    }

                    "*" -> stack.add(stack.pop() * stack.pop())
                    "/" -> {
                        val b = stack.pop()
                        val a = stack.pop()
                        stack.add(a / b)
                    }

                    else -> stack.add(token.toInt())
                }
            }
            return stack.single()
        }


    }
}
