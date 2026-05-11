package leetcode.binary_tree_general


class I0117populateNextRightPointersInEachNode2 {
    class Node(var `val`: Int) {
        var left: Node? = null
        var right: Node? = null
        var next: Node? = null
    }

    fun connect(root: Node?): Node? {
        if (root == null) return null

        var leftmost = root

        while (leftmost != null) {
            //dummy to track next level
            val dummy = Node(0)
            var tail = dummy

            var curr = leftmost
            while (curr != null) {
                //connect left
                if (curr.left != null) {
                    tail.next = curr.left
                    tail = tail.next!!
                }
                //connect right
                if (curr.right != null) {
                    tail.next = curr.right
                    tail = tail.next!!
                }

                //move to next node in current level
                curr = curr.next
            }
            leftmost = dummy.next
        }

        return root
    }
}
