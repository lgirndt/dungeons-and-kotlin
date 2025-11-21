package io.dungeons.core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BoundingBoxTest {

    @Nested
    inner class Contains {

        @Test
        fun `should return true for position at minX and minY`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertThat(GridIndex(0, 0) in box, equalTo(true))
        }

        @Test
        fun `should return true for position at maxX and maxY`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertThat(GridIndex(10, 10) in box, equalTo(true))
        }

        @Test
        fun `should return true for position inside bounds`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertThat(GridIndex(5, 5) in box, equalTo(true))
        }

        @Test
        fun `should return false for position below minX`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertThat(GridIndex(-1, 5) in box, equalTo(false))
        }

        @Test
        fun `should return false for position below minY`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertThat(GridIndex(5, -1) in box, equalTo(false))
        }

        @Test
        fun `should return false for position above maxX`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertThat(GridIndex(11, 5) in box, equalTo(false))
        }

        @Test
        fun `should return false for position above maxY`() {
            val box = BoundingBox(minX = 0, minY = 0, maxX = 10, maxY = 10)

            assertThat(GridIndex(5, 11) in box, equalTo(false))
        }

        @Test
        fun `should work with negative coordinates`() {
            val box = BoundingBox(minX = -10, minY = -10, maxX = 10, maxY = 10)

            assertThat(GridIndex(-10, -10) in box, equalTo(true))
            assertThat(GridIndex(-5, -5) in box, equalTo(true))
            assertThat(GridIndex(-11, 0) in box, equalTo(false))
        }


        @Test
        fun `should work with single cell bounding box`() {
            val box = BoundingBox(minX = 5, minY = 5, maxX = 5, maxY = 5)

            assertThat(GridIndex(5, 5) in box, equalTo(true))
            assertThat(GridIndex(4, 5) in box, equalTo(false))
            assertThat(GridIndex(6, 5) in box, equalTo(false))
            assertThat(GridIndex(5, 4) in box, equalTo(false))
            assertThat(GridIndex(5, 6) in box, equalTo(false))
        }
    }
}
