package io.dungeons.world

class BitMatrix(
    private val width: Int,
    private val height: Int,
) {
    private val uint_size = width * height / UInt.SIZE_BITS + 1
    private val data: Array<UInt> = Array(uint_size) { 0u }

    private fun toPosition(x: Int, y: Int): Pair<Int, Int> {
        require(x in 0 until width) { "x coordinate $x out of bounds (0..${width - 1})" }
        require(y in 0 until height) { "y coordinate $y out of bounds (0..${height - 1})" }
        val bitIndex = y * width + x
        val uintIndex = bitIndex / UInt.SIZE_BITS
        val bitPosition = bitIndex % UInt.SIZE_BITS
        return Pair(uintIndex, bitPosition)
    }

    fun get(x: Int, y: Int): Boolean {
        val (uintIndex, bitPosition) = toPosition(x, y)
        return (data[uintIndex] and (1u shl bitPosition)) != 0u
    }

    fun set(x: Int, y: Int, value: Boolean) {
        val (uintIndex, bitPosition) = toPosition(x, y)

        data[uintIndex] = if (value) {
            data[uintIndex] or (1u shl bitPosition)
        } else {
            // set bit at position bitPosition to 0
            data[uintIndex] and (1u shl bitPosition).inv()
        }
    }

}
