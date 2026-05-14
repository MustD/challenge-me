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
    val array = this.replace("[", "").replace("]", "").split(",").map { it.trim().toIntOrNull() }
    if (array.mapNotNull { it }.isEmpty()) return null

    val root = array[0]?.let { TreeNode(it) } ?: return null
    val queue = ArrayDeque<TreeNode>()
    queue.add(root)
    var i = 1

    while (queue.isNotEmpty() && i <= array.lastIndex) {
        val node = queue.removeFirst()

        if (i <= array.lastIndex) {
            array[i++]?.let { node.left = TreeNode(it).also(queue::add) }
        }
        if (i <= array.lastIndex) {
            array[i++]?.let { node.right = TreeNode(it).also(queue::add) }
        }
    }

    return root
}
