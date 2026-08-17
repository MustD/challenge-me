package leetcode.stack

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test

typealias I0150 = (Array<String>) -> Int

class I0150EvaluateReversePolishNotation {

    @Nested
    inner class Solution : ProblemTest<I0150> {

        override val cases = testCases<I0150>(
            """["2","1","+","3","*"]""" expects 9,
            """["4","13","5","/","+"]""" expects 6,
            """["10","6","9","3","+","-11","*","/","*","17","+","5","+"]""" expects 22,
            """["4","3","-"]""" expects 1,
        )

        @Test
        fun test() = check(::solution1)

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
