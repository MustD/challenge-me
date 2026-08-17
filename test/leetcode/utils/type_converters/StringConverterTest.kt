package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StringConverterTest {

    @Test
    fun `convert string to String`() =
        assertEquals("hello", TypeConverters.convert("hello", typeOf<String>()))

    @Test
    fun `equal String - string expected`() =
        assertTrue(TypeConverters.equal("hello", "hello", typeOf<String>()))

    @Test
    fun `equal String - typed expected`() =
        assertTrue(TypeConverters.equal("hello", "hello", typeOf<String>()))
}
