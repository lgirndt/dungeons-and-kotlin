package core

import io.dungeons.core.GridIndex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GridPositionTest {
    @Test
    fun `should create valid position with positive coordinates`() {
        val pos = GridIndex(5, 10)

        assertEquals(5, pos.x)
        assertEquals(10, pos.y)
    }

    @Test
    fun `should create position at origin`() {
        val pos = GridIndex(0, 0)

        assertEquals(0, pos.x)
        assertEquals(0, pos.y)
    }

    @Test
    fun `should support negative x`() {
        val pos = GridIndex(-1, 5)

        assertEquals(-1, pos.x)
        assertEquals(5, pos.y)
    }

    @Test
    fun `should support negative y`() {
        val pos = GridIndex(5, -1)

        assertEquals(5, pos.x)
        assertEquals(-1, pos.y)
    }

    @Test
    fun `should support both negative coordinates`() {
        val pos = GridIndex(-1, -1)

        assertEquals(-1, pos.x)
        assertEquals(-1, pos.y)
    }

    @Test
    fun `equality should work correctly`() {
        val pos1 = GridIndex(3, 4)
        val pos2 = GridIndex(3, 4)

        assertEquals(pos2, pos1)
    }

    @Test
    fun `inequality should work correctly`() {
        val pos1 = GridIndex(3, 4)
        val pos2 = GridIndex(4, 3)

        assertEquals(false, pos1 == pos2)
    }
}
