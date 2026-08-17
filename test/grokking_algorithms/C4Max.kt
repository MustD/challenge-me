package grokking_algorithms

import kotlin.test.Test

class C4Max {

    private fun max(input: List<Int>): Int {

        tailrec fun rec(list: List<Int>, max: Int = Int.MIN_VALUE): Int {
            if (list.isEmpty()) return max
            return rec(
                list.drop(1),
                if (list.first() > max) list.first() else max
            )
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
            val actual = max(list)
            val expected = list.max()
            assert(actual == expected) { "sum of $list was $actual, but expected $expected" }
        }

    }
}
