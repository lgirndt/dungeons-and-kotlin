package io.dungeons.board

import io.dungeons.core.Grid
import io.dungeons.core.GridCell

class GameBoard(
    val width: Int,
    val height: Int
) {
    private val grid: Grid<Token?> = Grid(width, height, null)

    fun putTokenAt(position: GridCell, token: Token) {
        grid[position] = token
    }
}