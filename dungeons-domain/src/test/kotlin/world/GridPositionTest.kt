package io.dungeons.world

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

class GridPositionTest {

    @Test
    fun `adding two grid positions should return correct result`() {
        val pos1 = GridPosition.from(3, 5)
        val pos2 = GridPosition.from(2, 4)
        val result = pos1 + pos2
        
        assertThat(result.x, equalTo(Square(5)))
        assertThat(result.y, equalTo(Square(9)))
    }

    @Test
    fun `subtracting two grid positions should return correct result`() {
        val pos1 = GridPosition.from(5, 7)
        val pos2 = GridPosition.from(2, 4)
        val result = pos1 - pos2
        
        assertThat(result.x, equalTo(Square(3)))
        assertThat(result.y, equalTo(Square(3)))
    }

    @Test
    fun `equality should be correct`() {
        val pos1 = GridPosition.from(1, 2)
        val pos2 = GridPosition.from(1, 2)
        assertThat(pos1, equalTo(pos2))
    }

    @Test
    fun `from factory method should create correct position`() {
        val pos = GridPosition.from(5, 10)
        assertThat(pos.x, equalTo(Square(5)))
        assertThat(pos.y, equalTo(Square(10)))
    }

    @Test
    fun `chebyshev distance between adjacent horizontal positions should be 1`() {
        val pos1 = GridPosition.from(0, 0)
        val pos2 = GridPosition.from(1, 0)
        
        assertThat(pos1.distance(pos2), equalTo(Square(1)))
    }

    @Test
    fun `chebyshev distance between adjacent vertical positions should be 1`() {
        val pos1 = GridPosition.from(0, 0)
        val pos2 = GridPosition.from(0, 1)
        
        assertThat(pos1.distance(pos2), equalTo(Square(1)))
    }

    @Test
    fun `chebyshev distance between adjacent diagonal positions should be 1`() {
        val pos1 = GridPosition.from(0, 0)
        val pos2 = GridPosition.from(1, 1)
        
        // In D&D, diagonal movement counts as 1 square
        assertThat(pos1.distance(pos2), equalTo(Square(1)))
    }

    @Test
    fun `chebyshev distance should be maximum of x and y differences`() {
        val pos1 = GridPosition.from(0, 0)
        val pos2 = GridPosition.from(3, 4)
        
        // Chebyshev distance = max(|3-0|, |4-0|) = 4
        assertThat(pos1.distance(pos2), equalTo(Square(4)))
    }

    @Test
    fun `chebyshev distance should be symmetric`() {
        val pos1 = GridPosition.from(1, 2)
        val pos2 = GridPosition.from(4, 6)
        
        assertThat(pos1.distance(pos2), equalTo(pos2.distance(pos1)))
    }

    @Test
    fun `chebyshev distance to same position should be 0`() {
        val pos = GridPosition.from(5, 5)
        
        assertThat(pos.distance(pos), equalTo(Square(0)))
    }

    @Test
    fun `chebyshev distance for L-shaped move should use larger component`() {
        val pos1 = GridPosition.from(0, 0)
        val pos2 = GridPosition.from(2, 5)
        
        // Can move diagonally 2 squares, then 3 more vertically = max(2, 5) = 5
        assertThat(pos1.distance(pos2), equalTo(Square(5)))
    }

    @Test
    fun `toCoordinate should convert grid position to world coordinates`() {
        val pos = GridPosition.from(2, 3)
        val coord = pos.toCoordinate()
        
        assertThat(coord.x, equalTo(Feet(10.0))) // 2 * 5 feet
        assertThat(coord.y, equalTo(Feet(15.0))) // 3 * 5 feet
    }

    @Test
    fun `toCoordinate for origin should return origin in feet`() {
        val pos = GridPosition.from(0, 0)
        val coord = pos.toCoordinate()
        
        assertThat(coord.x, equalTo(Feet(0.0)))
        assertThat(coord.y, equalTo(Feet(0.0)))
    }

    @Test
    fun `fromCoordinate should convert world coordinates to grid position`() {
        val coord = Coordinate.from(10.0, 15.0)
        val pos = GridPosition.fromCoordinate(coord)
        
        assertThat(pos.x, equalTo(Square(2))) // 10 / 5
        assertThat(pos.y, equalTo(Square(3))) // 15 / 5
    }

    @Test
    fun `fromCoordinate should round down to nearest grid square`() {
        val coord = Coordinate.from(12.5, 18.9)
        val pos = GridPosition.fromCoordinate(coord)
        
        assertThat(pos.x, equalTo(Square(2))) // 12.5 / 5 = 2.5 -> 2
        assertThat(pos.y, equalTo(Square(3))) // 18.9 / 5 = 3.78 -> 3
    }

    @Test
    fun `fromCoordinate for origin should return origin grid position`() {
        val coord = Coordinate.from(0.0, 0.0)
        val pos = GridPosition.fromCoordinate(coord)
        
        assertThat(pos.x, equalTo(Square(0)))
        assertThat(pos.y, equalTo(Square(0)))
    }

    @Test
    fun `toCoordinate and fromCoordinate should be consistent for aligned positions`() {
        val original = GridPosition.from(5, 7)
        val coord = original.toCoordinate()
        val roundtrip = GridPosition.fromCoordinate(coord)
        
        assertThat(roundtrip, equalTo(original))
    }

    @Test
    fun `chebyshev distance should work with negative differences`() {
        val pos1 = GridPosition.from(5, 8)
        val pos2 = GridPosition.from(2, 3)
        
        // Chebyshev distance = max(|5-2|, |8-3|) = max(3, 5) = 5
        assertThat(pos1.distance(pos2), equalTo(Square(5)))
    }

    @Test
    fun `adding positions with negative components should work correctly`() {
        val pos1 = GridPosition(Square(5), Square(3))
        val pos2 = GridPosition(Square(-2), Square(-1))
        val result = pos1 + pos2
        
        assertThat(result.x, equalTo(Square(3)))
        assertThat(result.y, equalTo(Square(2)))
    }

    @Test
    fun `subtracting positions resulting in negative components should work correctly`() {
        val pos1 = GridPosition.from(2, 3)
        val pos2 = GridPosition.from(5, 7)
        val result = pos1 - pos2
        
        assertThat(result.x, equalTo(Square(-3)))
        assertThat(result.y, equalTo(Square(-4)))
    }
}
