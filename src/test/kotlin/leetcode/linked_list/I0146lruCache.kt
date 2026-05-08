package leetcode.linked_list

import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals

class I0146lruCache {

    class LRUCache(private val capacity: Int) {

        private class Node(var key: Int, var value: Int) {
            var prev: Node? = null
            var next: Node? = null
        }

        private val map = HashMap<Int, Node>()
        private val head = Node(0, 0)
        private val tail = Node(0, 0)

        init {
            head.next = tail
            tail.prev = head
        }

        fun get(key: Int): Int {
            val node = map[key] ?: return -1
            moveToFront(node)
            return node.value
        }

        fun put(key: Int, value: Int) {
            val existing = map[key]
            if (existing != null) {
                existing.value = value
                moveToFront(existing)
                return
            }
            val node = Node(key, value)
            map[key] = node
            addToFront(node)
            if (map.size > capacity) {
                val lru = tail.prev!!
                unlink(lru)
                map.remove(lru.key)
            }
        }

        private fun addToFront(node: Node) {
            val first = head.next!!
            node.prev = head
            node.next = first
            head.next = node
            first.prev = node
        }

        private fun unlink(node: Node) {
            node.prev!!.next = node.next
            node.next!!.prev = node.prev
        }

        private fun moveToFront(node: Node) {
            unlink(node)
            addToFront(node)
        }
    }

    @Nested
    inner class Solution {

        @Test
        fun example() {
            val c = LRUCache(2)
            c.put(1, 1)
            c.put(2, 2)
            assertEquals(1, c.get(1))
            c.put(3, 3) // evicts key 2
            assertEquals(-1, c.get(2))
            c.put(4, 4) // evicts key 1
            assertEquals(-1, c.get(1))
            assertEquals(3, c.get(3))
            assertEquals(4, c.get(4))
        }

        @Test
        fun updateExistingKeyDoesNotEvict() {
            val c = LRUCache(2)
            c.put(1, 1)
            c.put(2, 2)
            c.put(1, 10) // update, key 2 should remain
            assertEquals(2, c.get(2))
            assertEquals(10, c.get(1))
        }

        @Test
        fun getRefreshesRecency() {
            val c = LRUCache(2)
            c.put(1, 1)
            c.put(2, 2)
            assertEquals(1, c.get(1)) // 1 becomes MRU
            c.put(3, 3) // evicts 2, not 1
            assertEquals(1, c.get(1))
            assertEquals(-1, c.get(2))
        }
    }
}
