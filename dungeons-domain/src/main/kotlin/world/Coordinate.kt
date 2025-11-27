package io.dungeons.world

import io.dungeons.board.BoardPosition

data class Coordinate(val x: Feet, val y: Feet) {
    operator fun plus(other: Coordinate): Coordinate = Coordinate(this.x + other.x, this.y + other.y)

    operator fun minus(other: Coordinate): Coordinate = Coordinate(this.x - other.x, this.y - other.y)

    fun distance(other: Coordinate): Feet {
        val deltaX = other.x - this.x
        val deltaY = other.y - this.y
        return (deltaX * deltaX + deltaY * deltaY).sqrt()
    }

    companion object {
        fun from(x: Double, y: Double): Coordinate = Coordinate(Feet(x), Feet(y))

        fun from(x: Int, y: Int): Coordinate = Coordinate(Feet(x.toDouble()), Feet(y.toDouble()))
    }
}

fun isInRange(from: Coordinate, to: Coordinate, range: Feet): Boolean = from.distance(to) <= range

fun isInRange(from: BoardPosition, to: BoardPosition, range: Square): Boolean = from.distance(to) <= range
