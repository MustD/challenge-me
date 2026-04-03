package leetcode.binary_tree_general

import leetcode.AproblemTest
import leetcode.utils.TreeNode
import leetcode.utils.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0112pathSum {

    data class Case(
        val root: TreeNode?,
        val targetSum: Int,
        val output: Boolean,
    )

    fun prepareCase(s: String, t: Int, out: Boolean) = Case(s.toTreeNode(), t, out)


    @Nested
    inner class Solution : AproblemTest<Case, (TreeNode?, Int) -> Boolean> {
        override val cases: List<Case> = listOf(
            prepareCase("[5,4,8,11,null,13,4,7,2,null,null,null,1]", 22, true),
            prepareCase("[1,2,3]", 5, false),
            prepareCase("[]", 0, false),
        )
        override val solutions: List<Pair<String, (TreeNode?, Int) -> Boolean>> = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
        )

        override fun Case.check(solution: (TreeNode?, Int) -> Boolean): Pair<Boolean, Any> {
            val result = solution(root, targetSum)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun solution1(root: TreeNode?, targetSum: Int): Boolean {
            fun dfs(node: TreeNode?, sum: Int): Boolean {
                if (node == null) return false
                if (node.left == null && node.right == null && (sum + node.`val`) == targetSum) return true
                return dfs(node.left, sum + node.`val`) || dfs(node.right, sum + node.`val`)
            }
            return dfs(root, 0)
        }

        fun solution2(root: TreeNode?, targetSum: Int): Boolean {
            if (root == null) return false

            val stack = ArrayDeque<Pair<TreeNode, Int>>()
            stack.addLast(root to 0)

            while (stack.isNotEmpty()) {
                val (node, sum) = stack.removeLast()
                val currentSum = sum + node.`val`
                if (node.left == null && node.right == null && currentSum == targetSum) return true

                node.right?.let { stack.addLast(it to currentSum) }
                node.left?.let { stack.addLast(it to currentSum) }
            }
            return false
        }

        /*
        | Language       | Runtime | JIT?          | GC?      | Recursive DFS     | Iterative DFS      | Recommendation                  |
        |----------------|---------|---------------|----------|-------------------|--------------------|---------------------------------|
        | Kotlin/JVM     | JVM     | Aggressive    | Yes      | Faster            | Slower (boxing)    | Recursive                       |
        | Python         | CPython | None          | Yes      | Slower + limit    | Faster             | Iterative                       |
        | TypeScript     | V8      | Moderate      | Yes      | Similar           | Similar            | Either (iterative for safety)   |
        | Rust           | Native  | N/A (AOT)     | No       | Fast              | Fast               | Either (iterative for deep trees)|
         */
    }
}
