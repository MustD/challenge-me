package leetcode.utils.type_converters

import leetcode.utils.NaryNode
import leetcode.utils.TypeConverters
import leetcode.utils.toNaryNode
import kotlin.reflect.typeOf
import kotlin.test.*

class NaryNodeConverterTest {

    @Test
    fun `convert string to NaryNode`() {
        val result = TypeConverters.convert("[1,null,3,2,4,null,5,6]", typeOf<NaryNode>()) as NaryNode?
        assertNotNull(result)
        assertEquals(1, result.`val`)
        assertEquals(listOf(3, 2, 4), result.children.map { it.`val` })
        assertEquals(listOf(5, 6), result.children[0].children.map { it.`val` })
        assertTrue(result.children[1].children.isEmpty())
    }

    @Test
    fun `empty string converts to null`() {
        assertNull(TypeConverters.convert("[]", typeOf<NaryNode>()))
    }

    @Test
    fun `single node round trips`() = assertEquals("[1]", "[1]".toNaryNode().toString())

    @Test
    fun `round trip deeper tree`() {
        val s = "[1,null,2,3,4,5,null,null,6,7,null,8,null,9,10,null,null,11,null,12,null,13,null,null,14]"
        assertEquals(s, s.toNaryNode().toString())
    }

    @Test
    fun `whitespace is tolerated`() =
        assertEquals("[1,null,3,2,4,null,5,6]", "[1, null, 3, 2, 4,\n null, 5, 6]".toNaryNode().toString())

    @Test
    fun `equal NaryNode - string expected`() =
        assertTrue(TypeConverters.equal("[1,null,3,2]".toNaryNode(), "[1,null,3,2]", typeOf<NaryNode>()))

    @Test
    fun `not equal NaryNode - different shape`() =
        assertFalse(TypeConverters.equal("[1,null,3,2]".toNaryNode(), "[1,null,2,3]", typeOf<NaryNode>()))
}
