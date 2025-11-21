package io.dungeons.core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BooleanGridTest {

    @Nested
    inner class GetAndSet {

        @Test
        fun `should return false for unset positions`() {
            val grid = BooleanGrid(maxX = 10, maxY = 10)

            assertThat(grid[GridIndex(5, 5)], equalTo(false))
        }

        @Test
        fun `should return true for set positions`() {
            val grid = BooleanGrid(maxX = 10, maxY = 10)

            grid[GridIndex(5, 5)] = true

            assertThat(grid[GridIndex(5, 5)], equalTo(true))
        }

        @Test
        fun `should return false after setting to false`() {
            val grid = BooleanGrid(maxX = 10, maxY = 10)
            grid[GridIndex(5, 5)] = true

            grid[GridIndex(5, 5)] = false

            assertThat(grid[GridIndex(5, 5)], equalTo(false))
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

            assertThat(result[GridIndex(1, 1)], equalTo(true))
            assertThat(result[GridIndex(2, 2)], equalTo(true))
            assertThat(result[GridIndex(3, 3)], equalTo(true))
            assertThat(result[GridIndex(4, 4)], equalTo(false))
        }

        @Test
        fun `should expand bounds to accommodate both grids`() {
            val grid1 = BooleanGrid(minX = 0, minY = 0, maxX = 5, maxY = 5)
            grid1[GridIndex(1, 1)] = true

            val grid2 = BooleanGrid(minX = 5, minY = 5, maxX = 10, maxY = 10)
            grid2[GridIndex(8, 8)] = true

            val result = grid1.union(grid2)

            assertThat(result.minX, equalTo(0))
            assertThat(result.minY, equalTo(0))
            assertThat(result.maxX, equalTo(10))
            assertThat(result.maxY, equalTo(10))
            assertThat(result[GridIndex(1, 1)], equalTo(true))
            assertThat(result[GridIndex(8, 8)], equalTo(true))
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

            assertThat(result[GridIndex(1, 1)], equalTo(false)) // Only in grid1
            assertThat(result[GridIndex(2, 2)], equalTo(true))  // In both
            assertThat(result[GridIndex(3, 3)], equalTo(true))  // In both
            assertThat(result[GridIndex(4, 4)], equalTo(false)) // Only in grid2
        }

        @Test
        fun `should shrink bounds to overlapping region`() {
            val grid1 = BooleanGrid(minX = 0, minY = 0, maxX = 10, maxY = 10)
            grid1[GridIndex(5, 5)] = true

            val grid2 = BooleanGrid(minX = 5, minY = 5, maxX = 15, maxY = 15)
            grid2[GridIndex(5, 5)] = true

            val result = grid1.intersect(grid2)

            assertThat(result.minX, equalTo(5))
            assertThat(result.minY, equalTo(5))
            assertThat(result.maxX, equalTo(10))
            assertThat(result.maxY, equalTo(10))
            assertThat(result[GridIndex(5, 5)], equalTo(true))
        }

        @Test
        fun `should return empty intersection when grids do not overlap`() {
            val grid1 = BooleanGrid(minX = 0, minY = 0, maxX = 5, maxY = 5)
            grid1[GridIndex(2, 2)] = true

            val grid2 = BooleanGrid(minX = 0, minY = 0, maxX = 5, maxY = 5)
            grid2[GridIndex(3, 3)] = true

            val result = grid1.intersect(grid2)

            assertThat(result[GridIndex(2, 2)], equalTo(false))
            assertThat(result[GridIndex(3, 3)], equalTo(false))
        }
    }

    @Nested
    inner class BoundsHandling {

        @Test
        fun `should work with custom bounds`() {
            val grid = BooleanGrid(minX = 10, minY = 10, maxX = 20, maxY = 20)

            grid[GridIndex(15, 15)] = true

            assertThat(grid[GridIndex(15, 15)], equalTo(true))
        }

        @Test
        fun `should work with negative coordinates`() {
            val grid = BooleanGrid(minX = -10, minY = -10, maxX = 10, maxY = 10)

            grid[GridIndex(-5, -5)] = true

            assertThat(grid[GridIndex(-5, -5)], equalTo(true))
        }
    }
}
