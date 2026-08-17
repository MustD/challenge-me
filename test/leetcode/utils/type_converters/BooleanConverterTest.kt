package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BooleanConverterTest {

    @Test
    fun `convert string to Boolean`() =
        assertEquals(true, TypeConverters.convert("true", typeOf<Boolean>()))

    @Test
    fun `equal Boolean - string expected`() =
        assertTrue(TypeConverters.equal(true, "true", typeOf<Boolean>()))

    @Test
    fun `equal Boolean - typed expected`() =
        assertTrue(TypeConverters.equal(false, false, typeOf<Boolean>()))
}
