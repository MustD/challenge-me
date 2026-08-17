package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LongConverterTest {

    @Test
    fun `convert string to Long`() =
        assertEquals(100L, TypeConverters.convert("100", typeOf<Long>()))

    @Test
    fun `equal Long - string expected`() =
        assertTrue(TypeConverters.equal(100L, "100", typeOf<Long>()))

    @Test
    fun `equal Long - typed expected`() =
        assertTrue(TypeConverters.equal(100L, 100L, typeOf<Long>()))
}
