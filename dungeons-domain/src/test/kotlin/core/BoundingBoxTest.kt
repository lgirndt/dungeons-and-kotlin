package io.dungeons.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BoundingBoxTest {
    @Nested
    inner class Contains {
        @Test
        fun `should return true for position at minX and minY`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertEquals(true, GridIndex(0, 0) in box)
        }

        @Test
        fun `should return true for position at maxX and maxY`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertEquals(true, GridIndex(10, 10) in box)
        }

        @Test
        fun `should return true for position inside bounds`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertEquals(true, GridIndex(5, 5) in box)
        }

        @Test
        fun `should return false for position below minX`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertEquals(false, GridIndex(-1, 5) in box)
        }

        @Test
        fun `should return false for position below minY`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertEquals(false, GridIndex(5, -1) in box)
        }

        @Test
        fun `should return false for position above maxX`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertEquals(false, GridIndex(11, 5) in box)
        }

        @Test
        fun `should return false for position above maxY`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertEquals(false, GridIndex(5, 11) in box)
        }

        @Test
        fun `should work with negative coordinates`() {
            val box = BoundingBox(minX = -10, minY = -10, maxX = 10, maxY = 10)

            assertEquals(true, GridIndex(-10, -10) in box)
            assertEquals(true, GridIndex(-5, -5) in box)
            assertEquals(false, GridIndex(-11, 0) in box)
        }

        @Test
        fun `should work with single cell bounding box`() {
            val box = BoundingBox(minX = 5, minY = 5, maxX = 5, maxY = 5)

            assertEquals(true, GridIndex(5, 5) in box)
            assertEquals(false, GridIndex(4, 5) in box)
            assertEquals(false, GridIndex(6, 5) in box)
            assertEquals(false, GridIndex(5, 4) in box)
            assertEquals(false, GridIndex(5, 6) in box)
        }
    }
}
