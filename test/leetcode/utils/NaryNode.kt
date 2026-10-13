package leetcode.utils

/**
 * N-ary tree node used by LeetCode 589/590/429/559 (the `Node` of the N-ary tree problems).
 *
 * Distinct from [Node] (the graph adjacency-list node).
 */
class NaryNode(var `val`: Int) {
    var children: MutableList<NaryNode> = mutableListOf()

    /**
     * Serializes to LeetCode's level-order form with `null` separating each group of children:
     * the root, then `null`, then the children of each dequeued node followed by `null`.
     * Trailing `null`s are trimmed, e.g. `[1,null,3,2,4,null,5,6]`.
     */
    override fun toString(): String {
        val tokens = mutableListOf<String>(`val`.toString(), "null")
        val queue = ArrayDeque<NaryNode>()
        queue.add(this)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            node.children.forEach {
                tokens.add(it.`val`.toString())
                queue.add(it)
            }
            tokens.add("null")
        }
        while (tokens.isNotEmpty() && tokens.last() == "null") tokens.removeAt(tokens.lastIndex)
        return tokens.joinToString(",", "[", "]")
    }
}

/**
 * Parses LeetCode's N-ary tree level-order serialization (`null` separates groups of children),
 * e.g. `"[1,null,3,2,4,null,5,6]"`. Returns `null` for an empty input.
 */
fun String.toNaryNode(): NaryNode? {
    val tokens = replace(Regex("\\s+"), "").removePrefix("[").removeSuffix("]")
        .split(",").filter { it.isNotEmpty() }
    if (tokens.isEmpty() || tokens[0] == "null") return null

    val root = NaryNode(tokens[0].toInt())
    val queue = ArrayDeque<NaryNode>()
    queue.add(root)
    var idx = 2 // tokens[1] is the null that closes the root's own group
    while (queue.isNotEmpty() && idx < tokens.size) {
        val parent = queue.removeFirst()
        while (idx < tokens.size && tokens[idx] != "null") {
            val child = NaryNode(tokens[idx++].toInt())
            parent.children.add(child)
            queue.add(child)
        }
        idx++ // skip the group separator
    }
    return root
}
