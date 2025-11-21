package io.dungeons.core

abstract class Grid<T> {
    private val cells: MutableMap<GridIndex,T> = mutableMapOf()

    open operator fun get(pos: GridIndex): T? {
        return cells[pos]
    }

    open operator fun set(pos: GridIndex, value: T) {
        cells[pos] = value
    }

    open fun remove(pos: GridIndex): T? {
        return cells.remove(pos)
    }

    open fun isEmpty(pos: GridIndex): Boolean {
        return cells.containsKey(pos)
    }
}

class UnboundedGrid<T> : Grid<T>()

class BoundedGrid<T>(
    val minX: Int = 0,
    val minY: Int = 0,
    val maxX: Int,
    val maxY: Int
) : Grid<T>() {

    init {
        require(maxX > minX) { "maxX must be greater than minX, but maxX=$maxX and minX=$minX" }
        require(maxY > minY) { "maxY must be greater than minY, but maxY=$maxY and minY=$minY" }
    }

    override fun set(pos: GridIndex, value: T) {
        requireInBounds(pos)
        super.set(pos, value)
    }

    override fun get(pos: GridIndex): T? {
        requireInBounds(pos)
        return super.get(pos)
    }

    override fun remove(pos: GridIndex): T? {
        requireInBounds(pos)
        return super.remove(pos)
    }

    override fun isEmpty(pos: GridIndex): Boolean {
        requireInBounds(pos)
        return super.isEmpty(pos)
    }

    fun isInBounds(pos: GridIndex): Boolean {
        return pos.x in minX until maxX && pos.y in minY until maxY
    }

    private fun requireInBounds(pos: GridIndex) {
        require(isInBounds(pos)) {
            "Position $pos is out of bounds for grid with bounds [$minX..$maxX) x [$minY..$maxY)"
        }
    }
}