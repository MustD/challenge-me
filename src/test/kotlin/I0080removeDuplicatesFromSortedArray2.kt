import common.ArrayUtils.toIntArray
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0080removeDuplicatesFromSortedArray2 {

    data class Case(
        val input: IntArray,
        val output: Int,
    )

    val prepareCase = { s: String, o: Int ->
        Case(s.toIntArray(), o)
    }

    @Nested
    inner class Solution : AproblemTest<Case, (IntArray) -> Int> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,1,1,2,2,3]", 5),
            prepareCase("[0,0,1,1,1,1,2,3,3]", 7),
            prepareCase("[1,1,1]", 2)
        )
        override val solutions: List<Pair<String, (IntArray) -> Int>> = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
        )

        override fun Case.check(solution: (IntArray) -> Int): Pair<Boolean, Any> {
            val result = solution(input.copyOf())
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(nums: IntArray): Int {
            var count = 0
            var i = 0
            while (i < nums.lastIndex) {
                if (nums[i] > nums[i + 1]) return i + 1
                if (count == 3) return i + 1

                if (count == 0) {
                    if (nums[i] == nums[i + 1]) count++
                } else {
                    if (nums[i] == nums[i + 1]) {
                        nums[i + 1] = -1
                        var j = i + 1
                        while (j <= nums.lastIndex - 1) {
                            val t = nums[j]
                            nums[j] = nums[j + 1]
                            nums[j + 1] = t
                            j++
                        }
                        continue
                    } else {
                        count = 0
                    }
                }
                i++
            }
            return nums.size
        }

        fun solution2(nums: IntArray): Int {
            if (nums.size <= 2) return nums.size

            var slow = 2
            for (fast in 2 until nums.size) {
                if (nums[slow - 2] != nums[fast]) nums[slow++] = nums[fast]
            }

            return slow
        }

    }
}