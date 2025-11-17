package io.dungeons.world

import kotlin.math.abs
import kotlin.math.max

data class GridPosition(
    val x: Square,
    val y: Square,
) {
    operator fun plus(other: GridPosition): GridPosition {
        return GridPosition(this.x + other.x, this.y + other.y)
    }

    operator fun minus(other: GridPosition): GridPosition {
        return GridPosition(this.x - other.x, this.y - other.y)
    }

    /**
     * Calculate Manhattan distance (in squares) between two grid positions.
     * This represents the minimum number of orthogonal moves needed.
     */
    fun manhattanDistance(other: GridPosition): Square {
        val dx = abs((this.x - other.x).value)
        val dy = abs((this.y - other.y).value)
        return Square(dx + dy)
    }

    /**
     * Calculate Chebyshev distance (in squares) between two grid positions.
     * This represents the minimum number of moves including diagonals (D&D grid movement).
     * In D&D 5e, diagonal movement counts as 1 square.
     */
    fun chebyshevDistance(other: GridPosition): Square {
        val dx = abs((this.x - other.x).value)
        val dy = abs((this.y - other.y).value)
        return Square(max(dx, dy))
    }

    /**
     * Convert grid position to world coordinate.
     * Each square represents 5 feet.
     */
    fun toCoordinate(): Coordinate {
        return Coordinate(this.x.toFeet(), this.y.toFeet())
    }

    companion object {
        fun from(x: Int, y: Int): GridPosition {
            return GridPosition(Square(x), Square(y))
        }

        /**
         * Convert world coordinate to grid position.
         * Rounds down to the nearest grid square.
         */
        fun fromCoordinate(coordinate: Coordinate): GridPosition {
            val xSquares = (coordinate.x.value / Square.FEET_PER_SQUARE).toInt()
            val ySquares = (coordinate.y.value / Square.FEET_PER_SQUARE).toInt()
            return GridPosition(Square(xSquares), Square(ySquares))
        }
    }
}
