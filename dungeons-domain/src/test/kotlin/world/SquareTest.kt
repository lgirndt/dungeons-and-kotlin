package io.dungeons.world

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SquareTest {

    @Test
    fun `adding two square values should return correct result`() {
        val square1 = Square(3)
        val square2 = Square(2)
        val result = square1 + square2
        assertEquals(Square(5), result)
    }

    @Test
    fun `subtracting two square values should return correct result`() {
        val square1 = Square(10)
        val square2 = Square(3)
        val result = square1 - square2
        assertEquals(Square(7), result)
    }

    @Test
    fun `multiplying square by integer should return correct result`() {
        val square = Square(4)
        val result = square * 3
        assertEquals(Square(12), result)
    }

    @Test
    fun `dividing square by integer should return correct result`() {
        val square = Square(10)
        val result = square / 2
        assertEquals(Square(5), result)
    }

    @Test
    fun `squares with same value should be equal`() {
        val square1 = Square(5)
        val square2 = Square(5)
        assertEquals(square2, square1)
    }

    @Test
    fun `converting squares to feet should use 5 feet per square`() {
        val square = Square(1)
        assertEquals(Feet(5.0), square.toFeet())
    }

    @Test
    fun `converting multiple squares to feet should multiply correctly`() {
        val square = Square(6)
        assertEquals(Feet(30.0), square.toFeet())
    }

    @Test
    fun `converting zero squares to feet should return zero feet`() {
        val square = Square(0)
        assertEquals(Feet(0.0), square.toFeet())
    }

    @Test
    fun `comparing squares should work correctly`() {
        val small = Square(3)
        val large = Square(10)

        assertEquals(true, small < large)
        assertEquals(true, large > small)
        assertEquals(true, small <= Square(3))
        assertEquals(true, large >= Square(10))
    }

    @Test
    fun `operations with zero should work correctly`() {
        val square = Square(10)
        val zero = Square(0)

        assertEquals(Square(10), square + zero)
        assertEquals(Square(10), square - zero)
        assertEquals(Square(0), square * 0)
    }

    @Test
    fun `operations with negative results should work correctly`() {
        val square1 = Square(5)
        val square2 = Square(10)

        assertEquals(Square(-5), square1 - square2)
    }

    @Test
    fun `multiplying by negative factor should work correctly`() {
        val square = Square(5)
        val result = square * -2
        assertEquals(Square(-10), result)
    }

    @Test
    fun `FEET_PER_SQUARE constant should be 5`() {
        assertEquals(5.0, Square.FEET_PER_SQUARE)
    }
}
