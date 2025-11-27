package io.dungeons.world

import io.dungeons.board.BoardPosition
import io.dungeons.board.distance

data class Coordinate(val x: Feet, val y: Feet) {
    operator fun plus(other: Coordinate): Coordinate = Coordinate(this.x + other.x, this.y + other.y)

    operator fun minus(other: Coordinate): Coordinate = Coordinate(this.x - other.x, this.y - other.y)

    companion object {
        fun from(x: Double, y: Double): Coordinate = Coordinate(Feet(x), Feet(y))

        fun from(x: Int, y: Int): Coordinate = Coordinate(Feet(x.toDouble()), Feet(y.toDouble()))
    }
}

fun isInRange(from: Coordinate, to: Coordinate, range: Feet): Boolean = distance(from, to) <= range

fun isInRange(from: BoardPosition, to: BoardPosition, range: Square): Boolean = distance(from, to) <= range

fun distance(from: Coordinate, to: Coordinate): Feet {
    val deltaX = to.x - from.x
    val deltaY = to.y - from.y
    return (deltaX * deltaX + deltaY * deltaY).sqrt()
}
