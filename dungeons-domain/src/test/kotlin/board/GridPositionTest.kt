package board

import io.dungeons.board.BoardPosition
import io.dungeons.board.distance
import io.dungeons.world.Coordinate
import io.dungeons.world.Feet
import io.dungeons.world.Square
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GridPositionTest {
    @Test
    fun `adding two grid positions should return correct result`() {
        val pos1 = BoardPosition.from(3, 5)
        val pos2 = BoardPosition.from(2, 4)
        val result = pos1 + pos2

        assertEquals(Square(5), result.x)
        assertEquals(Square(9), result.y)
    }

    @Test
    fun `subtracting two grid positions should return correct result`() {
        val pos1 = BoardPosition.from(5, 7)
        val pos2 = BoardPosition.from(2, 4)
        val result = pos1 - pos2

        assertEquals(Square(3), result.x)
        assertEquals(Square(3), result.y)
    }

    @Test
    fun `equality should be correct`() {
        val pos1 = BoardPosition.from(1, 2)
        val pos2 = BoardPosition.from(1, 2)
        assertEquals(pos2, pos1)
    }

    @Test
    fun `from factory method should create correct position`() {
        val pos = BoardPosition.from(5, 10)
        assertEquals(Square(5), pos.x)
        assertEquals(Square(10), pos.y)
    }

    @Test
    fun `chebyshev distance between adjacent horizontal positions should be 1`() {
        val pos1 = BoardPosition.from(0, 0)
        val pos2 = BoardPosition.from(1, 0)

        assertEquals(Square(1), distance(pos1, pos2))
    }

    @Test
    fun `chebyshev distance between adjacent vertical positions should be 1`() {
        val pos1 = BoardPosition.from(0, 0)
        val pos2 = BoardPosition.from(0, 1)

        assertEquals(Square(1), distance(pos1, pos2))
    }

    @Test
    fun `chebyshev distance between adjacent diagonal positions should be 1`() {
        val pos1 = BoardPosition.from(0, 0)
        val pos2 = BoardPosition.from(1, 1)

        // In D&D, diagonal movement counts as 1 square
        assertEquals(Square(1), distance(pos1, pos2))
    }

    @Test
    fun `chebyshev distance should be maximum of x and y differences`() {
        val pos1 = BoardPosition.from(0, 0)
        val pos2 = BoardPosition.from(3, 4)

        // Chebyshev distance = max(|3-0|, |4-0|) = 4
        assertEquals(Square(4), distance(pos1, pos2))
    }

    @Test
    fun `chebyshev distance should be symmetric`() {
        val pos1 = BoardPosition.from(1, 2)
        val pos2 = BoardPosition.from(4, 6)

        assertEquals(distance(pos2, pos1), distance(pos1, pos2))
    }

    @Test
    fun `chebyshev distance to same position should be 0`() {
        val pos = BoardPosition.from(5, 5)

        assertEquals(Square(0), distance(pos, pos))
    }

    @Test
    fun `chebyshev distance for L-shaped move should use larger component`() {
        val pos1 = BoardPosition.from(0, 0)
        val pos2 = BoardPosition.from(2, 5)

        // Can move diagonally 2 squares, then 3 more vertically = max(2, 5) = 5
        assertEquals(Square(5), distance(pos1, pos2))
    }

    @Test
    fun `toCoordinate should convert grid position to world coordinates`() {
        val pos = BoardPosition.from(2, 3)
        val coord = pos.toCoordinate()

        assertEquals(Feet(10.0), coord.x) // 2 * 5 feet
        assertEquals(Feet(15.0), coord.y) // 3 * 5 feet
    }

    @Test
    fun `toCoordinate for origin should return origin in feet`() {
        val pos = BoardPosition.from(0, 0)
        val coord = pos.toCoordinate()

        assertEquals(Feet(0.0), coord.x)
        assertEquals(Feet(0.0), coord.y)
    }

    @Test
    fun `fromCoordinate should convert world coordinates to grid position`() {
        val coord = Coordinate.from(10.0, 15.0)
        val pos = BoardPosition.fromCoordinate(coord)

        assertEquals(Square(2), pos.x) // 10 / 5
        assertEquals(Square(3), pos.y) // 15 / 5
    }

    @Test
    fun `fromCoordinate should round down to nearest grid square`() {
        val coord = Coordinate.from(12.5, 18.9)
        val pos = BoardPosition.fromCoordinate(coord)

        assertEquals(Square(2), pos.x) // 12.5 / 5 = 2.5 -> 2
        assertEquals(Square(3), pos.y) // 18.9 / 5 = 3.78 -> 3
    }

    @Test
    fun `fromCoordinate for origin should return origin grid position`() {
        val coord = Coordinate.from(0.0, 0.0)
        val pos = BoardPosition.fromCoordinate(coord)

        assertEquals(Square(0), pos.x)
        assertEquals(Square(0), pos.y)
    }

    @Test
    fun `toCoordinate and fromCoordinate should be consistent for aligned positions`() {
        val original = BoardPosition.from(5, 7)
        val coord = original.toCoordinate()
        val roundtrip = BoardPosition.fromCoordinate(coord)

        assertEquals(original, roundtrip)
    }

    @Test
    fun `chebyshev distance should work with negative differences`() {
        val pos1 = BoardPosition.from(5, 8)
        val pos2 = BoardPosition.from(2, 3)

        // Chebyshev distance = max(|5-2|, |8-3|) = max(3, 5) = 5
        assertEquals(Square(5), distance(pos1, pos2))
    }

    @Test
    fun `adding positions with negative components should work correctly`() {
        val pos1 = BoardPosition(Square(5), Square(3))
        val pos2 = BoardPosition(Square(-2), Square(-1))
        val result = pos1 + pos2

        assertEquals(Square(3), result.x)
        assertEquals(Square(2), result.y)
    }

    @Test
    fun `subtracting positions resulting in negative components should work correctly`() {
        val pos1 = BoardPosition.from(2, 3)
        val pos2 = BoardPosition.from(5, 7)
        val result = pos1 - pos2

        assertEquals(Square(-3), result.x)
        assertEquals(Square(-4), result.y)
    }
}
