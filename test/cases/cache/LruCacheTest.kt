package cases.cache

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.expect


class LruCacheTest {

    class LruReference<K : Any, V : Any>(val capacity: UInt) : Cache<K, V> {
        private val intCapacity = capacity.toInt()
        val implementation = object : LinkedHashMap<K, V>(
            /* initialCapacity = */ intCapacity,
            /* loadFactor = */ 0.75f,
            /* accessOrder = */ true
        ) {
            override fun removeEldestEntry(eldest: Map.Entry<K?, V?>?) = size > intCapacity
        }

        override fun add(key: K, value: V) {
            implementation[key] = value
        }

        override fun remove(key: K) {
            implementation.remove(key)
        }

        override fun get(key: K): V? {
            return implementation[key]
        }
    }

    @Test
    fun initTest() {
        val actual = runCatching { LruCache<Int, Int>(10u) }
        assertTrue("Unexpected init result ${actual.exceptionOrNull()}") { actual.isSuccess }
    }

    @Test
    fun smoke() {
        val given = LruCache<Int, Int>(3u)

        val actual = runCatching {
            (1..4).forEach {
                given.add(it, it)
            }
            expect(null, "Unexpected cache state: LRU value(1) exist") { given.get(1) }
            (2..4).forEach {
                expect(it, "Unexpected cache value($it) not exist") { given.get(it) }
            }
        }
        assertTrue("Unexpected smoke exception: ${actual.exceptionOrNull()}") { actual.isSuccess }
    }

}
