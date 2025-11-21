package io.dungeons.core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GridTest {

    @Nested
    inner class Construction {

        @Test
        fun `should create grid with specified dimensions`() {
            val grid = Grid<Int>(3, 4)

            assertThat(grid.width, equalTo(3))
            assertThat(grid.height, equalTo(4))
        }

        @Test
        fun `should initialize all cells as null`() {
            val grid = Grid<Int>(3, 3)

            for (y in 0 until 3) {
                for (x in 0 until 3) {
                    assertThat(grid[GridIndex(x, y)], equalTo(null))
                }
            }
        }

        @Test
        fun `should reject zero width`() {
            val exception = assertThrows<IllegalArgumentException> {
                Grid<String>(0, 5)
            }
            assertThat(exception.message, equalTo("width must be positive, but was 0"))
        }

        @Test
        fun `should reject negative width`() {
            val exception = assertThrows<IllegalArgumentException> {
                Grid<String>(-1, 5)
            }
            assertThat(exception.message, equalTo("width must be positive, but was -1"))
        }

        @Test
        fun `should reject zero height`() {
            val exception = assertThrows<IllegalArgumentException> {
                Grid<String>(5, 0)
            }
            assertThat(exception.message, equalTo("height must be positive, but was 0"))
        }

        @Test
        fun `should reject negative height`() {
            val exception = assertThrows<IllegalArgumentException> {
                Grid<String>(5, -1)
            }
            assertThat(exception.message, equalTo("height must be positive, but was -1"))
        }

        @Test
        fun `should create 1x1 grid`() {
            val grid = Grid<Int>(1, 1)

            assertThat(grid[GridIndex(0, 0)], equalTo(null))
        }

        @Test
        fun `should create large grid`() {
            val grid = Grid<Int>(100, 100)

            assertThat(grid.width, equalTo(100))
            assertThat(grid.height, equalTo(100))
        }
    }

    @Nested
    inner class GetOperation {

        @Test
        fun `should get null at origin when not set`() {
            val grid = Grid<String>(5, 5)

            assertThat(grid[GridIndex(0, 0)], equalTo(null))
        }

        @Test
        fun `should get value after setting it`() {
            val grid = Grid<Int>(5, 5)
            grid[GridIndex(4, 0)] = 123

            assertThat(grid[GridIndex(4, 0)], equalTo(123))
        }

        @Test
        fun `should get null when not set at bottom-left corner`() {
            val grid = Grid<Int>(5, 5)

            assertThat(grid[GridIndex(0, 4)], equalTo(null))
        }

        @Test
        fun `should get value at bottom-right corner after setting`() {
            val grid = Grid<Int>(5, 5)
            grid[GridIndex(4, 4)] = 123

            assertThat(grid[GridIndex(4, 4)], equalTo(123))
        }

        @Test
        fun `should get value in middle of grid after setting`() {
            val grid = Grid<Char>(5, 5)
            grid[GridIndex(2, 2)] = 'X'

            assertThat(grid[GridIndex(2, 2)], equalTo('X'))
        }

        @Test
        fun `should throw when x is out of bounds to the right`() {
            val grid = Grid<Int>(3, 3)

            val exception = assertThrows<IllegalArgumentException> {
                grid[GridIndex(3, 1)]
            }
        }

        @Test
        fun `should throw when y is out of bounds to the bottom`() {
            val grid = Grid<Int>(3, 3)

            val exception = assertThrows<IllegalArgumentException> {
                grid[GridIndex(1, 3)]
            }
        }
    }

    @Nested
    inner class SetOperation {

        @Test
        fun `should set value at origin`() {
            val grid = Grid<Int>(5, 5)

            grid[GridIndex(0, 0)] = 42

            assertThat(grid[GridIndex(0, 0)], equalTo(42))
        }

        @Test
        fun `should set value at specific position`() {
            val grid = Grid<String>(5, 5)

            grid[GridIndex(2, 3)] = "occupied"

            assertThat(grid[GridIndex(2, 3)], equalTo("occupied"))
        }


        @Test
        fun `should overwrite existing value`() {
            val grid = Grid<Int>(3, 3)

            grid[GridIndex(1, 1)] = 20
            grid[GridIndex(1, 1)] = 30

            assertThat(grid[GridIndex(1, 1)], equalTo(30))
        }


        @Test
        fun `should throw when setting x out of bounds`() {
            val grid = Grid<Int>(3, 3)

            val exception = assertThrows<IllegalArgumentException> {
                grid[GridIndex(3, 1)] = 42
            }
        }

        @Test
        fun `should throw when setting y out of bounds`() {
            val grid = Grid<Int>(3, 3)

            val exception = assertThrows<IllegalArgumentException> {
                grid[GridIndex(1, 3)] = 42
            }
        }
    }

    @Nested
    inner class IsInBounds {

        @Test
        fun `should return true for origin`() {
            val grid = Grid<Int>(5, 5)

            assertThat(grid.isInBounds(GridIndex(0, 0)), equalTo(true))
        }

        @Test
        fun `should return true for valid position in middle`() {
            val grid = Grid<Int>(5, 5)

            assertThat(grid.isInBounds(GridIndex(2, 3)), equalTo(true))
        }

        @Test
        fun `should return true for top-right corner`() {
            val grid = Grid<Int>(5, 5)

            assertThat(grid.isInBounds(GridIndex(4, 0)), equalTo(true))
        }

        @Test
        fun `should return true for bottom-left corner`() {
            val grid = Grid<Int>(5, 5)

            assertThat(grid.isInBounds(GridIndex(0, 4)), equalTo(true))
        }

        @Test
        fun `should return true for bottom-right corner`() {
            val grid = Grid<Int>(5, 5)

            assertThat(grid.isInBounds(GridIndex(4, 4)), equalTo(true))
        }

        @Test
        fun `should return false when x is out of bounds to the right`() {
            val grid = Grid<Int>(5, 5)

            assertThat(grid.isInBounds(GridIndex(5, 2)), equalTo(false))
        }

        @Test
        fun `should return false when y is out of bounds to the bottom`() {
            val grid = Grid<Int>(5, 5)

            assertThat(grid.isInBounds(GridIndex(2, 5)), equalTo(false))
        }

        @Test
        fun `should return false when both coordinates are out of bounds`() {
            val grid = Grid<Int>(5, 5)

            assertThat(grid.isInBounds(GridIndex(10, 10)), equalTo(false))
        }

        @Test
        fun `should return false for way out of bounds position`() {
            val grid = Grid<Int>(5, 5)

            assertThat(grid.isInBounds(GridIndex(100, 100)), equalTo(false))
        }
    }

    @Nested
    inner class ComplexScenarios {

        @Test
        fun `should work correctly with nullable types`() {
            val grid = Grid<String?>(3, 3)

            assertThat(grid[GridIndex(0, 0)], equalTo(null))

            grid[GridIndex(1, 1)] = "value"
            assertThat(grid[GridIndex(1, 1)], equalTo("value"))
        }

        @Test
        fun `should work with complex objects`() {
            data class Cell(val type: String, val value: Int)

            val grid = Grid<Cell>(3, 3)

            val occupied = Cell("occupied", 42)
            grid[GridIndex(1, 1)] = occupied

            assertThat(grid[GridIndex(1, 1)], equalTo(occupied))
            assertThat(grid[GridIndex(0, 0)], equalTo(null))
        }

    }
}