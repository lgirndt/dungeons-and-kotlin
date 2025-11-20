package io.dungeons.board

import io.dungeons.core.Grid

class GameBoard(
    val width: Int,
    val height: Int
) {
    private val grid: Grid<Token?> = Grid(width, height, null)

    fun putTokenAt(position: GridPosition, token: Token) {
        grid[position] = token
    }
}