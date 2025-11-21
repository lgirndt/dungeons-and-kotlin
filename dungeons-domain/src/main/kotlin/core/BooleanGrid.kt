package io.dungeons.core

class BooleanGrid(
    val minX: Int = 0,
    val minY: Int = 0,
    val maxX: Int,
    val maxY: Int
) {
    private val grid: Grid<Unit> = BoundedGrid(minX, minY, maxX, maxY)

    operator fun get(index: GridIndex): Boolean {
        return grid.contains(index)
    }

    operator fun set(index: GridIndex, value: Boolean) {
        if (value) {
            grid.set(index, Unit)
            return
        } else {
            grid.remove(index)
        }
    }

    fun union(other: BooleanGrid): BooleanGrid {
        val newMinX = minOf(this.minX, other.minX)
        val newMinY = minOf(this.minY, other.minY)
        val newMaxX = maxOf(this.maxX, other.maxX)
        val newMaxY = maxOf(this.maxY, other.maxY)

        val result = BooleanGrid(newMinX, newMinY, newMaxX, newMaxY)

        for (x in newMinX..newMaxX) {
            for (y in newMinY..newMaxY) {
                val index = GridIndex(x, y)
                result[index] = this[index] || other[index]
            }
        }

        return result
    }

    fun intersect(other: BooleanGrid): BooleanGrid {
        val newMinX = maxOf(this.minX, other.minX)
        val newMinY = maxOf(this.minY, other.minY)
        val newMaxX = minOf(this.maxX, other.maxX)
        val newMaxY = minOf(this.maxY, other.maxY)

        val result = BooleanGrid(newMinX, newMinY, newMaxX, newMaxY)

        for (x in newMinX..newMaxX) {
            for (y in newMinY..newMaxY) {
                val index = GridIndex(x, y)
                result[index] = this[index] && other[index]
            }
        }

        return result
    }

}