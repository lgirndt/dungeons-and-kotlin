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
            val grid = Grid(3, 4, 0)

            assertThat(grid.width, equalTo(3))
            assertThat(grid.height, equalTo(4))
        }

        @Test
        fun `should initialize all cells with initial value`() {
            val grid = Grid(3, 3, 42)

            for (y in 0 until 3) {
                for (x in 0 until 3) {
                    assertThat(grid[GridCell(x, y)], equalTo(42))
                }
            }
        }

        @Test
        fun `should initialize string grid correctly`() {
            val grid = Grid(2, 2, "empty")

            assertThat(grid[GridCell(0, 0)], equalTo("empty"))
            assertThat(grid[GridCell(1, 1)], equalTo("empty"))
        }

        @Test
        fun `should reject zero width`() {
            val exception = assertThrows<IllegalArgumentException> {
                Grid(0, 5, "test")
            }
            assertThat(exception.message, equalTo("width must be positive, but was 0"))
        }

        @Test
        fun `should reject negative width`() {
            val exception = assertThrows<IllegalArgumentException> {
                Grid(-1, 5, "test")
            }
            assertThat(exception.message, equalTo("width must be positive, but was -1"))
        }

        @Test
        fun `should reject zero height`() {
            val exception = assertThrows<IllegalArgumentException> {
                Grid(5, 0, "test")
            }
            assertThat(exception.message, equalTo("height must be positive, but was 0"))
        }

        @Test
        fun `should reject negative height`() {
            val exception = assertThrows<IllegalArgumentException> {
                Grid(5, -1, "test")
            }
            assertThat(exception.message, equalTo("height must be positive, but was -1"))
        }

        @Test
        fun `should create 1x1 grid`() {
            val grid = Grid(1, 1, 99)

            assertThat(grid[GridCell(0, 0)], equalTo(99))
        }

        @Test
        fun `should create large grid`() {
            val grid = Grid(100, 100, 0)

            assertThat(grid.width, equalTo(100))
            assertThat(grid.height, equalTo(100))
        }
    }

    @Nested
    inner class GetOperation {

        @Test
        fun `should get value at origin`() {
            val grid = Grid(5, 5, "initial")

            assertThat(grid[GridCell(0, 0)], equalTo("initial"))
        }

        @Test
        fun `should get value at top-right corner`() {
            val grid = Grid(5, 5, 123)

            assertThat(grid[GridCell(4, 0)], equalTo(123))
        }

        @Test
        fun `should get value at bottom-left corner`() {
            val grid = Grid(5, 5, 123)

            assertThat(grid[GridCell(0, 4)], equalTo(123))
        }

        @Test
        fun `should get value at bottom-right corner`() {
            val grid = Grid(5, 5, 123)

            assertThat(grid[GridCell(4, 4)], equalTo(123))
        }

        @Test
        fun `should get value in middle of grid`() {
            val grid = Grid(5, 5, 'X')

            assertThat(grid[GridCell(2, 2)], equalTo('X'))
        }

        @Test
        fun `should throw when x is out of bounds to the right`() {
            val grid = Grid(3, 3, 0)

            val exception = assertThrows<IllegalArgumentException> {
                grid[GridCell(3, 1)]
            }
        }

        @Test
        fun `should throw when y is out of bounds to the bottom`() {
            val grid = Grid(3, 3, 0)

            val exception = assertThrows<IllegalArgumentException> {
                grid[GridCell(1, 3)]
            }
        }
    }

    @Nested
    inner class SetOperation {

        @Test
        fun `should set value at origin`() {
            val grid = Grid(5, 5, 0)

            grid[GridCell(0, 0)] = 42

            assertThat(grid[GridCell(0, 0)], equalTo(42))
        }

        @Test
        fun `should set value at specific position`() {
            val grid = Grid(5, 5, "empty")

            grid[GridCell(2, 3)] = "occupied"

            assertThat(grid[GridCell(2, 3)], equalTo("occupied"))
        }


        @Test
        fun `should overwrite existing value`() {
            val grid = Grid(3, 3, 10)

            grid[GridCell(1, 1)] = 20
            grid[GridCell(1, 1)] = 30

            assertThat(grid[GridCell(1, 1)], equalTo(30))
        }


        @Test
        fun `should throw when setting x out of bounds`() {
            val grid = Grid(3, 3, 0)

            val exception = assertThrows<IllegalArgumentException> {
                grid[GridCell(3, 1)] = 42
            }
        }

        @Test
        fun `should throw when setting y out of bounds`() {
            val grid = Grid(3, 3, 0)

            val exception = assertThrows<IllegalArgumentException> {
                grid[GridCell(1, 3)] = 42
            }
        }
    }

    @Nested
    inner class IsInBounds {

        @Test
        fun `should return true for origin`() {
            val grid = Grid(5, 5, 0)

            assertThat(grid.isInBounds(GridCell(0, 0)), equalTo(true))
        }

        @Test
        fun `should return true for valid position in middle`() {
            val grid = Grid(5, 5, 0)

            assertThat(grid.isInBounds(GridCell(2, 3)), equalTo(true))
        }

        @Test
        fun `should return true for top-right corner`() {
            val grid = Grid(5, 5, 0)

            assertThat(grid.isInBounds(GridCell(4, 0)), equalTo(true))
        }

        @Test
        fun `should return true for bottom-left corner`() {
            val grid = Grid(5, 5, 0)

            assertThat(grid.isInBounds(GridCell(0, 4)), equalTo(true))
        }

        @Test
        fun `should return true for bottom-right corner`() {
            val grid = Grid(5, 5, 0)

            assertThat(grid.isInBounds(GridCell(4, 4)), equalTo(true))
        }

        @Test
        fun `should return false when x is out of bounds to the right`() {
            val grid = Grid(5, 5, 0)

            assertThat(grid.isInBounds(GridCell(5, 2)), equalTo(false))
        }

        @Test
        fun `should return false when y is out of bounds to the bottom`() {
            val grid = Grid(5, 5, 0)

            assertThat(grid.isInBounds(GridCell(2, 5)), equalTo(false))
        }

        @Test
        fun `should return false when both coordinates are out of bounds`() {
            val grid = Grid(5, 5, 0)

            assertThat(grid.isInBounds(GridCell(10, 10)), equalTo(false))
        }

        @Test
        fun `should return false for way out of bounds position`() {
            val grid = Grid(5, 5, 0)

            assertThat(grid.isInBounds(GridCell(100, 100)), equalTo(false))
        }
    }

    @Nested
    inner class ComplexScenarios {

        @Test
        fun `should work correctly with nullable types`() {
            val grid = Grid<String?>(3, 3, null)

            assertThat(grid[GridCell(0, 0)], equalTo(null))

            grid[GridCell(1, 1)] = "value"
            assertThat(grid[GridCell(1, 1)], equalTo("value"))
        }

        @Test
        fun `should work with complex objects`() {
            data class Cell(val type: String, val value: Int)

            val initial = Cell("empty", 0)
            val grid = Grid(3, 3, initial)

            val occupied = Cell("occupied", 42)
            grid[GridCell(1, 1)] = occupied

            assertThat(grid[GridCell(1, 1)], equalTo(occupied))
            assertThat(grid[GridCell(0, 0)], equalTo(initial))
        }

    }
}