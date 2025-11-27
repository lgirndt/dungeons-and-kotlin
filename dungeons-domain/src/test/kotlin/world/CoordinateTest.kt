package io.dungeons.world

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CoordinateTest {
    @Test
    fun `adding two coordinates should return correct result`() {
        val coord1 = Coordinate.from(3, 5)
        val coord2 = Coordinate.from(2, 4)
        val result = coord1 + coord2
        assertEquals(Feet(3 + 2), result.x)
        assertEquals(Feet(5 + 4), result.y)
    }

    @Test
    fun `subtracting two coordinates should return correct result`() {
        val coord1 = Coordinate.from(5, 7)
        val coord2 = Coordinate.from(2, 4)
        val result = coord1 - coord2
        assertEquals(Feet(5 - 2), result.x)
        assertEquals(Feet(7 - 4), result.y)
    }

    @Test
    fun `equality should be correct`() {
        val coord1 = Coordinate.from(1, 2)
        val coord2 = Coordinate.from(1, 2)
        assertEquals(coord2, coord1)
    }

    @Test
    fun `distance between two coordinates should be correct`() {
        val coord1 = Coordinate.from(0, 0)
        val coord2 = Coordinate.from(3, 4)
        val distance = coord1.distance(coord2)
        assertEquals(Feet(5.0), distance) // 3-4-5 triangle
    }
}
