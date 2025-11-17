package io.dungeons.world

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

class SquareTest {

    @Test
    fun `adding two square values should return correct result`() {
        val square1 = Square(3)
        val square2 = Square(2)
        val result = square1 + square2
        assertThat(result, equalTo(Square(5)))
    }

    @Test
    fun `subtracting two square values should return correct result`() {
        val square1 = Square(10)
        val square2 = Square(3)
        val result = square1 - square2
        assertThat(result, equalTo(Square(7)))
    }

    @Test
    fun `multiplying square by integer should return correct result`() {
        val square = Square(4)
        val result = square * 3
        assertThat(result, equalTo(Square(12)))
    }

    @Test
    fun `dividing square by integer should return correct result`() {
        val square = Square(10)
        val result = square / 2
        assertThat(result, equalTo(Square(5)))
    }

    @Test
    fun `squares with same value should be equal`() {
        val square1 = Square(5)
        val square2 = Square(5)
        assertThat(square1, equalTo(square2))
    }

    @Test
    fun `converting squares to feet should use 5 feet per square`() {
        val square = Square(1)
        assertThat(square.toFeet(), equalTo(Feet(5.0)))
    }

    @Test
    fun `converting multiple squares to feet should multiply correctly`() {
        val square = Square(6)
        assertThat(square.toFeet(), equalTo(Feet(30.0)))
    }

    @Test
    fun `converting zero squares to feet should return zero feet`() {
        val square = Square(0)
        assertThat(square.toFeet(), equalTo(Feet(0.0)))
    }

    @Test
    fun `comparing squares should work correctly`() {
        val small = Square(3)
        val large = Square(10)

        assertThat(small < large, equalTo(true))
        assertThat(large > small, equalTo(true))
        assertThat(small <= Square(3), equalTo(true))
        assertThat(large >= Square(10), equalTo(true))
    }

    @Test
    fun `operations with zero should work correctly`() {
        val square = Square(10)
        val zero = Square(0)

        assertThat(square + zero, equalTo(Square(10)))
        assertThat(square - zero, equalTo(Square(10)))
        assertThat(square * 0, equalTo(Square(0)))
    }

    @Test
    fun `operations with negative results should work correctly`() {
        val square1 = Square(5)
        val square2 = Square(10)

        assertThat(square1 - square2, equalTo(Square(-5)))
    }

    @Test
    fun `multiplying by negative factor should work correctly`() {
        val square = Square(5)
        val result = square * -2
        assertThat(result, equalTo(Square(-10)))
    }

    @Test
    fun `FEET_PER_SQUARE constant should be 5`() {
        assertThat(Square.FEET_PER_SQUARE, equalTo(5.0))
    }
}
