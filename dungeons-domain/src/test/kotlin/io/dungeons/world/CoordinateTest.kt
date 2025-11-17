package io.dungeons.world

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

class CoordinateTest {

    @Test
    fun `adding two coordinates should return correct result`() {
        val coord1 = Coordinate.from(3, 5)
        val coord2 = Coordinate.from(2, 4)
        val result = coord1 + coord2
        assertThat(result.x, equalTo(Feet(3 + 2)))
        assertThat(result.y, equalTo(Feet(5 + 4)))
    }

    @Test
    fun `subtracting two coordinates should return correct result`() {
        val coord1 = Coordinate.from(5, 7)
        val coord2 = Coordinate.from(2, 4)
        val result = coord1 - coord2
        assertThat(result.x, equalTo(Feet(5 - 2)))
        assertThat(result.y, equalTo(Feet(7 - 4)))
    }

    @Test
    fun `equality should be correct`() {
        val coord1 = Coordinate.from(1, 2)
        val coord2 = Coordinate.from(1, 2)
        assertThat(coord1, equalTo(coord2))
    }

    @Test
    fun `distance between two coordinates should be correct`() {
        val coord1 = Coordinate.from(0, 0)
        val coord2 = Coordinate.from(3, 4)
        val distance = coord1.distance(coord2)
        assertThat(distance, equalTo(Feet(5.0))) // 3-4-5 triangle
    }
}
