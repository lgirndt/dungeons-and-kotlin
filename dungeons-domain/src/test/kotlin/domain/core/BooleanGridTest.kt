package domain.core

import io.dungeons.domain.core.BooleanGrid
import io.dungeons.domain.core.BoundingBox
import io.dungeons.domain.core.GridIndex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BooleanGridTest {
    @Nested
    inner class GetAndSet {
        @Test
        fun `should return false for unset positions`() {
            val grid = BooleanGrid(maxX = 10, maxY = 10)

            assertEquals(false, grid[GridIndex(5, 5)])
        }

        @Test
        fun `should return true for set positions`() {
            val grid = BooleanGrid(maxX = 10, maxY = 10)

            grid[GridIndex(5, 5)] = true

            assertEquals(true, grid[GridIndex(5, 5)])
        }

        @Test
        fun `should return false after setting to false`() {
            val grid = BooleanGrid(maxX = 10, maxY = 10)
            grid[GridIndex(5, 5)] = true

            grid[GridIndex(5, 5)] = false

            assertEquals(false, grid[GridIndex(5, 5)])
        }
    }

    @Nested
    inner class Union {
        @Test
        fun `should combine two grids with OR logic`() {
            val grid1 = BooleanGrid(maxX = 5, maxY = 5)
            grid1[GridIndex(1, 1)] = true
            grid1[GridIndex(2, 2)] = true

            val grid2 = BooleanGrid(maxX = 5, maxY = 5)
            grid2[GridIndex(2, 2)] = true
            grid2[GridIndex(3, 3)] = true

            val result = grid1.union(grid2)

            assertEquals(true, result[GridIndex(1, 1)])
            assertEquals(true, result[GridIndex(2, 2)])
            assertEquals(true, result[GridIndex(3, 3)])
            assertEquals(false, result[GridIndex(4, 4)])
        }

        @Test
        fun `should expand bounds to accommodate both grids`() {
            val grid1 = BooleanGrid(minX = 0, minY = 0, maxX = 5, maxY = 5)
            grid1[GridIndex(1, 1)] = true

            val grid2 = BooleanGrid(minX = 5, minY = 5, maxX = 10, maxY = 10)
            grid2[GridIndex(8, 8)] = true

            val result = grid1.union(grid2)

            assertEquals(BoundingBox(0, 0, 10, 10), result.boundingBox)
            assertEquals(true, result[GridIndex(1, 1)])
            assertEquals(true, result[GridIndex(8, 8)])
        }
    }

    @Nested
    inner class Intersect {
        @Test
        fun `should combine two grids with AND logic`() {
            val grid1 = BooleanGrid(maxX = 5, maxY = 5)
            grid1[GridIndex(1, 1)] = true
            grid1[GridIndex(2, 2)] = true
            grid1[GridIndex(3, 3)] = true

            val grid2 = BooleanGrid(maxX = 5, maxY = 5)
            grid2[GridIndex(2, 2)] = true
            grid2[GridIndex(3, 3)] = true
            grid2[GridIndex(4, 4)] = true

            val result = grid1.intersect(grid2)

            assertEquals(false, result[GridIndex(1, 1)]) // Only in grid1
            assertEquals(true, result[GridIndex(2, 2)]) // In both
            assertEquals(true, result[GridIndex(3, 3)]) // In both
            assertEquals(false, result[GridIndex(4, 4)]) // Only in grid2
        }

        @Test
        fun `should shrink bounds to overlapping region`() {
            val grid1 = BooleanGrid(minX = 0, minY = 0, maxX = 10, maxY = 10)
            grid1[GridIndex(5, 5)] = true

            val grid2 = BooleanGrid(minX = 5, minY = 5, maxX = 15, maxY = 15)
            grid2[GridIndex(5, 5)] = true

            val result = grid1.intersect(grid2)

            assertEquals(BoundingBox(5, 5, 10, 10), result.boundingBox)
            assertEquals(true, result[GridIndex(5, 5)])
        }

        @Test
        fun `should return empty intersection when grids do not overlap`() {
            val grid1 = BooleanGrid(minX = 0, minY = 0, maxX = 5, maxY = 5)
            grid1[GridIndex(2, 2)] = true

            val grid2 = BooleanGrid(minX = 0, minY = 0, maxX = 5, maxY = 5)
            grid2[GridIndex(3, 3)] = true

            val result = grid1.intersect(grid2)

            assertEquals(false, result[GridIndex(2, 2)])
            assertEquals(false, result[GridIndex(3, 3)])
        }
    }

    @Nested
    inner class BoundsHandling {
        @Test
        fun `should work with custom bounds`() {
            val grid = BooleanGrid(minX = 10, minY = 10, maxX = 20, maxY = 20)

            grid[GridIndex(15, 15)] = true

            assertEquals(true, grid[GridIndex(15, 15)])
        }

        @Test
        fun `should work with negative coordinates`() {
            val grid = BooleanGrid(minX = -10, minY = -10, maxX = 10, maxY = 10)

            grid[GridIndex(-5, -5)] = true

            assertEquals(true, grid[GridIndex(-5, -5)])
        }
    }
}
