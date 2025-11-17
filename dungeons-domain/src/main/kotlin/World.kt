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

class BitMatrix(
    private val width: Int,
    private val height: Int,
) {
    private val uint_size = width * height / UInt.SIZE_BITS + 1
    private val data: Array<UInt> = Array(uint_size) { 0u }

    private fun toPosition(x: Int, y: Int): Pair<Int, Int> {
        require(x in 0 until width) { "x coordinate $x out of bounds (0..${width - 1})" }
        require(y in 0 until height) { "y coordinate $y out of bounds (0..${height - 1})" }
        val bitIndex = y * width + x
        val uintIndex = bitIndex / UInt.SIZE_BITS
        val bitPosition = bitIndex % UInt.SIZE_BITS
        return Pair(uintIndex, bitPosition)
    }

    fun get(x: Int, y: Int): Boolean {
        val (uintIndex, bitPosition) = toPosition(x, y)
        return (data[uintIndex] and (1u shl bitPosition)) != 0u
    }

    fun set(x: Int, y: Int, value: Boolean) {
        val (uintIndex, bitPosition) = toPosition(x, y)

        data[uintIndex] = if (value) {
            data[uintIndex] or (1u shl bitPosition)
        } else {
            // set bit at position bitPosition to 0
            data[uintIndex] and (1u shl bitPosition).inv()
        }
    }

}