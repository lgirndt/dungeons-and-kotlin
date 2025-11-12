import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.example.Coordinate
import org.example.Feet
import org.example.isInRange
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