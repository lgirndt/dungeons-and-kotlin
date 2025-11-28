package io.dungeons.world

import io.dungeons.domain.world.Feet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FeetTest {
    @Test
    fun `adding two feet values should return correct result`() {
        val feet1 = Feet(10.0)
        val feet2 = Feet(5.0)
        val result = feet1 + feet2
        assertEquals(Feet(15.0), result)
    }

    @Test
    fun `subtracting two feet values should return correct result`() {
        val feet1 = Feet(10.0)
        val feet2 = Feet(3.0)
        val result = feet1 - feet2
        assertEquals(Feet(7.0), result)
    }

    @Test
    fun `multiplying two feet values should return correct result`() {
        val feet1 = Feet(4.0)
        val feet2 = Feet(2.5)
        val result = feet1 * feet2
        assertEquals(Feet(10.0), result)
    }

    @Test
    fun `dividing two feet values should return correct result`() {
        val feet1 = Feet(10.0)
        val feet2 = Feet(2.0)
        val result = feet1 / feet2
        assertEquals(Feet(5.0), result)
    }

    @Test
    fun `feet with same value should be equal`() {
        val feet1 = Feet(5.0)
        val feet2 = Feet(5.0)
        assertEquals(feet2, feet1)
    }

    @Test
    fun `operations with zero should work correctly`() {
        val feet = Feet(10.0)
        val zero = Feet(0.0)
        assertEquals(Feet(10.0), feet + zero)
        assertEquals(Feet(10.0), feet - zero)
        assertEquals(Feet(0.0), feet * zero)
    }

    @Test
    fun `operations with negative values should work correctly`() {
        val positive = Feet(10.0)
        val negative = Feet(-5.0)
        assertEquals(Feet(5.0), positive + negative)
        assertEquals(Feet(15.0), positive - negative)
    }
}
