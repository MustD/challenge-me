package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DoubleArrayConverterTest {

    @Test
    fun `convert string to DoubleArray`() {
        val result = TypeConverters.convert("[1.0,2.5,3.0]", typeOf<DoubleArray>()) as DoubleArray
        assertEquals(listOf(1.0, 2.5, 3.0), result.toList())
    }

    @Test
    fun `equal DoubleArray - string expected`() =
        assertTrue(TypeConverters.equal(doubleArrayOf(1.0, 2.5, 3.0), "[1.0,2.5,3.0]", typeOf<DoubleArray>()))

    @Test
    fun `equal DoubleArray - typed expected`() =
        assertTrue(
            TypeConverters.equal(
                doubleArrayOf(1.0, 2.5, 3.0),
                doubleArrayOf(1.0, 2.5, 3.0),
                typeOf<DoubleArray>()
            )
        )
}
