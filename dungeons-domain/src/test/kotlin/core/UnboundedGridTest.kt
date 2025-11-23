package core

import org.junit.jupiter.api.Assertions.assertEquals
import io.dungeons.core.GridIndex
import io.dungeons.core.UnboundedGrid
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UnboundedGridTest {

    @Nested
    inner class GetAndSet {

        @Test
        fun `should get null for unset cell`() {
            val grid = UnboundedGrid<String>()

            assertEquals(null, grid[GridIndex(100, 200)])
        }

        @Test
        fun `should set and get value`() {
            val grid = UnboundedGrid<String>()

            grid[GridIndex(100, 200)] = "test"

            assertEquals("test", grid[GridIndex(100, 200)])
        }

        @Test
        fun `should work with negative coordinates`() {
            val grid = UnboundedGrid<Int>()

            grid[GridIndex(-1000, -2000)] = 42

            assertEquals(42, grid[GridIndex(-1000, -2000)])
        }

        @Test
        fun `should work with large coordinates`() {
            val grid = UnboundedGrid<Int>()

            grid[GridIndex(1000000, 2000000)] = 99

            assertEquals(99, grid[GridIndex(1000000, 2000000)])
        }
    }

    @Nested
    inner class Remove {

        @Test
        fun `should remove and return value`() {
            val grid = UnboundedGrid<String>()
            grid[GridIndex(10, 20)] = "test"

            val removed = grid.remove(GridIndex(10, 20))

            assertEquals("test", removed)
            assertEquals(null, grid[GridIndex(10, 20)])
        }

        @Test
        fun `should return null when removing unset cell`() {
            val grid = UnboundedGrid<String>()

            val removed = grid.remove(GridIndex(10, 20))

            assertEquals(null, removed)
        }
    }

    @Nested
    inner class IsEmpty {

        @Test
        fun `should return false for unset cell`() {
            val grid = UnboundedGrid<String>()

            assertEquals(false, grid.isEmpty(GridIndex(10, 20)))
        }

        @Test
        fun `should return true for set cell`() {
            val grid = UnboundedGrid<String>()
            grid[GridIndex(10, 20)] = "test"

            assertEquals(true, grid.isEmpty(GridIndex(10, 20)))
        }
    }

    @Nested
    inner class SparseStorage {

        @Test
        fun `should handle sparse data efficiently`() {
            val grid = UnboundedGrid<Int>()

            grid[GridIndex(0, 0)] = 1
            grid[GridIndex(1000, 1000)] = 2
            grid[GridIndex(-500, -500)] = 3

            assertEquals(1, grid[GridIndex(0, 0)])
            assertEquals(2, grid[GridIndex(1000, 1000)])
            assertEquals(3, grid[GridIndex(-500, -500)])
            assertEquals(null, grid[GridIndex(500, 500)])
        }
    }

    @Nested
    inner class BoundingBox {

        @Test
        fun `should return empty bounding box for empty grid`() {
            val grid = UnboundedGrid<Int>()

            val box = grid.boundingBox

            assertEquals(0, box.minX)
            assertEquals(0, box.minY)
            assertEquals(0, box.maxX)
            assertEquals(0, box.maxY)
        }

        @Test
        fun `should return bounding box for single cell`() {
            val grid = UnboundedGrid<Int>()
            grid[GridIndex(5, 10)] = 42

            val box = grid.boundingBox

            assertEquals(5, box.minX)
            assertEquals(10, box.minY)
            assertEquals(5, box.maxX)
            assertEquals(10, box.maxY)
        }

        @Test
        fun `should return bounding box for multiple cells`() {
            val grid = UnboundedGrid<Int>()
            grid[GridIndex(0, 0)] = 1
            grid[GridIndex(10, 20)] = 2
            grid[GridIndex(5, 15)] = 3

            val box = grid.boundingBox

            assertEquals(0, box.minX)
            assertEquals(0, box.minY)
            assertEquals(10, box.maxX)
            assertEquals(20, box.maxY)
        }

        @Test
        fun `should handle negative coordinates`() {
            val grid = UnboundedGrid<Int>()
            grid[GridIndex(-10, -20)] = 1
            grid[GridIndex(10, 20)] = 2

            val box = grid.boundingBox

            assertEquals(-10, box.minX)
            assertEquals(-20, box.minY)
            assertEquals(10, box.maxX)
            assertEquals(20, box.maxY)
        }

        @Test
        fun `should update bounding box when cells are added`() {
            val grid = UnboundedGrid<Int>()
            grid[GridIndex(0, 0)] = 1

            val box1 = grid.boundingBox
            assertEquals(0, box1.maxX)
            assertEquals(0, box1.maxY)

            grid[GridIndex(100, 100)] = 2

            val box2 = grid.boundingBox
            assertEquals(100, box2.maxX)
            assertEquals(100, box2.maxY)
        }

        @Test
        fun `should update bounding box when cells are removed`() {
            val grid = UnboundedGrid<Int>()
            grid[GridIndex(0, 0)] = 1
            grid[GridIndex(100, 100)] = 2

            val box1 = grid.boundingBox
            assertEquals(100, box1.maxX)
            assertEquals(100, box1.maxY)

            grid.remove(GridIndex(100, 100))

            val box2 = grid.boundingBox
            assertEquals(0, box2.maxX)
            assertEquals(0, box2.maxY)
        }
    }
}
