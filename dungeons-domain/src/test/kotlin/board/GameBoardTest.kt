package io.dungeons.board

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.dungeons.core.GridIndex
import io.dungeons.core.Id
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameBoardTest {

    // Test token that blocks movement
    private data class BlockingToken(
        override val id: Id<Token> = Id.generate()
    ) : Token {
        override val allowsMovementToSameSqqare: Boolean = false
    }

    // Test token that allows movement through it
    private data class PassableToken(
        override val id: Id<Token> = Id.generate()
    ) : Token {
        override val allowsMovementToSameSqqare: Boolean = true
    }

    @Nested
    inner class CalculateReach {

        @Test
        fun `should handle single blocking token directly adjacent`() {
            val board = GameBoard(3, 3)
            val start = BoardPosition.from(1, 1)
            val blockingToken = BlockingToken()

            // Place blocking token to the north
            board.putTokenTo(BoardPosition.from(1, 0), blockingToken)

            val reach = board.calculateReach(start, steps = 1)

            // Should NOT include the blocked position
            assertThat(reach[GridIndex(1, 0)], equalTo(null))
            // Should include other neighbors
            assertThat(reach[GridIndex(1, 2)], equalTo(1))
        }

        @Test
        fun `should return only start position when steps is zero`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)

            val reach = board.calculateReach(start, steps = 0)

            assertThat(reach[GridIndex(5, 5)], equalTo(0))
        }

        @Test
        fun `should reach all 8 neighbors with steps 1`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)

            val reach = board.calculateReach(start, steps = 1)

            assertThat(reach[GridIndex(5, 5)], equalTo(0))
            assertThat(reach[GridIndex(4, 4)], equalTo(1)) // NW
            assertThat(reach[GridIndex(5, 4)], equalTo(1)) // N
            assertThat(reach[GridIndex(6, 4)], equalTo(1)) // NE
            assertThat(reach[GridIndex(4, 5)], equalTo(1)) // W
            assertThat(reach[GridIndex(6, 5)], equalTo(1)) // E
            assertThat(reach[GridIndex(4, 6)], equalTo(1)) // SW
            assertThat(reach[GridIndex(5, 6)], equalTo(1)) // S
            assertThat(reach[GridIndex(6, 6)], equalTo(1)) // SE
        }

        @Test
        fun `should expand reach with higher steps`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)

            val reach = board.calculateReach(start, steps = 2)

            // Should reach positions 2 steps away
            assertThat(reach[GridIndex(5, 3)], equalTo(2)) // 2 steps north
            assertThat(reach[GridIndex(7, 5)], equalTo(2)) // 2 steps east
            assertThat(reach[GridIndex(3, 5)], equalTo(2)) // 2 steps west
            assertThat(reach[GridIndex(5, 7)], equalTo(2)) // 2 steps south
        }

        @Test
        fun `should not reach blocked positions directly`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)
            val blockingToken = BlockingToken()

            // Place blocking token to the north
            board.putTokenTo(BoardPosition.from(5, 4), blockingToken)

            val reach = board.calculateReach(start, steps = 1)

            // Should not include the directly blocked position
            assertThat(reach[GridIndex(5, 4)], equalTo(null))

            // Should still reach other directions
            assertThat(reach[GridIndex(5, 6)], equalTo(1))
            assertThat(reach[GridIndex(6, 5)], equalTo(1))
            assertThat(reach[GridIndex(4, 5)], equalTo(1))
        }

        @Test
        fun `should pass through passable tokens`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)
            val passableToken = PassableToken()

            // Place passable token to the north
            board.putTokenTo(BoardPosition.from(5, 4), passableToken)

            val reach = board.calculateReach(start, steps = 2)

            // Should reach through the passable token
            assertThat(reach[GridIndex(5, 4)], equalTo(1))
            assertThat(reach[GridIndex(5, 3)], equalTo(2))
        }

        @Test
        fun `should route around blocking tokens`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)
            val blockingToken1 = BlockingToken()
            val blockingToken2 = BlockingToken()

            // Create a wall to the north
            board.putTokenTo(BoardPosition.from(4, 4), blockingToken1)
            board.putTokenTo(BoardPosition.from(5, 4), blockingToken2)

            val reach = board.calculateReach(start, steps = 2)

            // Can reach positions around the wall
            assertThat(reach[GridIndex(6, 4)], equalTo(1)) // NE is reachable
            assertThat(reach[GridIndex(3, 5)], equalTo(2)) // Can reach to the west
            // The blocked positions should not be reachable
            assertThat(reach[GridIndex(4, 4)], equalTo(null))
            assertThat(reach[GridIndex(5, 4)], equalTo(null))
        }

        @Test
        fun `should respect board boundaries`() {
            val board = GameBoard(5, 5)
            val start = BoardPosition.from(0, 0)

            val reach = board.calculateReach(start, steps = 2)

            // Should reach corner and adjacent positions
            assertThat(reach[GridIndex(0, 0)], equalTo(0))
            assertThat(reach[GridIndex(1, 0)], equalTo(1))
            assertThat(reach[GridIndex(0, 1)], equalTo(1))
            assertThat(reach[GridIndex(2, 2)], equalTo(2))
        }

        @Test
        fun `should handle large steps values`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(0, 0)

            val reach = board.calculateReach(start, steps = 20)

            // Should reach diagonal corner
            assertThat(reach[GridIndex(9, 9)], equalTo(9)) // Diagonal distance
        }

        @Test
        fun `should handle starting from edge`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(0, 0)

            val reach = board.calculateReach(start, steps = 1)

            // Should reach start and 3 neighbors
            assertThat(reach[GridIndex(0, 0)], equalTo(0))
            assertThat(reach[GridIndex(1, 0)], equalTo(1))
            assertThat(reach[GridIndex(0, 1)], equalTo(1))
            assertThat(reach[GridIndex(1, 1)], equalTo(1))
        }

        @Test
        fun `should reject negative steps`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)

            val exception = assertThrows<IllegalArgumentException> {
                board.calculateReach(start, steps = -1)
            }

            assertThat(exception.message, equalTo("Steps must be non-negative, but was -1"))
        }

        @Test
        fun `should handle surrounded scenario`() {
            val board = GameBoard(7, 7)
            val start = BoardPosition.from(3, 3)

            fun surroundingPairs(): Sequence<Pair<Int, Int>> = sequence {
                for (x in 2..4)
                    for (y in 2..4)
                        if (x != 3 || y != 3)
                            yield(Pair(x, y))
            }

            surroundingPairs().forEach {
                (x, y) ->
                board.putTokenTo(BoardPosition.from(x, y), BlockingToken())
            }

            val reach = board.calculateReach(start, steps = 1)

            // Should only reach the starting position (all neighbors are blocked)
            assertThat(reach[GridIndex(3, 3)], equalTo(0))
            // Verify neighbors are not reachable
            surroundingPairs().forEach { (x, y) ->
                assertThat(reach[GridIndex(x, y)], equalTo(null))
            }
        }
    }
}
