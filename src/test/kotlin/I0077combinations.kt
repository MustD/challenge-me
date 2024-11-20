import common.ArrayUtils.toListOfIntLists
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0077combinations {

    data class Case(
        val n: Int,
        val k: Int,
        val output: List<List<Int>>,
    )

    @Nested
    inner class Solution : AproblemTest<Case, (Int, Int) -> List<List<Int>>> {

        fun prepareCase(n: Int, k: Int, output: String): Case {
            return Case(n, k, output.toListOfIntLists())
        }

        override val cases: List<Case> = listOf(
            prepareCase(4, 2, "[[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]"),
            prepareCase(1, 1, "[[1]]"),
        )
        override val solutions: List<Pair<String, (Int, Int) -> List<List<Int>>>> = listOf(
            "solution1" to ::solution1,
            "community" to ::community,
            "solution2" to ::solution2,
        )

        override fun Case.check(solution: (Int, Int) -> List<List<Int>>): Pair<Boolean, Any> {
            val result = solution(n, k)
            return (result == output) to result
        }

        @Test
        fun test() = check()

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