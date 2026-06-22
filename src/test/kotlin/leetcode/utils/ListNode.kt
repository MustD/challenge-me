package leetcode.utils

import leetcode.utils.ArrayUtils.array2arraySplit

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

fun String.toListNode(): ListNode? = this.replace("[", "").replace("]", "")
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() } // handle empty list "[]" -> null
    .map { it.toInt() }
    .reversed()
    .fold<Int, ListNode?>(null) { r, t ->
        ListNode(t).apply { next = r }
    }

/**
 * Parses a LeetCode "array of linked lists" literal such as `"[[1,4,5],[1,3,4],[2,6]]"` into an
 * `Array<ListNode?>`, one head per inner list. An empty inner list (`[]`) becomes a `null` head.
 * The outer `"[]"` yields an empty array (zero lists). Reuses [array2arraySplit] so the
 * empty-inner-list and structural-whitespace edge cases are handled consistently.
 */
fun String.toListNodeArray(): Array<ListNode?> = array2arraySplit()
    .map { row -> row.foldRight<String, ListNode?>(null) { v, r -> ListNode(v.trim().toInt(), r) } }
    .toTypedArray()
