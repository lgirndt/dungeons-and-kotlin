package io.dungeons.board

import io.dungeons.core.BoundedGrid
import io.dungeons.core.BoundingBox
import io.dungeons.core.Grid
import io.dungeons.core.GridIndex
import io.dungeons.core.Id
import io.dungeons.core.UnboundedGrid
import kotlin.math.abs

enum class BoardLayer {
    GROUND,
    OBJECT,
    CREATURE,
}

class GameBoard(val width: Int, val height: Int) {
    private val layers: Map<BoardLayer, Grid<Token>> =
        BoardLayer.entries.associateWith { BoundedGrid.fromDimensions<Token>(width, height) }

    private val tokenToIndex = mutableMapOf<Id<Token>, GridIndex>()

    private val boundingBox: BoundingBox
        get() = gridOnLayer(BoardLayer.GROUND).boundingBox

    private fun BoardPosition.toGridIndex(): GridIndex = GridIndex(this.x.value, this.y.value)

    fun putTokenTo(position: BoardPosition, token: Token) {
        require(!tokenToIndex.containsKey(token.id)) {
            "Token $token is already on the board"
        }
        require(!gridOnLayer(token.layer).isEmpty(position.toGridIndex())) {
            "There is already a token at position $position"
        }

        tokenToIndex[token.id] = position.toGridIndex()
        gridOnLayer(token.layer).set(position.toGridIndex(), token)
    }

    fun getTokenAt(layer: BoardLayer, position: BoardPosition): Token? = gridOnLayer(layer)[position.toGridIndex()]

    private fun gridOnLayer(layer: BoardLayer): Grid<Token> = layers[layer]!!

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

    private fun getNeighbors(index: GridIndex): List<GridIndex> = DIRECTIONS
        .map { (dx, dy) ->
            GridIndex(index.x + dx, index.y + dy)
        }.filter { it in boundingBox }

    private fun allowsMovement(index: GridIndex): Boolean {
        if (index !in boundingBox) {
            return false // Out of bounds positions are blocked
        }
        return testOnEachLayer(index, defaultOnAbsence = true) { it.allowsMovement }
    }

    private fun testOnEachLayer(index: GridIndex, defaultOnAbsence: Boolean, predicate: (Token) -> Boolean): Boolean =
        BoardLayer.entries.all { layer ->
            val token = gridOnLayer(layer)[index]
            if (token == null) {
                defaultOnAbsence
            } else {
                predicate(token)
            }
        }

    private companion object {
        private val DIRECTIONS = listOf(
            0 to -1, // N
            1 to -1, // NE
            1 to 0, // E
            1 to 1, // SE
            0 to 1, // S
            -1 to 1, // SW
            -1 to 0, // W
            -1 to -1, // NW
        )
    }

    fun hasLineOfSight(from: BoardPosition, to: BoardPosition): Boolean {
        val fromIndex = from.toGridIndex()
        val toIndex = to.toGridIndex()

        // Use Bresenham's line algorithm to trace the line
        val line = bresenhamLine(fromIndex, toIndex)

        // Check each position along the line (excluding start and end positions)
        for (index in line.drop(1).dropLast(1)) {
            if (!allowsSight(index)) {
                return false // Sight is blocked
            }
        }

        return true // No blocking tokens found
    }

    private fun allowsSight(index: GridIndex): Boolean {
        if (index !in boundingBox) {
            return false // Out of bounds positions are blocked
        }
        return testOnEachLayer(index, defaultOnAbsence = true) { it.allowsSight }
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
