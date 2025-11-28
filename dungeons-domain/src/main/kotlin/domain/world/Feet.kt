package io.dungeons.domain.world

import kotlin.math.sqrt

@JvmInline
value class Feet(val value: Double) : Comparable<Feet> {
    constructor(value: Int) : this(value.toDouble())

    operator fun plus(other: Feet): Feet = Feet(this.value + other.value)

    operator fun minus(other: Feet): Feet = Feet(this.value - other.value)

    operator fun times(other: Feet): Feet = Feet(this.value * other.value)

    operator fun div(other: Feet): Feet = Feet(this.value / other.value)

    fun sqrt(): Feet = Feet(sqrt(this.value))

    override fun compareTo(other: Feet): Int = this.value.compareTo(other.value)
}
