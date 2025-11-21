package core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
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

            assertThat(grid[GridIndex(100, 200)], equalTo(null))
        }

        @Test
        fun `should set and get value`() {
            val grid = UnboundedGrid<String>()

            grid[GridIndex(100, 200)] = "test"

            assertThat(grid[GridIndex(100, 200)], equalTo("test"))
        }

        @Test
        fun `should work with negative coordinates`() {
            val grid = UnboundedGrid<Int>()

            grid[GridIndex(-1000, -2000)] = 42

            assertThat(grid[GridIndex(-1000, -2000)], equalTo(42))
        }

        @Test
        fun `should work with large coordinates`() {
            val grid = UnboundedGrid<Int>()

            grid[GridIndex(1000000, 2000000)] = 99

            assertThat(grid[GridIndex(1000000, 2000000)], equalTo(99))
        }
    }

    @Nested
    inner class Remove {

        @Test
        fun `should remove and return value`() {
            val grid = UnboundedGrid<String>()
            grid[GridIndex(10, 20)] = "test"

            val removed = grid.remove(GridIndex(10, 20))

            assertThat(removed, equalTo("test"))
            assertThat(grid[GridIndex(10, 20)], equalTo(null))
        }

        @Test
        fun `should return null when removing unset cell`() {
            val grid = UnboundedGrid<String>()

            val removed = grid.remove(GridIndex(10, 20))

            assertThat(removed, equalTo(null))
        }
    }

    @Nested
    inner class IsEmpty {

        @Test
        fun `should return false for unset cell`() {
            val grid = UnboundedGrid<String>()

            assertThat(grid.isEmpty(GridIndex(10, 20)), equalTo(false))
        }

        @Test
        fun `should return true for set cell`() {
            val grid = UnboundedGrid<String>()
            grid[GridIndex(10, 20)] = "test"

            assertThat(grid.isEmpty(GridIndex(10, 20)), equalTo(true))
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

            assertThat(grid[GridIndex(0, 0)], equalTo(1))
            assertThat(grid[GridIndex(1000, 1000)], equalTo(2))
            assertThat(grid[GridIndex(-500, -500)], equalTo(3))
            assertThat(grid[GridIndex(500, 500)], equalTo(null))
        }
    }

    @Nested
    inner class BoundingBox {

        @Test
        fun `should return empty bounding box for empty grid`() {
            val grid = UnboundedGrid<Int>()

            val box = grid.boundingBox

            assertThat(box.minX, equalTo(0))
            assertThat(box.minY, equalTo(0))
            assertThat(box.maxX, equalTo(0))
            assertThat(box.maxY, equalTo(0))
        }

        @Test
        fun `should return bounding box for single cell`() {
            val grid = UnboundedGrid<Int>()
            grid[GridIndex(5, 10)] = 42

            val box = grid.boundingBox

            assertThat(box.minX, equalTo(5))
            assertThat(box.minY, equalTo(10))
            assertThat(box.maxX, equalTo(6))
            assertThat(box.maxY, equalTo(11))
        }

        @Test
        fun `should return bounding box for multiple cells`() {
            val grid = UnboundedGrid<Int>()
            grid[GridIndex(0, 0)] = 1
            grid[GridIndex(10, 20)] = 2
            grid[GridIndex(5, 15)] = 3

            val box = grid.boundingBox

            assertThat(box.minX, equalTo(0))
            assertThat(box.minY, equalTo(0))
            assertThat(box.maxX, equalTo(11))
            assertThat(box.maxY, equalTo(21))
        }

        @Test
        fun `should handle negative coordinates`() {
            val grid = UnboundedGrid<Int>()
            grid[GridIndex(-10, -20)] = 1
            grid[GridIndex(10, 20)] = 2

            val box = grid.boundingBox

            assertThat(box.minX, equalTo(-10))
            assertThat(box.minY, equalTo(-20))
            assertThat(box.maxX, equalTo(11))
            assertThat(box.maxY, equalTo(21))
        }

        @Test
        fun `should update bounding box when cells are added`() {
            val grid = UnboundedGrid<Int>()
            grid[GridIndex(0, 0)] = 1

            val box1 = grid.boundingBox
            assertThat(box1.maxX, equalTo(1))
            assertThat(box1.maxY, equalTo(1))

            grid[GridIndex(100, 100)] = 2

            val box2 = grid.boundingBox
            assertThat(box2.maxX, equalTo(101))
            assertThat(box2.maxY, equalTo(101))
        }

        @Test
        fun `should update bounding box when cells are removed`() {
            val grid = UnboundedGrid<Int>()
            grid[GridIndex(0, 0)] = 1
            grid[GridIndex(100, 100)] = 2

            val box1 = grid.boundingBox
            assertThat(box1.maxX, equalTo(101))
            assertThat(box1.maxY, equalTo(101))

            grid.remove(GridIndex(100, 100))

            val box2 = grid.boundingBox
            assertThat(box2.maxX, equalTo(1))
            assertThat(box2.maxY, equalTo(1))
        }
    }
}