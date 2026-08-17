package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DoubleConverterTest {

    @Test
    fun `convert string to Double`() =
        assertEquals(3.14, TypeConverters.convert("3.14", typeOf<Double>()))

    @Test
    fun `equal Double - string expected`() =
        assertTrue(TypeConverters.equal(3.14, "3.14", typeOf<Double>()))

    @Test
    fun `equal Double - typed expected`() =
        assertTrue(TypeConverters.equal(3.14, 3.14, typeOf<Double>()))
}
