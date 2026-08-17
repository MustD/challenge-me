package leetcode.utils

/**
 * Quad-tree node used by LeetCode 427 (Construct Quad Tree).
 *
 * Distinct from [Node] (which is the graph adjacency-list node). A quad-tree node is either:
 * - a leaf (`isLeaf == true`): it carries a boolean [`val`] and has no children, or
 * - an internal node (`isLeaf == false`): its [`val`] is arbitrary (LeetCode emits `true`) and it has
 *   exactly four children — [topLeft], [topRight], [bottomLeft], [bottomRight].
 */
class QuadNode(var `val`: Boolean, var isLeaf: Boolean) {
    var topLeft: QuadNode? = null
    var topRight: QuadNode? = null
    var bottomLeft: QuadNode? = null
    var bottomRight: QuadNode? = null

    /**
     * Serializes to LeetCode's level-order form: each present node becomes `[isLeaf,val]`
     * (each rendered as `1`/`0`) and absent child slots become `null`. A leaf enqueues no children;
     * an internal node enqueues all four (topLeft, topRight, bottomLeft, bottomRight) in that order.
     *
     * Trailing `null`s are trimmed, matching LeetCode's printed output.
     */
    override fun toString(): String {
        val tokens = mutableListOf<String>()
        val queue = ArrayDeque<QuadNode?>()
        queue.add(this)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node == null) {
                tokens.add("null")
                continue
            }
            tokens.add("[${if (node.isLeaf) 1 else 0},${if (node.`val`) 1 else 0}]")
            if (!node.isLeaf) {
                queue.add(node.topLeft)
                queue.add(node.topRight)
                queue.add(node.bottomLeft)
                queue.add(node.bottomRight)
            }
        }
        while (tokens.isNotEmpty() && tokens.last() == "null") tokens.removeAt(tokens.lastIndex)
        return tokens.joinToString(",", "[", "]")
    }
}

/**
 * Parses LeetCode's level-order quad-tree serialization, e.g. `"[[0,1],[1,1],[1,0],[1,1],[1,1]]"`
 * where each entry is `[isLeaf,val]` (0/1) or `null`. Returns `null` for an empty/blank input.
 *
 * Reconstruction is BFS: dequeue an internal node and assign the next four queue entries as its
 * (topLeft, topRight, bottomLeft, bottomRight); leaves consume no further tokens.
 */
fun String.toQuadNode(): QuadNode? {
    val inner = trim().removePrefix("[").removeSuffix("]").trim()
    if (inner.isEmpty()) return null

    // Split top-level entries: either "null" or "[a,b]".
    val tokens = mutableListOf<String?>()
    var i = 0
    while (i < inner.length) {
        when {
            inner[i] == '[' -> {
                val close = inner.indexOf(']', i)
                tokens.add(inner.substring(i + 1, close))
                i = close + 1
            }

            inner.startsWith("null", i) -> {
                tokens.add(null)
                i += 4
            }

            else -> i++ // skip commas / whitespace
        }
    }

    if (tokens.isEmpty() || tokens[0] == null) return null

    fun nodeFrom(pair: String): QuadNode {
        val (leaf, value) = pair.split(",").map { it.trim().toInt() }
        return QuadNode(`val` = value == 1, isLeaf = leaf == 1)
    }

    val root = nodeFrom(tokens[0]!!)
    val queue = ArrayDeque<QuadNode>()
    if (!root.isLeaf) queue.add(root)
    var idx = 1
    while (queue.isNotEmpty() && idx < tokens.size) {
        val parent = queue.removeFirst()
        val children = arrayOfNulls<QuadNode>(4)
        for (c in 0 until 4) {
            if (idx >= tokens.size) break
            val tok = tokens[idx++]
            if (tok != null) {
                val child = nodeFrom(tok)
                children[c] = child
                if (!child.isLeaf) queue.add(child)
            }
        }
        parent.topLeft = children[0]
        parent.topRight = children[1]
        parent.bottomLeft = children[2]
        parent.bottomRight = children[3]
    }
    return root
}
