package leetcode.backtracking

import leetcode.ProblemTest
import leetcode.args
import leetcode.expectsAnyOrder
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0077 = (Int, Int) -> List<List<Int>>

class I0077combinations {

    @Nested
    inner class Solution : ProblemTest<I0077> {

        override val cases = testCases<I0077>(
            args(4, 2) expectsAnyOrder "[[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]",
            args(1, 1) expectsAnyOrder "[[1]]",
        )

        @Test
        fun test() = check(::solution1, ::community, ::solution2)

        //too long
        fun solution1(n: Int, k: Int): List<List<Int>> {
            fun cartesianProduct(lists: List<List<Int>>): List<List<Int>> {
                if (lists.isEmpty()) return emptyList()
                return lists.fold(listOf(listOf())) { acc, list ->
                    acc.flatMap { accList ->
                        list.mapNotNull { element ->
                            if (accList.contains(element)) null else (accList + element).sorted()
                        }
                    }
                }
            }

            val all = (1..k).map { (1..n).toList() }
            return cartesianProduct(all).distinct()
        }

        fun community(n: Int, k: Int): List<List<Int>> {
            fun backtrack(result: MutableList<List<Int>>, temp: MutableList<Int>, start: Int, n: Int, k: Int) {
                if (temp.size == k) {
                    result.add(ArrayList(temp))
                    return
                }
                for (i in start..n) {
                    temp.add(i)
                    backtrack(result, temp, i + 1, n, k)
                    temp.removeAt(temp.size - 1)
                }
            }

            val result = mutableListOf<List<Int>>()
            backtrack(result, mutableListOf(), 1, n, k)
            return result
        }

        fun solution2(n: Int, k: Int): List<List<Int>> {
            fun backtrack(result: MutableList<List<Int>>, temp: MutableList<Int>, start: Int, n: Int, k: Int) {
                if (temp.size == k) {
                    result.add(temp.toList())
                    return
                }
                (start..n).forEach { i ->
                    temp.add(i)
                    backtrack(result, temp, i + 1, n, k)
                    temp.removeAt(temp.lastIndex)
                }

            }

            val result = mutableListOf<List<Int>>()
            backtrack(result, mutableListOf(), 1, n, k)
            return result
        }

    }
}
