package cases.cache

interface Cache<K : Any, V : Any> {
    fun add(key: K, value: V)
    fun remove(key: K)
    fun get(key: K): V?
}

class LruCache<K : Any, V : Any>(capacity: UInt) : Cache<K, V> {
    val intCapacity = capacity.toInt()

    private class Node<K, V>(val key: K, var value: V) {
        var next: Node<K, V> = this
        var prev: Node<K, V> = this
    }


    private val index = HashMap<K, Node<K, V>>(intCapacity)

    @Suppress("UNCHECKED_CAST")
    private val guard = Node(key = Unit, value = Unit) as Node<K, V>
    private fun addFirst(node: Node<K, V>) {
        val old = guard.next
        //linking old first
        old.prev = node
        node.next = old

        //linking guard
        guard.next = node
        node.prev = guard
    }

    private fun unlink(node: Node<K, V>) {
        val nodeNext = node.next
        val nodePrev = node.prev
        //link prev and next
        nodeNext.prev = nodePrev
        nodePrev.next = nodeNext

        node.next = node
        node.prev = node
    }


    val lock = Any()
    override fun add(key: K, value: V): Unit = synchronized(lock) {
        val node = index.computeIfAbsent(key) { Node(key, value) }.also { it.value = value }
        unlink(node)
        addFirst(node)
        if (index.size > intCapacity) {
            val last = guard.prev
            unlink(last)
            index.remove(last.key)
        }
    }

    override fun remove(key: K): Unit = synchronized(lock) {
        index.remove(key)?.let { unlink(it) }
    }

    override fun get(key: K): V? = synchronized(lock) {
        return index[key]?.let {
            unlink(it)
            addFirst(it)
            it.value
        }
    }


}
