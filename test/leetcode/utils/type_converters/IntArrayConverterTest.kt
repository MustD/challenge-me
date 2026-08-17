package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntArrayConverterTest {

    @Test
    fun `convert string to IntArray`() {
        val result = TypeConverters.convert("[1,2,3]", typeOf<IntArray>()) as IntArray
        assertEquals(listOf(1, 2, 3), result.toList())
    }

    @Test
    fun `convert empty array to empty IntArray`() {
        val result = TypeConverters.convert("[]", typeOf<IntArray>()) as IntArray
        assertEquals(emptyList<Int>(), result.toList())
    }

    @Test
    fun `equal IntArray - string expected`() =
        assertTrue(TypeConverters.equal(intArrayOf(1, 2, 3), "[1,2,3]", typeOf<IntArray>()))

    @Test
    fun `equal IntArray - typed expected`() =
        assertTrue(TypeConverters.equal(intArrayOf(1, 2, 3), intArrayOf(1, 2, 3), typeOf<IntArray>()))
}
