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
            prepareCase(4, 2, "[[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]")
        )
        override val solutions: List<Pair<String, (Int, Int) -> List<List<Int>>>> = listOf(
            "solution1" to ::solution1,
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

//            val total = (n.toDouble().pow(k.toDouble()) - k).toInt() //6
            val all = (1..k).map { (1..n).toList() }
            return cartesianProduct(all).distinct()
        }
    }
}