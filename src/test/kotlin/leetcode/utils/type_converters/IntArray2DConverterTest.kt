package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntArray2DConverterTest {

    @Test
    fun `convert string to Array of IntArray`() {
        val result = TypeConverters.convert("[[1,2],[3,4]]", typeOf<Array<IntArray>>()) as Array<IntArray>
        assertEquals(listOf(listOf(1, 2), listOf(3, 4)), result.map { it.toList() })
    }

    @Test
    fun `equal Array of IntArray - string expected`() =
        assertTrue(
            TypeConverters.equal(
                arrayOf(intArrayOf(1, 2), intArrayOf(3, 4)),
                "[[1,2],[3,4]]",
                typeOf<Array<IntArray>>()
            )
        )

    @Test
    fun `equal Array of IntArray - typed expected`() =
        assertTrue(
            TypeConverters.equal(
                arrayOf(intArrayOf(1, 2), intArrayOf(3, 4)),
                arrayOf(intArrayOf(1, 2), intArrayOf(3, 4)),
                typeOf<Array<IntArray>>()
            )
        )
}
