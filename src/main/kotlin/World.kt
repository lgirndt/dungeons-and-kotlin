package org.example

import kotlin.math.sqrt

@JvmInline
value class Feet(val value: Double) {
    operator fun plus(other: Feet): Feet {
        return Feet(this.value + other.value)
    }

    operator fun minus(other: Feet): Feet {
        return Feet(this.value - other.value)
    }

    operator fun times(other: Feet): Feet {
        return Feet(this.value * other.value)
    }

    operator fun div(other: Feet): Feet {
        return Feet(this.value / other.value)
    }
}

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