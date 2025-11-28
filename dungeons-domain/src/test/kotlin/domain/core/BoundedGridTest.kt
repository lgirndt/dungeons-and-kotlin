package domain.core

import io.dungeons.domain.core.BoundedGrid
import io.dungeons.domain.core.GridIndex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BoundedGridTest {
    @Nested
    inner class Construction {
        @Test
        fun `should create grid with default min bounds`() {
            val grid = BoundedGrid<Int>(maxX = 10, maxY = 10)

            assertEquals(0, grid.minX)
            assertEquals(0, grid.minY)
            assertEquals(10, grid.maxX)
            assertEquals(10, grid.maxY)
        }

        @Test
        fun `should create grid with custom min bounds`() {
            val grid = BoundedGrid<Int>(minX = 5, minY = 5, maxX = 10, maxY = 10)

            assertEquals(5, grid.minX)
            assertEquals(5, grid.minY)
            assertEquals(10, grid.maxX)
            assertEquals(10, grid.maxY)
        }

        @Test
        fun `should support negative coordinates`() {
            val grid = BoundedGrid<Int>(minX = -10, minY = -10, maxX = 10, maxY = 10)

            assertEquals(-10, grid.minX)
            assertEquals(-10, grid.minY)
            assertEquals(10, grid.maxX)
            assertEquals(10, grid.maxY)
        }

        @Test
        fun `should allow when maxX equals minX`() {
            val grid = BoundedGrid<Int>(minX = 5, maxX = 5, maxY = 10)
            assertEquals(5, grid.minX)
            assertEquals(5, grid.maxX)
        }

        @Test
        fun `should allow when maxY equals minY`() {
            val grid = BoundedGrid<Int>(maxX = 10, minY = 5, maxY = 5)
            assertEquals(5, grid.minY)
            assertEquals(5, grid.maxY)
        }
    }

    @Nested
    inner class GetAndSet {
        @Test
        fun `should get null for unset cell`() {
            val grid = BoundedGrid<String>(maxX = 5, maxY = 5)

            assertEquals(null, grid[GridIndex(2, 3)])
        }

        @Test
        fun `should set and get value`() {
            val grid = BoundedGrid<String>(maxX = 5, maxY = 5)

            grid[GridIndex(2, 3)] = "test"

            assertEquals("test", grid[GridIndex(2, 3)])
        }

        @Test
        fun `should work with offset bounds`() {
            val grid = BoundedGrid<Int>(minX = 10, minY = 10, maxX = 20, maxY = 20)

            grid[GridIndex(15, 15)] = 42

            assertEquals(42, grid[GridIndex(15, 15)])
        }

        @Test
        fun `should work with negative coordinates`() {
            val grid = BoundedGrid<Int>(minX = -10, minY = -10, maxX = 10, maxY = 10)

            grid[GridIndex(-5, -5)] = 42

            assertEquals(42, grid[GridIndex(-5, -5)])
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

            assertEquals("test", removed)
            assertEquals(null, grid[GridIndex(2, 3)])
        }

        @Test
        fun `should return null when removing unset cell`() {
            val grid = BoundedGrid<String>(maxX = 5, maxY = 5)

            val removed = grid.remove(GridIndex(2, 3))

            assertEquals(null, removed)
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

            assertEquals(false, grid.isEmpty(GridIndex(2, 3)))
        }

        @Test
        fun `should return true for set cell`() {
            val grid = BoundedGrid<String>(maxX = 5, maxY = 5)
            grid[GridIndex(2, 3)] = "test"

            assertEquals(true, grid.isEmpty(GridIndex(2, 3)))
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

            assertEquals(true, grid.isInBounds(GridIndex(0, 0)))
        }

        @Test
        fun `should return true for position at max bounds`() {
            val grid = BoundedGrid<Int>(maxX = 5, maxY = 5)

            assertEquals(true, grid.isInBounds(GridIndex(5, 5)))
        }

        @Test
        fun `should return false for position beyond max bounds`() {
            val grid = BoundedGrid<Int>(maxX = 5, maxY = 5)

            assertEquals(false, grid.isInBounds(GridIndex(6, 6)))
        }

        @Test
        fun `should work with offset bounds`() {
            val grid = BoundedGrid<Int>(minX = 10, minY = 10, maxX = 20, maxY = 20)

            assertEquals(true, grid.isInBounds(GridIndex(10, 10)))
            assertEquals(true, grid.isInBounds(GridIndex(15, 15)))
            assertEquals(true, grid.isInBounds(GridIndex(20, 20)))
            assertEquals(false, grid.isInBounds(GridIndex(9, 15)))
            assertEquals(false, grid.isInBounds(GridIndex(21, 15)))
        }

        @Test
        fun `should work with negative coordinates`() {
            val grid = BoundedGrid<Int>(minX = -10, minY = -10, maxX = 10, maxY = 10)

            assertEquals(true, grid.isInBounds(GridIndex(-10, -10)))
            assertEquals(true, grid.isInBounds(GridIndex(-5, -5)))
            assertEquals(true, grid.isInBounds(GridIndex(0, 0)))
            assertEquals(true, grid.isInBounds(GridIndex(10, 10)))
            assertEquals(false, grid.isInBounds(GridIndex(-11, 0)))
            assertEquals(false, grid.isInBounds(GridIndex(11, 0)))
        }
    }

    @Nested
    inner class BoundingBox {
        @Test
        fun `should return grid dimensions as bounding box`() {
            val grid = BoundedGrid<Int>(maxX = 10, maxY = 20)

            val box = grid.boundingBox

            assertEquals(0, box.minX)
            assertEquals(0, box.minY)
            assertEquals(10, box.maxX)
            assertEquals(20, box.maxY)
        }

        @Test
        fun `should return custom min bounds in bounding box`() {
            val grid = BoundedGrid<Int>(minX = 5, minY = 10, maxX = 15, maxY = 30)

            val box = grid.boundingBox

            assertEquals(5, box.minX)
            assertEquals(10, box.minY)
            assertEquals(15, box.maxX)
            assertEquals(30, box.maxY)
        }

        @Test
        fun `should return negative bounds in bounding box`() {
            val grid = BoundedGrid<Int>(minX = -10, minY = -20, maxX = 10, maxY = 20)

            val box = grid.boundingBox

            assertEquals(-10, box.minX)
            assertEquals(-20, box.minY)
            assertEquals(10, box.maxX)
            assertEquals(20, box.maxY)
        }
    }
}
