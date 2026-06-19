package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0112 = (TreeNode?, Int) -> Boolean

class I0112pathSum {

    @Nested
    inner class Solution : ProblemTest<I0112> {
        override val cases = testCases<I0112>(
            args("[5,4,8,11,null,13,4,7,2,null,null,null,1]", 22) expects true,
            args("[1,2,3]", 5) expects false,
            args("[]", 0) expects false,
        )

        @Test
        fun test() = check(::solution1, ::solution2)

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
