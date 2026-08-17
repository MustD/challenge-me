package leetcode.utils.type_converters

import leetcode.utils.QuadNode
import leetcode.utils.TypeConverters
import leetcode.utils.toQuadNode
import kotlin.reflect.typeOf
import kotlin.test.*

class QuadNodeConverterTest {

    // LeetCode 427 example: an internal root (`[0,1]`) with four leaf children.
    private val serialized = "[[0,1],[1,1],[1,0],[1,1],[1,1]]"

    @Test
    fun `convert string to QuadNode`() {
        val result = TypeConverters.convert(serialized, typeOf<QuadNode>()) as QuadNode?
        assertNotNull(result)
        assertEquals(false, result.isLeaf)
        assertEquals(true, result.`val`)
        // Four leaf children, in topLeft/topRight/bottomLeft/bottomRight order.
        assertEquals(true, result.topLeft?.isLeaf)
        assertEquals(true, result.topLeft?.`val`)
        assertEquals(false, result.topRight?.`val`)
    }

    @Test
    fun `convert empty array to null QuadNode`() =
        assertNull(TypeConverters.convert("[]", typeOf<QuadNode>()))

    @Test
    fun `equal QuadNode - string expected`() =
        assertTrue(TypeConverters.equal(serialized.toQuadNode(), serialized, typeOf<QuadNode>()))

    @Test
    fun `equal QuadNode - typed expected`() =
        assertTrue(TypeConverters.equal(serialized.toQuadNode(), serialized.toQuadNode(), typeOf<QuadNode>()))
}
