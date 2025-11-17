package io.dungeons.world

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

class IsInRangeTest {

    @Test
    fun `coordinates within range should return true`() {
        val from = Coordinate.from(0, 0)
        val to = Coordinate.from(3, 4)
        val range = Feet(10.0)
        assertThat(isInRange(from, to, range), equalTo(true))
    }

    @Test
    fun `coordinates exactly at range boundary should return true`() {
        val from = Coordinate.from(0, 0)
        val to = Coordinate.from(3, 4)
        val range = Feet(5.0) // exact distance
        assertThat(isInRange(from, to, range), equalTo(true))
    }

    @Test
    fun `coordinates beyond range should return false`() {
        val from = Coordinate.from(0, 0)
        val to = Coordinate.from(3, 4)
        val range = Feet(4.0) // distance is 5.0
        assertThat(isInRange(from, to, range), equalTo(false))
    }

    @Test
    fun `same coordinates should be in range`() {
        val coord = Coordinate.from(5, 5)
        val range = Feet(0.0)
        assertThat(isInRange(coord, coord, range), equalTo(true))
    }

    @Test
    fun `coordinates with zero range and non-zero distance should return false`() {
        val from = Coordinate.from(0, 0)
        val to = Coordinate.from(1, 0)
        val range = Feet(0.0)
        assertThat(isInRange(from, to, range), equalTo(false))
    }

    @Test
    fun `horizontal distance within range should return true`() {
        val from = Coordinate.from(0, 5)
        val to = Coordinate.from(3, 5)
        val range = Feet(5.0)
        assertThat(isInRange(from, to, range), equalTo(true))
    }

    @Test
    fun `vertical distance within range should return true`() {
        val from = Coordinate.from(5, 0)
        val to = Coordinate.from(5, 3)
        val range = Feet(5.0)
        assertThat(isInRange(from, to, range), equalTo(true))
    }

    @Test
    fun `diagonal distance beyond range should return false`() {
        val from = Coordinate.from(0, 0)
        val to = Coordinate.from(10, 10)
        val range = Feet(10.0) // actual distance is ~14.14
        assertThat(isInRange(from, to, range), equalTo(false))
    }
}
