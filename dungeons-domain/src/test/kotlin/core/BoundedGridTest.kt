package io.dungeons.core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BoundedGridTest {

    @Nested
    inner class Construction {

        @Test
        fun `should create grid with default min bounds`() {
            val grid = BoundedGrid<Int>(maxX = 10, maxY = 10)

            assertThat(grid.minX, equalTo(0))
            assertThat(grid.minY, equalTo(0))
            assertThat(grid.maxX, equalTo(10))
            assertThat(grid.maxY, equalTo(10))
        }

        @Test
        fun `should create grid with custom min bounds`() {
            val grid = BoundedGrid<Int>(minX = 5, minY = 5, maxX = 10, maxY = 10)

            assertThat(grid.minX, equalTo(5))
            assertThat(grid.minY, equalTo(5))
            assertThat(grid.maxX, equalTo(10))
            assertThat(grid.maxY, equalTo(10))
        }

        @Test
        fun `should support negative coordinates`() {
            val grid = BoundedGrid<Int>(minX = -10, minY = -10, maxX = 10, maxY = 10)

            assertThat(grid.minX, equalTo(-10))
            assertThat(grid.minY, equalTo(-10))
            assertThat(grid.maxX, equalTo(10))
            assertThat(grid.maxY, equalTo(10))
        }

        @Test
        fun `should allow when maxX equals minX`() {
            val grid = BoundedGrid<Int>(minX = 5, maxX = 5, maxY = 10)
            assertThat(grid.minX, equalTo(5))
            assertThat(grid.maxX, equalTo(5))
        }

        @Test
        fun `should allow when maxY equals minY`() {
            val grid = BoundedGrid<Int>(maxX = 10, minY = 5, maxY = 5)
            assertThat(grid.minY, equalTo(5))
            assertThat(grid.maxY, equalTo(5))
        }
    }

    @Nested
    inner class GetAndSet {

        @Test
        fun `should get null for unset cell`() {
            val grid = BoundedGrid<String>(maxX = 5, maxY = 5)

            assertThat(grid[GridIndex(2, 3)], equalTo(null))
        }

        @Test
        fun `should set and get value`() {
            val grid = BoundedGrid<String>(maxX = 5, maxY = 5)

            grid[GridIndex(2, 3)] = "test"

            assertThat(grid[GridIndex(2, 3)], equalTo("test"))
        }

        @Test
        fun `should work with offset bounds`() {
            val grid = BoundedGrid<Int>(minX = 10, minY = 10, maxX = 20, maxY = 20)

            grid[GridIndex(15, 15)] = 42

            assertThat(grid[GridIndex(15, 15)], equalTo(42))
        }

        @Test
        fun `should work with negative coordinates`() {
            val grid = BoundedGrid<Int>(minX = -10, minY = -10, maxX = 10, maxY = 10)

            grid[GridIndex(-5, -5)] = 42

            assertThat(grid[GridIndex(-5, -5)], equalTo(42))
        }

        @Test
        fun `should throw when getting out of bounds position`() {
            val grid = BoundedGrid<Int>(maxX = 5, maxY = 5)

            assertThrows<IllegalArgumentException> {
                grid[GridIndex(6, 2)]
            }
        }

        @Test
        fun `should throw when setting out of bounds position`() {
            val grid = BoundedGrid<Int>(maxX = 5, maxY = 5)

            assertThrows<IllegalArgumentException> {
                grid[GridIndex(2, 6)] = 42
            }
        }
    }

    @Nested
    inner class Remove {

        @Test
        fun `should remove and return value`() {
            val grid = BoundedGrid<String>(maxX = 5, maxY = 5)
            grid[GridIndex(2, 3)] = "test"

            val removed = grid.remove(GridIndex(2, 3))

            assertThat(removed, equalTo("test"))
            assertThat(grid[GridIndex(2, 3)], equalTo(null))
        }

        @Test
        fun `should return null when removing unset cell`() {
            val grid = BoundedGrid<String>(maxX = 5, maxY = 5)

            val removed = grid.remove(GridIndex(2, 3))

            assertThat(removed, equalTo(null))
        }

        @Test
        fun `should throw when removing out of bounds position`() {
            val grid = BoundedGrid<Int>(maxX = 5, maxY = 5)

            assertThrows<IllegalArgumentException> {
                grid.remove(GridIndex(6, 2))
            }
        }
    }

    @Nested
    inner class IsEmpty {

        @Test
        fun `should return false for unset cell`() {
            val grid = BoundedGrid<String>(maxX = 5, maxY = 5)

            assertThat(grid.isEmpty(GridIndex(2, 3)), equalTo(false))
        }

        @Test
        fun `should return true for set cell`() {
            val grid = BoundedGrid<String>(maxX = 5, maxY = 5)
            grid[GridIndex(2, 3)] = "test"

            assertThat(grid.isEmpty(GridIndex(2, 3)), equalTo(true))
        }

        @Test
        fun `should throw when checking out of bounds position`() {
            val grid = BoundedGrid<Int>(maxX = 5, maxY = 5)

            assertThrows<IllegalArgumentException> {
                grid.isEmpty(GridIndex(6, 2))
            }
        }
    }

    @Nested
    inner class IsInBounds {

        @Test
        fun `should return true for position at min bounds`() {
            val grid = BoundedGrid<Int>(maxX = 5, maxY = 5)

            assertThat(grid.isInBounds(GridIndex(0, 0)), equalTo(true))
        }

        @Test
        fun `should return true for position at max bounds`() {
            val grid = BoundedGrid<Int>(maxX = 5, maxY = 5)

            assertThat(grid.isInBounds(GridIndex(5, 5)), equalTo(true))
        }

        @Test
        fun `should return false for position beyond max bounds`() {
            val grid = BoundedGrid<Int>(maxX = 5, maxY = 5)

            assertThat(grid.isInBounds(GridIndex(6, 6)), equalTo(false))
        }

        @Test
        fun `should work with offset bounds`() {
            val grid = BoundedGrid<Int>(minX = 10, minY = 10, maxX = 20, maxY = 20)

            assertThat(grid.isInBounds(GridIndex(10, 10)), equalTo(true))
            assertThat(grid.isInBounds(GridIndex(15, 15)), equalTo(true))
            assertThat(grid.isInBounds(GridIndex(20, 20)), equalTo(true))
            assertThat(grid.isInBounds(GridIndex(9, 15)), equalTo(false))
            assertThat(grid.isInBounds(GridIndex(21, 15)), equalTo(false))
        }

        @Test
        fun `should work with negative coordinates`() {
            val grid = BoundedGrid<Int>(minX = -10, minY = -10, maxX = 10, maxY = 10)

            assertThat(grid.isInBounds(GridIndex(-10, -10)), equalTo(true))
            assertThat(grid.isInBounds(GridIndex(-5, -5)), equalTo(true))
            assertThat(grid.isInBounds(GridIndex(0, 0)), equalTo(true))
            assertThat(grid.isInBounds(GridIndex(10, 10)), equalTo(true))
            assertThat(grid.isInBounds(GridIndex(-11, 0)), equalTo(false))
            assertThat(grid.isInBounds(GridIndex(11, 0)), equalTo(false))
        }
    }

    @Nested
    inner class BoundingBox {

        @Test
        fun `should return grid dimensions as bounding box`() {
            val grid = BoundedGrid<Int>(maxX = 10, maxY = 20)

            val box = grid.boundingBox

            assertThat(box.minX, equalTo(0))
            assertThat(box.minY, equalTo(0))
            assertThat(box.maxX, equalTo(10))
            assertThat(box.maxY, equalTo(20))
        }

        @Test
        fun `should return custom min bounds in bounding box`() {
            val grid = BoundedGrid<Int>(minX = 5, minY = 10, maxX = 15, maxY = 30)

            val box = grid.boundingBox

            assertThat(box.minX, equalTo(5))
            assertThat(box.minY, equalTo(10))
            assertThat(box.maxX, equalTo(15))
            assertThat(box.maxY, equalTo(30))
        }

        @Test
        fun `should return negative bounds in bounding box`() {
            val grid = BoundedGrid<Int>(minX = -10, minY = -20, maxX = 10, maxY = 20)

            val box = grid.boundingBox

            assertThat(box.minX, equalTo(-10))
            assertThat(box.minY, equalTo(-20))
            assertThat(box.maxX, equalTo(10))
            assertThat(box.maxY, equalTo(20))
        }
    }
}