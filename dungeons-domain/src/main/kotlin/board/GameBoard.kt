package io.dungeons.board

import io.dungeons.core.Grid
import io.dungeons.core.GridIndex


class GameBoard(
    val width: Int,
    val height: Int
) {
    private val grid: Grid<Token> = Grid(width, height, EmptyToken())

    private fun BoardPosition.toGridIndex(): GridIndex {
        return GridIndex(this.x.value, this.y.value)
    }

    fun putTokenAt(position: BoardPosition, token: Token) {
        grid[position.toGridIndex()] = token
    }

    fun removeTokenAt(position: BoardPosition) {
        grid[position.toGridIndex()] = EmptyToken()
    }
}