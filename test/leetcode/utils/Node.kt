package leetcode.utils

data class Node(var `val`: Int) {
    var neighbors: ArrayList<Node?> = ArrayList<Node?>()

    override fun toString(): String {
        val visited = mutableSetOf<Int>()
        val order = mutableListOf<Node>()
        val queue = ArrayDeque<Node>()
        queue.add(this)
        visited.add(this.`val`)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            order.add(node)
            node.neighbors.filterNotNull().forEach { neighbor ->
                if (neighbor.`val` !in visited) {
                    visited.add(neighbor.`val`)
                    queue.add(neighbor)
                }
            }
        }
        return order.joinToString(",", "[", "]") { node ->
            node.neighbors.filterNotNull().joinToString(",", "[", "]") { it.`val`.toString() }
        }
    }
}

fun Node.deepCopy(): Node {
    val visited = mutableMapOf<Int, Node>()
    fun copy(node: Node): Node {
        visited[node.`val`]?.let { return it }
        val newNode = Node(node.`val`)
        visited[node.`val`] = newNode
        newNode.neighbors = ArrayList(node.neighbors.map { it?.let { copy(it) } })
        return newNode
    }
    return copy(this)
}

fun String.toNode(): Node? {
    val trimmed = this.trim().removePrefix("[").removeSuffix("]")
    if (trimmed.isBlank()) return null

    val adjLists = mutableListOf<List<Int>>()
    var depth = 0
    var start = -1
    for (i in trimmed.indices) {
        when (trimmed[i]) {
            '[' -> {
                depth++; if (depth == 1) start = i + 1
            }

            ']' -> {
                depth--
                if (depth == 0) {
                    val inner = trimmed.substring(start, i).trim()
                    adjLists.add(if (inner.isEmpty()) emptyList() else inner.split(",").map { it.trim().toInt() })
                }
            }
        }
    }

    if (adjLists.isEmpty()) return null
    val nodes = adjLists.indices.map { Node(it + 1) }
    adjLists.forEachIndexed { i, neighbors ->
        nodes[i].neighbors = ArrayList(neighbors.map { nodes[it - 1] })
    }
    return nodes[0]
}
