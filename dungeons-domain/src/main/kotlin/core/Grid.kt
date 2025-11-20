package io.dungeons.core

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

    operator fun get(pos: GridCell): T {
        requireInBounds(pos)
        return cells[toIndex(pos)]
    }

    operator fun set(pos: GridCell, value: T) {
        requireInBounds(pos)
        cells[toIndex(pos)] = value
    }

    fun isInBounds(pos: GridCell): Boolean {
        return pos.x in 0 until width && pos.y in 0 until height
    }

    private fun requireInBounds(pos: GridCell) {
        require(isInBounds(pos)) {
            "Position $pos is out of bounds for grid of size ${width}x$height"
        }
    }

    private fun toIndex(pos: GridCell): Int {
        return pos.y * width + pos.x
    }
}