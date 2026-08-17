package grokking_algorithms

import kotlin.test.Test

class C4Sum {

    private fun sum(input: List<Int>): Int {

        tailrec fun rec(list: List<Int>, sum: Int = 0): Int {
            if (list.isEmpty()) return sum
            return rec(list.drop(1), sum + list.first())
        }

        return rec(input)
    }

    @Test
    fun test() {
        val cases = listOf(
            listOf(1, 2, 3),
            listOf(1, 2, 3, 123, 13235, 6632, 2213, 1, 399, 356),
        )

        cases.forEach { list ->
            val actual = sum(list)
            val expected = list.sum()
            assert(actual == expected) { "sum of $list was $actual, but expected $expected" }
        }

    }
}
