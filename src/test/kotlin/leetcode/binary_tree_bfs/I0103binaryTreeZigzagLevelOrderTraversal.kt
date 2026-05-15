package leetcode.TODO_category

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0103 = (TreeNode?) -> List<List<Int>>

class I0103binaryTreeZigzagLevelOrderTraversal {

    @Nested
    inner class Solution : ProblemTest<I0103> {

        override val cases = testCases<I0103>(
            "[3,9,20,null,null,15,7]" expects listOf(
                listOf(3),
                listOf(20, 9),
                listOf(15, 7),
            ),
        )

        @Test
        fun test() = check(::zigzagLevelOrder)

        fun zigzagLevelOrder(root: TreeNode?): List<List<Int>> {
            if (root == null) return emptyList()
            val result = mutableListOf<List<Int>>()
            val queue = ArrayDeque<TreeNode>()
            queue.add(root)
            var left2right = true

            while (queue.isNotEmpty()) {
                val levelSize = queue.size
                val level = ArrayDeque<Int>()

                repeat(levelSize) {
                    val node = queue.removeFirst()
                    if (left2right) level.addLast(node.`val`)
                    else level.addFirst(node.`val`)

                    node.left?.let { queue.add(it) }
                    node.right?.let { queue.add(it) }
                }

                result.add(level.toList())
                left2right = left2right.not()
            }

            return result
        }

    }
}
