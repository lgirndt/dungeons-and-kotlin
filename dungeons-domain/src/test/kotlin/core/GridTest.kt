package core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.dungeons.core.GridIndex
import io.dungeons.core.UnboundedGrid
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
}