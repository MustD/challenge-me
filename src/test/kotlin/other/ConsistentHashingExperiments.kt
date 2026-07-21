package other

import java.security.MessageDigest
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConsistentHashing(private val numberOfReplicas: Int = 100) {
    private val ring = TreeMap<Int, String>()

    // Consistent hashing needs a hash that scatters values *uniformly* across the ring.
    // String.hashCode() clusters structurally-similar strings ("node-1", "node-2", ...) into
    // a narrow arc, which collapses the whole ring onto one node. Use MD5 and take the first
    // 4 bytes as the ring position instead.
    private fun hash(input: String): Int {
        val d = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return ((d[0].toInt() and 0xFF) shl 24) or
                ((d[1].toInt() and 0xFF) shl 16) or
                ((d[2].toInt() and 0xFF) shl 8) or
                (d[3].toInt() and 0xFF)
    }

    fun addNode(node: String) {
        for (i in 0 until numberOfReplicas) {
            ring[hash("$node#$i")] = node
        }
    }

    fun removeNode(node: String) {
        for (i in 0 until numberOfReplicas) {
            ring.remove(hash("$node#$i"))
        }
    }

    fun getNode(key: String): String? {
        if (ring.isEmpty()) return null
        val hash = hash(key)

        // Find the first node with hash >= key's hash
        val tailMap = ring.tailMap(hash)
        val targetHash = if (tailMap.isEmpty()) ring.firstKey() else tailMap.firstKey()

        return ring[targetHash]
    }
}


class ConsistentHashingExperiments {
    @Test
    fun testBasicHashing() {
        val ch = ConsistentHashing(numberOfReplicas = 50)
        ch.addNode("node-1")
        ch.addNode("node-2")
        ch.addNode("node-3")

        val key1 = "some-key"
        val node = ch.getNode(key1)
        println("Key '$key1' mapped to: $node")

        assertNotNull(node)

        // Verify multiple virtual nodes distribution
        val counts = mutableMapOf<String, Int>()
        for (i in 0 until 1000) {
            val n = ch.getNode("key-$i")
            if (n != null) {
                counts[n] = counts.getOrDefault(n, 0) + 1
            }
        }
        println("Distribution: $counts")
        assertEquals(3, counts.size, "Expected keys to be distributed among 3 nodes")
    }
}
