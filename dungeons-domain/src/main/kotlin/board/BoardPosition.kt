package io.dungeons.board

import io.dungeons.world.Coordinate
import io.dungeons.world.Square
import kotlin.math.abs
import kotlin.math.max

data class BoardPosition(
    val x: Square,
    val y: Square,
) {
    operator fun plus(other: BoardPosition): BoardPosition {
        return BoardPosition(this.x + other.x, this.y + other.y)
    }

    operator fun minus(other: BoardPosition): BoardPosition {
        return BoardPosition(this.x - other.x, this.y - other.y)
    }

    /**
     * Calculate Chebyshev distance (in squares) between two grid positions.
     * This represents the minimum number of moves including diagonals (D&D grid movement).
     * In D&D 5e, diagonal movement counts as 1 square.
     */
    fun distance(other: BoardPosition): Square {
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
        fun from(x: Int, y: Int): BoardPosition {
            return BoardPosition(Square(x), Square(y))
        }

        /**
         * Convert world coordinate to grid position.
         * Rounds down to the nearest grid square.
         */
        fun fromCoordinate(coordinate: Coordinate): BoardPosition {
            val xSquares = (coordinate.x.value / Square.Companion.FEET_PER_SQUARE).toInt()
            val ySquares = (coordinate.y.value / Square.Companion.FEET_PER_SQUARE).toInt()
            return BoardPosition(Square(xSquares), Square(ySquares))
        }
    }
}