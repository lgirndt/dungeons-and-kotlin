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
}