package leetcode.binary_tree_general

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import leetcode.utils.TreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0101 = (TreeNode?) -> Boolean

class I0101symmetricTree {

    @Nested
    inner class Solution : ProblemTest<I0101> {
        override val cases = testCases<I0101>(
            "[1,2,2,3,4,4,3]" expects true,
            "1,2,2,null,3,null,3]" expects false,
            "[1,2,2,2,null,2]" expects false,
        )

        @Test
        fun test() = check(::solution1, ::solution2, ::solutionCommunity)

        fun solution1(root: TreeNode?): Boolean {
            if (root == null) return true
            var lvlNodes = mutableListOf<TreeNode?>()
            lvlNodes.add(root.left)
            lvlNodes.add(root.right)

            while (lvlNodes.isNotEmpty()) {
                val last = lvlNodes.lastIndex

                for (i in 0..last / 2) {
                    val left = lvlNodes[i]?.`val`
                    val right = lvlNodes[last - i]?.`val`
                    if (left != right) return false
                }

                val newQueue = mutableListOf<TreeNode?>()
                for (i in 0..last) {
                    if (lvlNodes[i] == null) continue
                    val left = lvlNodes[i]?.left
                    val right = lvlNodes[i]?.right
                    newQueue.add(left)
                    newQueue.add(right)
                }
                lvlNodes = newQueue
            }
            return true
        }

        fun solution2(root: TreeNode?): Boolean {
            fun lRec(node: TreeNode?): String {
                if (node == null) return "N"
                return "${node.`val`}${lRec(node.left)}${lRec(node.right)}"
            }

            fun rRec(node: TreeNode?): String {
                if (node == null) return "N"
                return "${node.`val`}${rRec(node.right)}${rRec(node.left)}"
            }

            val l = lRec(root?.left)
            val r = rRec(root?.right)
            return l == r
        }

        fun solutionCommunity(root: TreeNode?): Boolean {
            if (root == null) return true

            fun isMirror(left: TreeNode?, right: TreeNode?): Boolean {
                if (left == null && right == null) return true
                if (left == null || right == null) return false

                return (left.`val` == right.`val`) &&
                        isMirror(left.left, right.right) &&
                        isMirror(left.right, right.left)
            }

            return isMirror(root.left, root.right)

        }

    }
}
