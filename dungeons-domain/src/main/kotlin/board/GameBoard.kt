package io.dungeons.board

import io.dungeons.core.Grid
import io.dungeons.core.GridCell


class GameBoard(
    val width: Int,
    val height: Int
) {
    private val grid: Grid<Token> = Grid(width, height, EmptyToken())

    private fun BoardPosition.toGridCell(): GridCell {
        return GridCell(this.x.value, this.y.value)
    }

    fun putTokenAt(position: BoardPosition, token: Token) {
        grid[position.toGridCell()] = token
    }

    fun removeTokenAt(position: BoardPosition) {
        grid[position.toGridCell()] = EmptyToken()
    }
}