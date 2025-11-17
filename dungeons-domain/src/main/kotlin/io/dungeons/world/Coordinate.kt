package io.dungeons.world

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
