package grokking_algorithms

import kotlin.test.Test

class C4Counter {

    private fun count(input: List<Int>): Int {

        tailrec fun rec(list: List<Int>, sum: Int = 0): Int {
            if (list.isEmpty()) return sum
            return rec(list.drop(1), sum + 1)
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
            val actual = count(list)
            val expected = list.size
            assert(actual == expected) { "sum of $list was $actual, but expected $expected" }
        }

    }
}
