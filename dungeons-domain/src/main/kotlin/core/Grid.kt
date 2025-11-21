package io.dungeons.core

data class BoundingBox(
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int
)

abstract class Grid<T> {
    protected val cells: MutableMap<GridIndex,T> = mutableMapOf()

    abstract val boundingBox : BoundingBox

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

class UnboundedGrid<T> : Grid<T>() {
    override val boundingBox: BoundingBox
        get() {
            if (cells.isEmpty()) {
                return BoundingBox(0, 0, 0, 0)
            }

            val minX = cells.keys.minOf { it.x }
            val maxX = cells.keys.maxOf { it.x } + 1
            val minY = cells.keys.minOf { it.y }
            val maxY = cells.keys.maxOf { it.y } + 1

            return BoundingBox(minX, minY, maxX, maxY)
        }
}

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

    override val boundingBox: BoundingBox
        get() = BoundingBox(minX, minY, maxX, maxY)

    private fun requireInBounds(pos: GridIndex) {
        require(isInBounds(pos)) {
            "Position $pos is out of bounds for grid with bounds [$minX..$maxX) x [$minY..$maxY)"
        }
    }

}