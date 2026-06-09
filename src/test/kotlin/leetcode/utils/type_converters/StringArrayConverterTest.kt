package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StringArrayConverterTest {

    @Test
    fun `convert string to Array of String`() {
        val result = TypeConverters.convert("""["a", "b", "c"]""", typeOf<Array<String>>()) as Array<*>
        assertEquals(listOf("a", "b", "c"), result.toList())
    }

    @Test
    fun `equal Array of String - string expected`() =
        assertTrue(TypeConverters.equal(arrayOf("a", "b", "c"), """["a", "b", "c"]""", typeOf<Array<String>>()))

    @Test
    fun `equal Array of String - typed expected`() =
        assertTrue(TypeConverters.equal(arrayOf("a", "b", "c"), arrayOf("a", "b", "c"), typeOf<Array<String>>()))
}
