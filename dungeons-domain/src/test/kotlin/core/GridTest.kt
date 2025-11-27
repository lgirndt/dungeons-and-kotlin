package core

import io.dungeons.core.BoundedGrid
import io.dungeons.core.BoundingBox
import io.dungeons.core.GridIndex
import io.dungeons.core.UnboundedGrid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GridTest {
    @Test
    fun `iterateWithIndex should return all items`() {
        val items = setOf(
            Pair("A", GridIndex(0, 0)),
            Pair("B", GridIndex(1, 1)),
            Pair("C", GridIndex(2, 2)),
        )
        val grid = UnboundedGrid<String>()
        items.forEach { (s, idx) -> grid[idx] = s }

        val result = grid.iterateWithIndex().asSequence().toSet()
        assertEquals(items, result)
    }

    @Nested
    inner class ToMaskBy {
        @Test
        fun `should create mask with true for values matching predicate`() {
            val grid = UnboundedGrid<Int>()
            grid[GridIndex(0, 0)] = 5
            grid[GridIndex(1, 1)] = 10
            grid[GridIndex(2, 2)] = 3

            val mask = grid.toMaskBy { it > 5 }

            assertEquals(false, mask[GridIndex(0, 0)]) // 5 is not > 5
            assertEquals(true, mask[GridIndex(1, 1)]) // 10 > 5
            assertEquals(false, mask[GridIndex(2, 2)]) // 3 is not > 5
        }

        @Test
        fun `should create mask with same bounds as original grid`() {
            val grid = BoundedGrid<String>(minX = 5, minY = 10, maxX = 15, maxY = 20)
            grid[GridIndex(7, 12)] = "test"

            val mask = grid.toMaskBy { it.length > 3 }

            assertEquals(BoundingBox(5, 10, 15, 20), mask.boundingBox)
        }

        @Test
        fun `should return false for unset positions in sparse grid`() {
            val grid = UnboundedGrid<String>()
            grid[GridIndex(0, 0)] = "A"
            grid[GridIndex(5, 5)] = "B"

            val mask = grid.toMaskBy { it == "A" }

            assertEquals(true, mask[GridIndex(0, 0)])
            assertEquals(false, mask[GridIndex(5, 5)])
            assertEquals(false, mask[GridIndex(2, 2)]) // Unset position
        }

        @Test
        fun `should handle empty grid`() {
            val grid = UnboundedGrid<Int>()

            val mask = grid.toMaskBy { it > 0 }

            assertEquals(false, mask[GridIndex(0, 0)])
        }
    }
}
