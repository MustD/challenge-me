package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntConverterTest {

    @Test
    fun `convert string to Int`() =
        assertEquals(42, TypeConverters.convert("42", typeOf<Int>()))

    @Test
    fun `equal Int - string expected`() =
        assertTrue(TypeConverters.equal(42, "42", typeOf<Int>()))

    @Test
    fun `equal Int - typed expected`() =
        assertTrue(TypeConverters.equal(42, 42, typeOf<Int>()))
}
