package io.dungeons.board

import io.dungeons.core.*
import io.dungeons.world.Square
import kotlin.math.abs


class GameBoard(
    val width: Int,
    val height: Int
) {
    private val grid: Grid<Token> = BoundedGrid.fromDimensions(width, height)
    private val tokenToIndex = mutableMapOf<Id<Token>, GridIndex>()

    private fun BoardPosition.toGridIndex(): GridIndex {
        return GridIndex(this.x.value, this.y.value)
    }

    fun putTokenTo(position: BoardPosition, token: Token) {
        require(!tokenToIndex.containsKey(token.id)) { "Token $token is already on the board" }
        require(!grid.isEmpty(position.toGridIndex())) { "There is already a token at position $position" }

        tokenToIndex[token.id] = position.toGridIndex()
        grid[position.toGridIndex()] = token
    }

    fun removeTokenFrom(position: BoardPosition): Token? {
        return grid.remove(position.toGridIndex())?.also {
            require(tokenToIndex.containsKey(it.id)) { "Internal Inconsistency. Token $it is not in the index" }

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
     * @param steps The maximum number of movement steps
     * @return A grid where reachable cells contain their distance from the start (in steps), unreachable cells are null
     */
    fun calculateReach(startPosition: BoardPosition, steps: Int): Grid<Int> {
        require(steps >= 0) { "Steps must be non-negative, but was $steps" }

        val startIndex = startPosition.toGridIndex()
        val distances = UnboundedGrid<Int>()
        val queue = ArrayDeque<GridIndex>()

        queue.add(startIndex)
        distances[startIndex] = 0

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val currentDistance = distances[current] ?: error("Internal Inconsistency. Token $current")

            if (currentDistance >= steps) {
                continue
            }

            for (neighbor in getNeighbors(current)) {
                if (neighbor !in distances && allowsMovement(neighbor)) {
                    distances[neighbor] = currentDistance + 1
                    queue.add(neighbor)
                }
            }
        }
        return distances
    }

    private fun getNeighbors(index: GridIndex): List<GridIndex> {
        return DIRECTIONS.map { (dx, dy) ->
            GridIndex(index.x + dx, index.y + dy)
        }.filter { it in grid.boundingBox }
    }

    private fun allowsMovement(index: GridIndex): Boolean {
        if (index !in grid.boundingBox) {
            return false // Out of bounds positions are blocked
        }
        val token = grid[index] ?: return true
        return token.allowsMovement
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

    fun hasLineOfSight(from: BoardPosition, to: BoardPosition): Boolean {
        val fromIndex = from.toGridIndex()
        val toIndex = to.toGridIndex()

        // Use Bresenham's line algorithm to trace the line
        val line = bresenhamLine(fromIndex, toIndex)

        // Check each position along the line (excluding start and end positions)
        for (index in line.drop(1).dropLast(1)) {
            val token = grid[index]
            if (token != null && !token.allowsSight) {
                return false // Line of sight is blocked
            }
        }

        return true // No blocking tokens found
    }

    private fun bresenhamLine(from: GridIndex, to: GridIndex): List<GridIndex> {
        val result = mutableListOf<GridIndex>()
        var x0 = from.x
        var y0 = from.y
        val x1 = to.x
        val y1 = to.y

        val dx = abs(x1 - x0)
        val dy = abs(y1 - y0)
        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1
        var err = dx - dy

        while (true) {
            result.add(GridIndex(x0, y0))

            if (x0 == x1 && y0 == y1) break

            val e2 = 2 * err
            if (e2 > -dy) {
                err -= dy
                x0 += sx
            }
            if (e2 < dx) {
                err += dx
                y0 += sy
            }
        }

        return result
    }

}