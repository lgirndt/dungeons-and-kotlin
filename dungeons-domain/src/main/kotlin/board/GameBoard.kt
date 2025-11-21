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
    private val grid: Grid<Token> = BoundedGrid(maxX = width - 1, maxY = height - 1)
    private val tokenToIndex = mutableMapOf<Id<Token>, GridIndex>()

    private fun BoardPosition.toGridIndex(): GridIndex {
        return GridIndex(this.x.value, this.y.value)
    }

    fun putTokenTo(position: BoardPosition, token: Token) {
        require(!tokenToIndex.containsKey(token.id)) {"Token $token is already on the board"}
        require(!grid.isEmpty(position.toGridIndex())) {"There is already a token at position $position"}

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

    /**
     * Calculate all positions reachable from a given position within the specified number of movement steps.
     * Uses BFS to account for blocking tokens and terrain.
     *
     * @param startPosition The starting position
     * @param speed The maximum number of movement steps
     * @return A grid where reachable cells contain their distance from the start (in steps), unreachable cells are null
     */
    fun calculateReach(startPosition: BoardPosition, speed: Int): Grid<Int> {
        require(speed >= 0) { "Speed must be non-negative, but was $speed" }

        val result = BoundedGrid<Int>(maxX = width - 1, maxY = height - 1)
        val startIndex = startPosition.toGridIndex()
        val distances = mutableMapOf<GridIndex, Int>()
        val queue = ArrayDeque<GridIndex>()

        queue.add(startIndex)
        distances[startIndex] = 0

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val currentDistance = distances[current]!!

            if (currentDistance >= speed) {
                continue
            }

            for (neighbor in getNeighbors(current)) {
                if (neighbor !in distances && !isBlocked(neighbor)) {
                    distances[neighbor] = currentDistance + 1
                    queue.add(neighbor)
                }
            }
        }

        // Populate the result grid with distances
        distances.forEach { (index, distance) ->
            result[index] = distance
        }

        return result
    }

    private fun getNeighbors(index: GridIndex): List<GridIndex> {
        return DIRECTIONS.map { (dx, dy) ->
            GridIndex(index.x + dx, index.y + dy)
        }.filter { it in grid.boundingBox }
    }

    private fun isBlocked(index: GridIndex): Boolean {
        if (index !in grid.boundingBox) {
            return true // Out of bounds positions are blocked
        }
        val token = grid[index] ?: return false
        return !token.allowsMovementToSameSqqare
    }

    private companion object {
        // 8 directions: N, NE, E, SE, S, SW, W, NW
        private val DIRECTIONS = listOf(
            0 to -1,  // N
            1 to -1,  // NE
            1 to 0,   // E
            1 to 1,   // SE
            0 to 1,   // S
            -1 to 1,  // SW
            -1 to 0,  // W
            -1 to -1  // NW
        )
    }

}