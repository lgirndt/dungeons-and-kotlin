package io.dungeons.core

data class GridIndex(
    val x: Int,
    val y: Int
) {
    init {
        require(x >= 0) { "x must be non-negative, but was $x" }
        require(y >= 0) { "y must be non-negative, but was $y" }
    }
}