
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.dungeons.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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

class FeetTest {

    @Test
    fun `adding two feet values should return correct result`() {
        val feet1 = Feet(10.0)
        val feet2 = Feet(5.0)
        val result = feet1 + feet2
        assertThat(result, equalTo(Feet(15.0)))
    }

    @Test
    fun `subtracting two feet values should return correct result`() {
        val feet1 = Feet(10.0)
        val feet2 = Feet(3.0)
        val result = feet1 - feet2
        assertThat(result, equalTo(Feet(7.0)))
    }

    @Test
    fun `multiplying two feet values should return correct result`() {
        val feet1 = Feet(4.0)
        val feet2 = Feet(2.5)
        val result = feet1 * feet2
        assertThat(result, equalTo(Feet(10.0)))
    }

    @Test
    fun `dividing two feet values should return correct result`() {
        val feet1 = Feet(10.0)
        val feet2 = Feet(2.0)
        val result = feet1 / feet2
        assertThat(result, equalTo(Feet(5.0)))
    }

    @Test
    fun `feet with same value should be equal`() {
        val feet1 = Feet(5.0)
        val feet2 = Feet(5.0)
        assertThat(feet1, equalTo(feet2))
    }

    @Test
    fun `operations with zero should work correctly`() {
        val feet = Feet(10.0)
        val zero = Feet(0.0)
        assertThat(feet + zero, equalTo(Feet(10.0)))
        assertThat(feet - zero, equalTo(Feet(10.0)))
        assertThat(feet * zero, equalTo(Feet(0.0)))
    }

    @Test
    fun `operations with negative values should work correctly`() {
        val positive = Feet(10.0)
        val negative = Feet(-5.0)
        assertThat(positive + negative, equalTo(Feet(5.0)))
        assertThat(positive - negative, equalTo(Feet(15.0)))
    }
}

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

class BitMatrixTest {

    @Test
    fun `newly created BitMatrix should have all bits set to false`() {
        val matrix = BitMatrix(5, 5)

        for (x in 0 until 5) {
            for (y in 0 until 5) {
                assertThat(matrix.get(x, y), equalTo(false))
            }
        }
    }

    @Test
    fun `setting a bit to true should be retrievable`() {
        val matrix = BitMatrix(10, 10)

        matrix.set(3, 7, true)

        assertThat(matrix.get(3, 7), equalTo(true))
    }

    @Test
    fun `setting a bit to false should be retrievable`() {
        val matrix = BitMatrix(10, 10)

        matrix.set(3, 7, true)
        matrix.set(3, 7, false)

        assertThat(matrix.get(3, 7), equalTo(false))
    }

    @Test
    fun `setting multiple bits should not affect other bits`() {
        val matrix = BitMatrix(10, 10)

        matrix.set(0, 0, true)
        matrix.set(5, 5, true)
        matrix.set(9, 9, true)

        assertThat(matrix.get(0, 0), equalTo(true))
        assertThat(matrix.get(5, 5), equalTo(true))
        assertThat(matrix.get(9, 9), equalTo(true))

        // Check that adjacent cells are still false
        assertThat(matrix.get(0, 1), equalTo(false))
        assertThat(matrix.get(1, 0), equalTo(false))
        assertThat(matrix.get(5, 4), equalTo(false))
        assertThat(matrix.get(4, 5), equalTo(false))
    }

    @Test
    fun `setting and clearing all bits should work correctly`() {
        val matrix = BitMatrix(8, 8)

        // Set all bits to true
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                matrix.set(x, y, true)
            }
        }

        // Verify all bits are true
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                assertThat(matrix.get(x, y), equalTo(true))
            }
        }

        // Clear all bits
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                matrix.set(x, y, false)
            }
        }

        // Verify all bits are false
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                assertThat(matrix.get(x, y), equalTo(false))
            }
        }
    }

    @Test
    fun `accessing bit at boundary positions should work correctly`() {
        val matrix = BitMatrix(5, 5)

        // Test corners
        matrix.set(0, 0, true)
        matrix.set(4, 0, true)
        matrix.set(0, 4, true)
        matrix.set(4, 4, true)

        assertThat(matrix.get(0, 0), equalTo(true))
        assertThat(matrix.get(4, 0), equalTo(true))
        assertThat(matrix.get(0, 4), equalTo(true))
        assertThat(matrix.get(4, 4), equalTo(true))
    }

    @Test
    fun `accessing bit at boundary positions of large matrix should work correctly`() {
        val x = 1000;
        val y = 1000;
        val matrix = BitMatrix(x+1, y+1)

        // Test corners
        matrix.set(0, 0, true)
        matrix.set(x, 0, true)
        matrix.set(0, y, true)
        matrix.set(x, y, true)

        assertThat(matrix.get(0, 0), equalTo(true))
        assertThat(matrix.get(x, 0), equalTo(true))
        assertThat(matrix.get(0, y), equalTo(true))
        assertThat(matrix.get(x, y), equalTo(true))
    }

    @Test
    fun `accessing bit with negative x coordinate should throw exception`() {
        val matrix = BitMatrix(5, 5)

        assertThrows<IllegalArgumentException> {
            matrix.get(-1, 2)
        }
    }

    @Test
    fun `accessing bit with negative y coordinate should throw exception`() {
        val matrix = BitMatrix(5, 5)

        assertThrows<IllegalArgumentException> {
            matrix.get(2, -1)
        }
    }

    @Test
    fun `accessing bit with x coordinate beyond width should throw exception`() {
        val matrix = BitMatrix(5, 5)

        assertThrows<IllegalArgumentException> {
            matrix.get(5, 2)
        }
    }

    @Test
    fun `accessing bit with y coordinate beyond height should throw exception`() {
        val matrix = BitMatrix(5, 5)

        assertThrows<IllegalArgumentException> {
            matrix.get(2, 5)
        }
    }

    @Test
    fun `setting bit with out of bounds coordinates should throw exception`() {
        val matrix = BitMatrix(5, 5)

        assertThrows<IllegalArgumentException> {
            matrix.set(10, 2, true)
        }
    }

    @Test
    fun `BitMatrix with large dimensions should work correctly`() {
        val matrix = BitMatrix(100, 100)

        // Set some scattered bits
        matrix.set(0, 0, true)
        matrix.set(50, 50, true)
        matrix.set(99, 99, true)

        assertThat(matrix.get(0, 0), equalTo(true))
        assertThat(matrix.get(50, 50), equalTo(true))
        assertThat(matrix.get(99, 99), equalTo(true))

        // Verify other positions are still false
        assertThat(matrix.get(1, 1), equalTo(false))
        assertThat(matrix.get(51, 51), equalTo(false))
    }

    @Test
    fun `BitMatrix should handle bits spanning multiple UInt storage units`() {
        // Create a matrix large enough to require multiple UInt storage units
        // UInt.SIZE_BITS is typically 32
        val matrix = BitMatrix(10, 10) // 100 bits, will span multiple UInts

        // Set bits across different UInt boundaries
        matrix.set(0, 0, true)  // First UInt
        matrix.set(9, 3, true)  // Likely in second UInt (bit index 39)
        matrix.set(9, 9, true)  // Last position (bit index 99)

        assertThat(matrix.get(0, 0), equalTo(true))
        assertThat(matrix.get(9, 3), equalTo(true))
        assertThat(matrix.get(9, 9), equalTo(true))

        // Verify adjacent bits are still false
        assertThat(matrix.get(1, 0), equalTo(false))
        assertThat(matrix.get(8, 3), equalTo(false))
        assertThat(matrix.get(9, 8), equalTo(false))
    }

    @Test
    fun `toggling the same bit multiple times should work correctly`() {
        val matrix = BitMatrix(5, 5)

        matrix.set(2, 2, true)
        assertThat(matrix.get(2, 2), equalTo(true))

        matrix.set(2, 2, false)
        assertThat(matrix.get(2, 2), equalTo(false))

        matrix.set(2, 2, true)
        assertThat(matrix.get(2, 2), equalTo(true))

        matrix.set(2, 2, false)
        assertThat(matrix.get(2, 2), equalTo(false))
    }

    @Test
    fun `BitMatrix with width of 1 should work correctly`() {
        val matrix = BitMatrix(1, 5)

        matrix.set(0, 0, true)
        matrix.set(0, 2, true)
        matrix.set(0, 4, true)

        assertThat(matrix.get(0, 0), equalTo(true))
        assertThat(matrix.get(0, 1), equalTo(false))
        assertThat(matrix.get(0, 2), equalTo(true))
        assertThat(matrix.get(0, 3), equalTo(false))
        assertThat(matrix.get(0, 4), equalTo(true))
    }

    @Test
    fun `BitMatrix with height of 1 should work correctly`() {
        val matrix = BitMatrix(5, 1)

        matrix.set(0, 0, true)
        matrix.set(2, 0, true)
        matrix.set(4, 0, true)

        assertThat(matrix.get(0, 0), equalTo(true))
        assertThat(matrix.get(1, 0), equalTo(false))
        assertThat(matrix.get(2, 0), equalTo(true))
        assertThat(matrix.get(3, 0), equalTo(false))
        assertThat(matrix.get(4, 0), equalTo(true))
    }
}