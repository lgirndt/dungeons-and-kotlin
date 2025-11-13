package io.dungeons

import kotlin.math.sqrt

@JvmInline
value class Feet(val value: Double) : Comparable<Feet> {

    constructor(value: Int) : this(value.toDouble())

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

    fun sqrt(): Feet {
        return Feet(sqrt(this.value))
    }

    override fun compareTo(other: Feet): Int {
        return this.value.compareTo(other.value)
    }
}

data class Coordinate(val x: Feet, val y: Feet) {
    operator fun plus(other: Coordinate): Coordinate {
        return Coordinate(this.x + other.x, this.y + other.y)
    }

    operator fun minus(other: Coordinate): Coordinate {
        return Coordinate(this.x - other.x, this.y - other.y)
    }

    fun distance(other: Coordinate): Feet {
        val deltaX = other.x - this.x
        val deltaY = other.y - this.y
        return (deltaX * deltaX + deltaY * deltaY).sqrt()
    }

    companion object {
        fun from(x: Double, y: Double): Coordinate {
            return Coordinate(Feet(x), Feet(y))
        }

        fun from(x: Int, y: Int): Coordinate {
            return Coordinate(Feet(x.toDouble()), Feet(y.toDouble()))
        }
    }
}

fun isInRange(from: Coordinate, to: Coordinate, range: Feet): Boolean {
    return from.distance(to) <= range
}