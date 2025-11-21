package core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.dungeons.core.BoundedGrid
import io.dungeons.core.BoundingBox
import io.dungeons.core.GridIndex
import io.dungeons.core.UnboundedGrid
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GridTest {

    @Test
    fun `iterateWithIndex should return all items`() {
        val items = setOf(
            Pair("A", GridIndex(0, 0)),
            Pair("B", GridIndex(1, 1)),
            Pair("C", GridIndex(2, 2))
        )
        val grid = UnboundedGrid<String>()
        items.forEach { (s, idx) -> grid[idx] = s }

        val result = grid.iterateWithIndex().asSequence().toSet()
        assertThat(result, equalTo(items))
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

            assertThat(mask[GridIndex(0, 0)], equalTo(false)) // 5 is not > 5
            assertThat(mask[GridIndex(1, 1)], equalTo(true))  // 10 > 5
            assertThat(mask[GridIndex(2, 2)], equalTo(false)) // 3 is not > 5
        }

        @Test
        fun `should create mask with same bounds as original grid`() {
            val grid = BoundedGrid<String>(minX = 5, minY = 10, maxX = 15, maxY = 20)
            grid[GridIndex(7, 12)] = "test"

            val mask = grid.toMaskBy { it.length > 3 }

            assertThat(mask.boundingBox, equalTo(BoundingBox(5, 10, 15, 20)))
        }

        @Test
        fun `should return false for unset positions in sparse grid`() {
            val grid = UnboundedGrid<String>()
            grid[GridIndex(0, 0)] = "A"
            grid[GridIndex(5, 5)] = "B"

            val mask = grid.toMaskBy { it == "A" }

            assertThat(mask[GridIndex(0, 0)], equalTo(true))
            assertThat(mask[GridIndex(5, 5)], equalTo(false))
            assertThat(mask[GridIndex(2, 2)], equalTo(false)) // Unset position
        }

        @Test
        fun `should handle empty grid`() {
            val grid = UnboundedGrid<Int>()

            val mask = grid.toMaskBy { it > 0 }

            assertThat(mask[GridIndex(0, 0)], equalTo(false))
        }

    }
}