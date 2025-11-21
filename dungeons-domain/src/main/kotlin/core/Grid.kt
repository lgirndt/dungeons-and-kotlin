package io.dungeons.core

import java.util.*

class Grid<T>(
    val width: Int,
    val height: Int
) {
    init {
        require(width > 0) { "width must be positive, but was $width" }
        require(height > 0) { "height must be positive, but was $height" }
    }

    private val cells: MutableMap<GridIndex,T> = mutableMapOf()


    operator fun get(pos: GridIndex): T? {
        requireInBounds(pos)
        return cells[pos]
    }

    operator fun set(pos: GridIndex, value: T) {
        requireInBounds(pos)
        cells[pos] = value
    }

    fun isInBounds(pos: GridIndex): Boolean {
        return pos.x in 0 until width && pos.y in 0 until height
    }

    private fun requireInBounds(pos: GridIndex) {
        require(isInBounds(pos)) {
            "Position $pos is out of bounds for grid of size ${width}x$height"
        }
    }
}