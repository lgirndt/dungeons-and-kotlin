package core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.dungeons.core.GridIndex
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GridPositionTest {

    @Test
    fun `should create valid position with positive coordinates`() {
        val pos = GridIndex(5, 10)

        assertThat(pos.x, equalTo(5))
        assertThat(pos.y, equalTo(10))
    }

    @Test
    fun `should create position at origin`() {
        val pos = GridIndex(0, 0)

        assertThat(pos.x, equalTo(0))
        assertThat(pos.y, equalTo(0))
    }

    @Test
    fun `should support negative x`() {
        val pos = GridIndex(-1, 5)

        assertThat(pos.x, equalTo(-1))
        assertThat(pos.y, equalTo(5))
    }

    @Test
    fun `should support negative y`() {
        val pos = GridIndex(5, -1)

        assertThat(pos.x, equalTo(5))
        assertThat(pos.y, equalTo(-1))
    }

    @Test
    fun `should support both negative coordinates`() {
        val pos = GridIndex(-1, -1)

        assertThat(pos.x, equalTo(-1))
        assertThat(pos.y, equalTo(-1))
    }

    @Test
    fun `equality should work correctly`() {
        val pos1 = GridIndex(3, 4)
        val pos2 = GridIndex(3, 4)

        assertThat(pos1, equalTo(pos2))
    }

    @Test
    fun `inequality should work correctly`() {
        val pos1 = GridIndex(3, 4)
        val pos2 = GridIndex(4, 3)

        assertThat(pos1 == pos2, equalTo(false))
    }
}
