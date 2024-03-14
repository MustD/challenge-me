import kotlin.test.Test
import kotlin.test.assertEquals

class TwoSum {

    @Test
    fun test() {
        run {
            val given = intArrayOf(2, 7, 11, 15)
            val givenTarget = 9
            val result = solution(given, givenTarget)
            val expected = intArrayOf(1, 2)
            assertEquals(expected.sort(), result.sort(), "Unexpected output")
        }
    }

    private fun solution(sums: IntArray, target: Int): IntArray {
        val list = sums.toList()

        return emptyList<Int>().toIntArray()
    }
}