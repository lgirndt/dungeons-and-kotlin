package core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.dungeons.core.GridPosition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GridPositionTest {

    @Test
    fun `should create valid position with non-negative coordinates`() {
        val pos = GridPosition(5, 10)

        assertThat(pos.x, equalTo(5))
        assertThat(pos.y, equalTo(10))
    }

    @Test
    fun `should create position at origin`() {
        val pos = GridPosition(0, 0)

        assertThat(pos.x, equalTo(0))
        assertThat(pos.y, equalTo(0))
    }

    @Test
    fun `should reject negative x`() {
        val exception = assertThrows<IllegalArgumentException> {
            GridPosition(-1, 5)
        }
        assertThat(exception.message, equalTo("x must be non-negative, but was -1"))
    }

    @Test
    fun `should reject negative y`() {
        val exception = assertThrows<IllegalArgumentException> {
            GridPosition(5, -1)
        }
        assertThat(exception.message, equalTo("y must be non-negative, but was -1"))
    }

    @Test
    fun `should reject both negative coordinates`() {
        assertThrows<IllegalArgumentException> {
            GridPosition(-1, -1)
        }
    }

    @Test
    fun `equality should work correctly`() {
        val pos1 = GridPosition(3, 4)
        val pos2 = GridPosition(3, 4)

        assertThat(pos1, equalTo(pos2))
    }

    @Test
    fun `inequality should work correctly`() {
        val pos1 = GridPosition(3, 4)
        val pos2 = GridPosition(4, 3)

        assertThat(pos1 == pos2, equalTo(false))
    }
}
