import common.TreeNode
import common.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0104maximumDepthOfBinaryTree {

    data class Case(
        val input: TreeNode?,
        val output: Int,
    )

    fun prepareCase(s: String, out: Int) = Case(s.toTreeNode(), out)


    @Nested
    inner class Solution : AproblemTest<Case, (TreeNode?) -> Int> {
        override val cases: List<Case> = listOf(
            prepareCase("[3,9,20,null,null,15,7]", 3),
            prepareCase("[1,null,2]", 2),
        )
        override val solutions: List<Pair<String, (TreeNode?) -> Int>> = listOf(
            ::solution1.name to ::solution1,
        )

        override fun Case.check(solution: (TreeNode?) -> Int): Pair<Boolean, Any> {
            val result = solution(input)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(root: TreeNode?): Int {
            fun dfs(node: TreeNode?, lvl: Int): Int {
                if (node == null) return lvl
                return maxOf(
                    a = dfs(node.left, lvl + 1),
                    b = dfs(node.right, lvl + 1)
                )
            }
            return dfs(root, 0)
        }
    }
}