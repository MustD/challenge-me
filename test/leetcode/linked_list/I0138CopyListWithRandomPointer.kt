package leetcode.linked_list


@Suppress("unused")
class I0138CopyListWithRandomPointer {
    class Node(var `val`: Int) {
        var next: Node? = null
        var random: Node? = null
    }

    class Solution {
        var visitedHash = HashMap<Node?, Node?>()

        fun copyRandomList(head: Node?): Node? {
            if (head == null) return null

            // If we have already processed the current node, then we simply return the cloned version of it.
            if (visitedHash.containsKey(head)) {
                return visitedHash.get(head)
            }

            // Create a new node with the value same as old node. (i.e. copy the node)
            val node = Node(head.`val`)

            // Save this value in the hash map. This is needed since there might be
            // loops during traversal due to randomness of random pointers and this would help us avoid them.
            this.visitedHash.put(head, node)

            // Recursively copy the remaining linked list starting once from the next pointer and then from
            // the random pointer.
            // Thus we have two independent recursive calls.
            // Finally we update the next and random pointers for the new node created.
            node.next = copyRandomList(head.next)
            node.random = copyRandomList(head.random)

            return node
        }
    }
}
