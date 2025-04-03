package leetcode.utils

data class ListNode(
    var `val`: Int,
    var next: ListNode? = null,
) {
    override fun toString(): String {
        return "$`val`" + (next?.let { ", $it" } ?: "")
    }

    fun deepCopy(): ListNode {
        val copiedNode = ListNode(`val`)
        copiedNode.next = next?.deepCopy()
        return copiedNode
    }
}

fun String.toListNode() = this.replace("[", "").replace("]", "").split(",").map {
    it.toInt()
}.reversed().fold<Int, ListNode?>(null) { r, t ->
    ListNode(t).apply { next = r }
}
