package io.dungeons.board

import io.dungeons.core.BoundedGrid
import io.dungeons.core.Grid
import io.dungeons.core.GridIndex
import io.dungeons.core.Id
import io.dungeons.world.Square


class GameBoard(
    val width: Int,
    val height: Int
) {
    private val grid: Grid<Token> = BoundedGrid(maxX = width, maxY = height)
    private val tokenToIndex = mutableMapOf<Id<Token>, GridIndex>()

    private fun BoardPosition.toGridIndex(): GridIndex {
        return GridIndex(this.x.value, this.y.value)
    }

    fun putTokenTo(position: BoardPosition, token: Token) {
        require(!tokenToIndex.containsKey(token.id)) {"Token $token is already on the board"}
        require(grid.isEmpty(position.toGridIndex())) {"There is already a token at position $position"}

        tokenToIndex[token.id] = position.toGridIndex()
        grid[position.toGridIndex()] = token
    }

    fun removeTokenFrom(position: BoardPosition) : Token? {
        return grid.remove(position.toGridIndex())?.also {
            require(tokenToIndex.containsKey(it.id)) {"Internal Inconsistency. Token $it is not in the index"}

            tokenToIndex.remove(it.id)
        }
    }

    fun getPositionOf(token: Token): BoardPosition? {
        val index = tokenToIndex[token.id] ?: return null
        return BoardPosition(
            Square(index.x),
            Square(index.y)
        )
    }

    fun moveTokenTo(toPosition: BoardPosition, token: Token) {
        val fromPosition = getPositionOf(token)
            ?: throw IllegalArgumentException("Token $token is not on the board")

        removeTokenFrom(fromPosition)
            ?: throw IllegalStateException("Internal Inconsistency. Token $token was not found at position $fromPosition")

        putTokenTo(toPosition, token)
    }
}