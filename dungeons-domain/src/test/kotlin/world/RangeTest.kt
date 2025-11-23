package io.dungeons.world

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IsInRangeTest {

    @Test
    fun `coordinates within range should return true`() {
        val from = Coordinate.from(0, 0)
        val to = Coordinate.from(3, 4)
        val range = Feet(10.0)
        assertEquals(true, isInRange(from, to, range))
    }

    @Test
    fun `coordinates exactly at range boundary should return true`() {
        val from = Coordinate.from(0, 0)
        val to = Coordinate.from(3, 4)
        val range = Feet(5.0) // exact distance
        assertEquals(true, isInRange(from, to, range))
    }

    @Test
    fun `coordinates beyond range should return false`() {
        val from = Coordinate.from(0, 0)
        val to = Coordinate.from(3, 4)
        val range = Feet(4.0) // distance is 5.0
        assertEquals(false, isInRange(from, to, range))
    }

    @Test
    fun `same coordinates should be in range`() {
        val coord = Coordinate.from(5, 5)
        val range = Feet(0.0)
        assertEquals(true, isInRange(coord, coord, range))
    }

    @Test
    fun `coordinates with zero range and non-zero distance should return false`() {
        val from = Coordinate.from(0, 0)
        val to = Coordinate.from(1, 0)
        val range = Feet(0.0)
        assertEquals(false, isInRange(from, to, range))
    }

    @Test
    fun `horizontal distance within range should return true`() {
        val from = Coordinate.from(0, 5)
        val to = Coordinate.from(3, 5)
        val range = Feet(5.0)
        assertEquals(true, isInRange(from, to, range))
    }

    @Test
    fun `vertical distance within range should return true`() {
        val from = Coordinate.from(5, 0)
        val to = Coordinate.from(5, 3)
        val range = Feet(5.0)
        assertEquals(true, isInRange(from, to, range))
    }

    @Test
    fun `diagonal distance beyond range should return false`() {
        val from = Coordinate.from(0, 0)
        val to = Coordinate.from(10, 10)
        val range = Feet(10.0) // actual distance is ~14.14
        assertEquals(false, isInRange(from, to, range))
    }
}
