package leetcode.utils.type_converters

import leetcode.utils.Node
import leetcode.utils.TypeConverters
import leetcode.utils.toNode
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NodeConverterTest {

    @Test
    fun `convert string to Node`() {
        val result = TypeConverters.convert("[[2,4],[1,3],[2,4],[1,3]]", typeOf<Node>()) as Node?
        assertNotNull(result)
        assertEquals(1, result.`val`)
        assertEquals(2, result.neighbors.size)
        assertEquals(2, result.neighbors[0]?.`val`)
        assertEquals(4, result.neighbors[1]?.`val`)
    }

    @Test
    fun `equal Node - string expected`() =
        assertTrue(
            TypeConverters.equal(
                "[[2,4],[1,3],[2,4],[1,3]]".toNode(),
                "[[2,4],[1,3],[2,4],[1,3]]",
                typeOf<Node>()
            )
        )

    @Test
    fun `equal Node - typed expected`() =
        assertTrue(
            TypeConverters.equal(
                "[[2,4],[1,3],[2,4],[1,3]]".toNode(),
                "[[2,4],[1,3],[2,4],[1,3]]".toNode(),
                typeOf<Node>()
            )
        )
}
