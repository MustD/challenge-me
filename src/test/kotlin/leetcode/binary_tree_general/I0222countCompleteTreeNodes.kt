package leetcode.binary_tree_general

import leetcode.AproblemTest
import leetcode.utils.TreeNode
import leetcode.utils.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class I0222countCompleteTreeNodes {

    data class Case(
        val input: TreeNode?,
        val output: Int,
    )

    fun prepareCase(s: String, out: Int) = Case(s.toTreeNode(), out)


    @Nested
    inner class Solution : AproblemTest<Case, (TreeNode?) -> Int> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,2,3,4,5,6]", 6),
            prepareCase("[]", 0),
            prepareCase("[1]", 1),
            prepareCase("[1,2,3]", 3),
            prepareCase("[1,2,3,4]", 4)
        )
        override val solutions: List<Pair<String, (TreeNode?) -> Int>> = listOf(
            ::countNodes.name to ::countNodes,
            ::countNodesBFS.name to ::countNodesBFS,
            ::countNodesDFS.name to ::countNodesDFS,
            ::aiCountNodesCompleteTree.name to ::aiCountNodesCompleteTree,
        )

        override fun Case.check(solution: (TreeNode?) -> Int): Pair<Boolean, Any> {
            val result = solution(input)
            return (result == output) to result
        }

        @Test
        fun test() = check()

        fun countNodes(root: TreeNode?): Int {
            return if (root == null) 0 else 1 + countNodes(root.left) + countNodes(root.right)
        }

        fun countNodesBFS(root: TreeNode?): Int {
            if (root == null) return 0
            val acc = ArrayDeque<TreeNode?>()
            acc.add(root)
            var count = 0

            while (acc.isNotEmpty()) {
                val node = acc.removeFirst()
                count++
                node?.left?.let(acc::add)
                node?.right?.let(acc::add)
            }
            return count
        }

        fun countNodesDFS(root: TreeNode?): Int {
            fun dfs(node: TreeNode?, count: Int = 0): Int {
                if (node == null) return count
                return dfs(node.left, count) + dfs(node.right, count) + 1
            }
            return dfs(root)
        }

        fun aiCountNodesCompleteTree(root: TreeNode?): Int {
            if (root == null) return 0

            // In a complete tree, depth equals the number of left-only steps from root
            fun depth(node: TreeNode?): Int {
                if (node == null) return 0
                return 1 + depth(node.left)
            }

            val depth = depth(root) - 1 // 0-based last level index (root = level 0)

            // Check if the node at 1-indexed level-order position idx exists.
            // Binary encoding trick: idx in binary = 1 | path, where each bit after
            // the leading 1 encodes a turn: 0 = left, 1 = right.
            // Example: idx=5 → 101 → strip leading 1 → 01 → Left, Right
            fun exist(idx: Int): Boolean {
                // number of bits below the leading 1 = number of steps from root
                val level = Int.SIZE_BITS - Integer.numberOfLeadingZeros(idx) - 1
                var node: TreeNode? = root
                for (bit in level - 1 downTo 0) {
                    node = if (((idx shr bit) and 1) == 0) node?.left else node?.right
                }
                return node != null
            }

            // Last level nodes occupy 1-indexed positions [lastLevelStart .. 2*lastLevelStart-1].
            // Binary search for how many of them exist (they are always left-packed).
            // lo tracks the count of confirmed existing nodes (0-indexed offset from lastLevelStart).
            val lastLevelStart = 1 shl depth  // = 2^depth, first 1-indexed position on last level
            var lo = 0
            var hi = lastLevelStart - 1  // max possible nodes on last level = 2^depth
            while (lo <= hi) {
                val mid = (lo + hi) / 2
                if (exist(lastLevelStart + mid)) lo = mid + 1 else hi = mid - 1
            }

            // (lastLevelStart - 1) = nodes in all full levels above last + lo nodes on last level
            return lastLevelStart - 1 + lo
        }
    }
}
