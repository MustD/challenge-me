package leetcode.stack

import java.util.*

@Suppress("unused")
class I0155MinStack() {

    val stack = Stack<Pair<Int, Int>>()

    fun push(`val`: Int) {
        val (_, min) = (if (stack.isNotEmpty()) stack.peek() else 0 to `val`)
        stack.add(`val` to minOf(min, `val`))
    }

    fun pop() {
        stack.pop()
    }

    fun top(): Int {
        val (top, _) = stack.peek()
        return top
    }

    fun getMin(): Int {
        val (_, min) = stack.peek()
        return min
    }

}
