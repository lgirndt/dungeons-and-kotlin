package io.dungeons.core

data class GridPosition(
    val x: Int,
    val y: Int
) {
    init {
        require(x >= 0) { "x must be non-negative, but was $x" }
        require(y >= 0) { "y must be non-negative, but was $y" }
    }
}

class Grid<T>(
    val width: Int,
    val height: Int,
    initialValue: T
) {
    init {
        require(width > 0) { "width must be positive, but was $width" }
        require(height > 0) { "height must be positive, but was $height" }
    }

    private val cells: MutableList<T> = MutableList(width * height) { initialValue }

    operator fun get(pos: GridPosition): T {
        requireInBounds(pos)
        return cells[toIndex(pos)]
    }

    operator fun set(pos: GridPosition, value: T) {
        requireInBounds(pos)
        cells[toIndex(pos)] = value
    }

    fun isInBounds(pos: GridPosition): Boolean {
        return pos.x in 0 until width && pos.y in 0 until height
    }

    private fun requireInBounds(pos: GridPosition) {
        require(isInBounds(pos)) {
            "Position $pos is out of bounds for grid of size ${width}x$height"
        }
    }

    private fun toIndex(pos: GridPosition): Int {
        return pos.y * width + pos.x
    }
}