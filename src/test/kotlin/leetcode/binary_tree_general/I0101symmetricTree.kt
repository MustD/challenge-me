package leetcode.binary_tree_general

import leetcode.AproblemTest
import leetcode.utils.TreeNode
import leetcode.utils.deepCopy
import leetcode.utils.toTreeNode
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0101 = (TreeNode?) -> Boolean

class I0101symmetricTree {

    data class Case(
        val input: TreeNode?,
        val output: Boolean,
    )

    fun prepareCase(i: String, o: Boolean) = Case(
        i.toTreeNode(),
        o,
    )


    @Nested
    inner class Solution : AproblemTest<Case, I0101> {
        override val cases: List<Case> = listOf(
            prepareCase("[1,2,2,3,4,4,3]", true),
            prepareCase("1,2,2,null,3,null,3]", false),
            prepareCase("[1,2,2,2,null,2]", false),
        )
        override val solutions: List<Pair<String, I0101>> = listOf(
            ::solution1.name to ::solution1,
            ::solution2.name to ::solution2,
            ::solutionCommunity.name to ::solutionCommunity,
        )

        override fun Case.check(solution: I0101): Pair<Boolean, Any> {
            val result = solution(input?.deepCopy())
            return (result == output) to result
        }

        @Test
        fun test() = check()

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
