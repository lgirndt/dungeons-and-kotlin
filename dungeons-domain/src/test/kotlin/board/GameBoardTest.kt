package io.dungeons.board

import io.dungeons.core.GridIndex
import io.dungeons.core.Id
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameBoardTest {

    // Test token that blocks movement
    private data class BlockingToken(
        override val id: Id<Token> = Id.generate()
    ) : Token {
        override val allowsMovement: Boolean = false
    }

    // Test token that allows movement through it
    private data class PassableToken(
        override val id: Id<Token> = Id.generate()
    ) : Token {
        override val allowsMovement: Boolean = true
    }

    // Test token that blocks sight
    private data class OpaqueToken(
        override val id: Id<Token> = Id.generate()
    ) : Token {
        override val allowsSight: Boolean = false
    }

    // Test token that allows sight
    private data class TransparentToken(
        override val id: Id<Token> = Id.generate()
    ) : Token {
        override val allowsSight: Boolean = true
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
            assertEquals(null, reach[GridIndex(1, 0)])
            // Should include other neighbors
            assertEquals(1, reach[GridIndex(1, 2)])
        }

        @Test
        fun `should return only start position when steps is zero`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)

            val reach = board.calculateReach(start, steps = 0)

            assertEquals(0, reach[GridIndex(5, 5)])
        }

        @Test
        fun `should reach all 8 neighbors with steps 1`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)

            val reach = board.calculateReach(start, steps = 1)

            assertEquals(0, reach[GridIndex(5, 5)])
            assertEquals(1, reach[GridIndex(4, 4)]) // NW
            assertEquals(1, reach[GridIndex(5, 4)]) // N
            assertEquals(1, reach[GridIndex(6, 4)]) // NE
            assertEquals(1, reach[GridIndex(4, 5)]) // W
            assertEquals(1, reach[GridIndex(6, 5)]) // E
            assertEquals(1, reach[GridIndex(4, 6)]) // SW
            assertEquals(1, reach[GridIndex(5, 6)]) // S
            assertEquals(1, reach[GridIndex(6, 6)]) // SE
        }

        @Test
        fun `should expand reach with higher steps`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)

            val reach = board.calculateReach(start, steps = 2)

            // Should reach positions 2 steps away
            assertEquals(2, reach[GridIndex(5, 3)]) // 2 steps north
            assertEquals(2, reach[GridIndex(7, 5)]) // 2 steps east
            assertEquals(2, reach[GridIndex(3, 5)]) // 2 steps west
            assertEquals(2, reach[GridIndex(5, 7)]) // 2 steps south
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
            assertEquals(null, reach[GridIndex(5, 4)])

            // Should still reach other directions
            assertEquals(1, reach[GridIndex(5, 6)])
            assertEquals(1, reach[GridIndex(6, 5)])
            assertEquals(1, reach[GridIndex(4, 5)])
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
            assertEquals(1, reach[GridIndex(5, 4)])
            assertEquals(2, reach[GridIndex(5, 3)])
        }

        @Test
        fun `should route around blocking tokens`() {
            // Grid layout (showing relevant area):
            //   3 4 5 6 7
            // 3 . . t . .  <- we test for (t) to be reachable
            // 4 . X X t .  <- Two blocking tokens (X) to the north
            // 5 . . S . .  <- Start position (S) at (5, 5)
            // 6 . . . . .
            // 7 . . . . .
            //
            // With steps=2, should reach around the wall but not through it

            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)
            val blockingToken1 = BlockingToken()
            val blockingToken2 = BlockingToken()

            // Create a wall to the north
            board.putTokenTo(BoardPosition.from(4, 4), blockingToken1)
            board.putTokenTo(BoardPosition.from(5, 4), blockingToken2)

            val reach = board.calculateReach(start, steps = 2)

            // Can reach positions around the wall
            assertEquals(1, reach[GridIndex(6, 4)]) // NE is reachable
            assertEquals(2, reach[GridIndex(5, 3)]) // Can reach to the west

            // The blocked positions should not be reachable
            assertEquals(null, reach[GridIndex(4, 4)])
            assertEquals(null, reach[GridIndex(5, 4)])
        }

        @Test
        fun `should respect board boundaries`() {
            val board = GameBoard(5, 5)
            val start = BoardPosition.from(0, 0)

            val reach = board.calculateReach(start, steps = 2)

            assertEquals(0, reach[GridIndex(0, 0)])
            assertEquals(1, reach[GridIndex(1, 0)])
            assertEquals(1, reach[GridIndex(0, 1)])
            assertEquals(2, reach[GridIndex(2, 2)])
        }

        @Test
        fun `should handle large steps values`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(0, 0)

            val reach = board.calculateReach(start, steps = 20)

            // Should reach diagonal corner
            assertEquals(9, reach[GridIndex(9, 9)]) // Diagonal distance
        }

        @Test
        fun `should handle starting from edge`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(0, 0)

            val reach = board.calculateReach(start, steps = 1)

            // Should reach start and 3 neighbors
            assertEquals(0, reach[GridIndex(0, 0)])
            assertEquals(1, reach[GridIndex(1, 0)])
            assertEquals(1, reach[GridIndex(0, 1)])
            assertEquals(1, reach[GridIndex(1, 1)])
        }

        @Test
        fun `should reject negative steps`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)

            val exception = assertThrows<IllegalArgumentException> {
                board.calculateReach(start, steps = -1)
            }

            assertEquals("Steps must be non-negative, but was -1", exception.message)
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
            assertEquals(0, reach[GridIndex(3, 3)])
            // Verify neighbors are not reachable
            surroundingPairs().forEach { (x, y) ->
                assertEquals(null, reach[GridIndex(x, y)])
            }
        }

        @Test
        fun `should reach diagonal from one corner to the other`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(0, 0)

            val reach = board.calculateReach(start, steps = 10)

            (1..9).forEach { i ->
                assertEquals(i, reach[GridIndex(i, i)])
             }
        }
    }

    @Nested
    inner class HasLineOfSight {

        @Test
        fun `should have clear line of sight with no obstacles`() {
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(0, 0)
            val to = BoardPosition.from(5, 5)

            assertEquals(true, board.hasLineOfSight(from, to))
        }

        @Test
        fun `should have line of sight to adjacent position`() {
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(5, 5)
            val to = BoardPosition.from(5, 6)

            assertEquals(true, board.hasLineOfSight(from, to))
        }

        @Test
        fun `should have line of sight to same position`() {
            val board = GameBoard(10, 10)
            val position = BoardPosition.from(5, 5)

            assertEquals(true, board.hasLineOfSight(position, position))
        }

        @Test
        fun `should block line of sight with opaque token in the way`() {
            // Grid layout:
            //   3 4 5 6 7
            // 3 F . . . .  <- From position
            // 4 . X . . .  <- Opaque token (X) blocks sight
            // 5 . . T . .  <- To position
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(3, 3)
            val to = BoardPosition.from(5, 5)

            board.putTokenTo(BoardPosition.from(4, 4), OpaqueToken())

            assertEquals(false, board.hasLineOfSight(from, to))
        }

        @Test
        fun `should allow line of sight through transparent token`() {
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(0, 0)
            val to = BoardPosition.from(4, 4)

            board.putTokenTo(BoardPosition.from(2, 2), TransparentToken())

            assertEquals(true, board.hasLineOfSight(from, to))
        }

        @Test
        fun `should work in all directions horizontally`() {
            val board = GameBoard(10, 10)
            val center = BoardPosition.from(5, 5)

            assertEquals(true, board.hasLineOfSight(center, BoardPosition.from(0, 5))) // West
            assertEquals(true, board.hasLineOfSight(center, BoardPosition.from(9, 5))) // East
        }


        @Test
        fun `should be symmetric - same result from both directions`() {
            val board = GameBoard(10, 10)
            val pos1 = BoardPosition.from(2, 2)
            val pos2 = BoardPosition.from(7, 7)

            board.putTokenTo(BoardPosition.from(4, 4), OpaqueToken())

            assertEquals(false, board.hasLineOfSight(pos1, pos2))
            assertEquals(false, board.hasLineOfSight(pos2, pos1))
        }

        @Test
        fun `should block with multiple opaque tokens in line`() {
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(0, 0)
            val to = BoardPosition.from(6, 6)

            board.putTokenTo(BoardPosition.from(2, 2), OpaqueToken())
            board.putTokenTo(BoardPosition.from(4, 4), OpaqueToken())

            assertEquals(false, board.hasLineOfSight(from, to))
        }

        @Test
        fun `should have line of sight when obstacle is at endpoint`() {
            // Start and end positions are excluded from blocking checks
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(0, 0)
            val to = BoardPosition.from(5, 5)

            board.putTokenTo(to, OpaqueToken())

            assertEquals(true, board.hasLineOfSight(from, to))
        }

        @Test
        fun `should block sight with token just off the diagonal`() {
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(0, 0)
            val to = BoardPosition.from(8, 6)

            // Place opaque token along the line
            board.putTokenTo(BoardPosition.from(4, 3), OpaqueToken())

            assertEquals(false, board.hasLineOfSight(from, to))
        }
    }
}
