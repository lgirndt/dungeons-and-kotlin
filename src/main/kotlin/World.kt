package org.example

import kotlin.math.sqrt

data class Coordinate(val x: Int, val y: Int) {
    operator fun plus(other: Coordinate): Coordinate {
        return Coordinate(this.x + other.x, this.y + other.y)
    }

    operator fun minus(other: Coordinate): Coordinate {
        return Coordinate(this.x - other.x, this.y - other.y)
    }

    fun distance(other: Coordinate): Double {
        val deltaX = (other.x - this.x).toDouble()
        val deltaY = (other.y - this.y).toDouble()
        return sqrt(deltaX * deltaX + deltaY * deltaY)
    }
}