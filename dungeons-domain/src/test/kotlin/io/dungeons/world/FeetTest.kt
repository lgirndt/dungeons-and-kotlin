package io.dungeons.world

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

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
