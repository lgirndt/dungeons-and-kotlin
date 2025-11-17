package io.dungeons.world

@JvmInline
value class Square(val value: Int) : Comparable<Square> {

    operator fun plus(other: Square): Square {
        return Square(this.value + other.value)
    }

    operator fun minus(other: Square): Square {
        return Square(this.value - other.value)
    }

    operator fun times(factor: Int): Square {
        return Square(this.value * factor)
    }

    operator fun div(divisor: Int): Square {
        return Square(this.value / divisor)
    }

    fun toFeet(): Feet {
        return Feet(this.value * FEET_PER_SQUARE)
    }

    override fun compareTo(other: Square): Int {
        return this.value.compareTo(other.value)
    }

    companion object {
        const val FEET_PER_SQUARE = 5.0
    }
}
