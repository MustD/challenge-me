package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CharArrayConverterTest {

    @Test
    fun `convert string to CharArray`() {
        val result = TypeConverters.convert("[\"a\",\"b\",\"c\"]", typeOf<CharArray>()) as CharArray
        assertEquals(listOf('a', 'b', 'c'), result.toList())
    }

    @Test
    fun `convert empty array to empty CharArray`() {
        val result = TypeConverters.convert("[]", typeOf<CharArray>()) as CharArray
        assertEquals(emptyList<Char>(), result.toList())
    }

    @Test
    fun `equal CharArray - string expected`() =
        assertTrue(TypeConverters.equal(charArrayOf('a', 'b', 'c'), "[\"a\",\"b\",\"c\"]", typeOf<CharArray>()))

    @Test
    fun `equal CharArray - typed expected`() =
        assertTrue(
            TypeConverters.equal(
                charArrayOf('a', 'b', 'c'),
                charArrayOf('a', 'b', 'c'),
                typeOf<CharArray>()
            )
        )

    @Test
    fun `unequal CharArray`() =
        assertFalse(TypeConverters.equal(charArrayOf('a', 'b'), "[\"a\",\"c\"]", typeOf<CharArray>()))
}
