package io.dungeons.domain.world

@JvmInline
value class Square(val value: Int) : Comparable<Square> {
    operator fun plus(other: Square): Square = Square(this.value + other.value)

    operator fun minus(other: Square): Square = Square(this.value - other.value)

    operator fun times(factor: Int): Square = Square(this.value * factor)

    operator fun div(divisor: Int): Square = Square(this.value / divisor)

    fun toFeet(): Feet = Feet(this.value * FEET_PER_SQUARE)

    override fun compareTo(other: Square): Int = this.value.compareTo(other.value)

    companion object {
        const val FEET_PER_SQUARE = 5.0
    }
}
