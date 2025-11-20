package io.dungeons.world

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
