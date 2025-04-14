package leetcode.utils

data class TreeNode(var `val`: Int) {
    var left: TreeNode? = null
    var right: TreeNode? = null

    override fun toString(): String {
        val result = mutableListOf<Int?>()
        val queue = ArrayDeque<TreeNode?>()
        queue.add(this)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node == null) {
                result.add(null)
            } else {
                result.add(node.`val`)
                queue.add(node.left)
                queue.add(node.right)
            }
        }
        return result.dropLastWhile { it == null }.joinToString { it.toString() }
    }

}

fun TreeNode.deepCopy(): TreeNode {
    return TreeNode(`val`).also {
        it.left = left?.deepCopy()
        it.right = right?.deepCopy()
    }
}


@Suppress("unused")
fun printTree(root: TreeNode?, prefix: String = "") {
    if (root == null) {
        println("$prefix null")
        return
    }

    println("$prefix ${root.`val`}")
    printTree(root.left, prefix + "L-")
    printTree(root.right, prefix + "R-")
}

fun String.toTreeNode(): TreeNode? {
    val array = this.replace("[", "").replace("]", "").split(",").map { it.toIntOrNull() }
    if (array.mapNotNull { it }.isEmpty()) return null

    fun buildTree(list: List<Int?>, index: Int): TreeNode? {
        if (index > list.lastIndex) return null

        val value = list[index]
        val node = if (value != null) {
            TreeNode(value)
        } else {
            return null
        }
        node.left = buildTree(list, 2 * index + 1)
        node.right = buildTree(list, 2 * index + 2)

        return node
    }

    return buildTree(array, 0)
}
