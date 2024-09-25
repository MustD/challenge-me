import common.ArrayUtils.toDoubleArray
import common.TreeNode
import common.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0637averageOfLevelsInBinaryTree {

    data class Case(
        val input: TreeNode?,
        val output: DoubleArray,
    )

    fun prepareCase(s: String, out: String) = Case(s.toTreeNode(), out.toDoubleArray())


    @Nested
    inner class Solution : AproblemTest<Case, (TreeNode?) -> DoubleArray> {
        override val cases: List<Case> = listOf(
            prepareCase("[3,9,20,null,null,15,7]", "[3.0,14.5,11.00000]"),
            prepareCase("[3,9,20,15,7]", "[3.0,14.5,11.0]"),
            prepareCase("[3,1,5,0,2,4,6]", "3.0,3.0,3.0]"),
        )
        override val solutions: List<Pair<String, (TreeNode?) -> DoubleArray>> = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
        )

        override fun Case.check(solution: (TreeNode?) -> DoubleArray): Pair<Boolean, Any> {
            val result = solution(input)
            return (result.toList() == output.toList()) to result.toList()
        }

        @Test
        fun test() = check()

        fun solution1(root: TreeNode?): DoubleArray {
            if (root == null) return doubleArrayOf()

            val queue: ArrayDeque<Pair<Int, TreeNode>> = ArrayDeque()
            queue.add(0 to root)
            val map = mutableMapOf<Int, MutableList<Double>>()

            while (queue.isNotEmpty()) {
                val (lvl, node) = queue.removeFirst()
                val current = map[lvl]
                if (current == null) {
                    map[lvl] = mutableListOf(node.`val`.toDouble())
                } else {
                    current.add(node.`val`.toDouble())
                }

                node.left?.let { queue.add(lvl + 1 to it) }
                node.right?.let { queue.add(lvl + 1 to it) }
            }

            return map.map { it.value.average() }.toDoubleArray()
        }

        fun solution2(root: TreeNode?): DoubleArray {
            val map = mutableMapOf<Int, MutableList<Double>>()
            fun dfs(node: TreeNode?, lvl: Int): Int {
                if (node == null) return lvl
                map.getOrPut(lvl) { mutableListOf() }.add(node.`val`.toDouble())

                return maxOf(
                    a = dfs(node.left, lvl + 1),
                    b = dfs(node.right, lvl + 1)
                )
            }

            dfs(root, 0)
            return map.map { it.value.average() }.toDoubleArray()
        }
    }
}