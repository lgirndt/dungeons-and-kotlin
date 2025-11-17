package io.dungeons.world

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
