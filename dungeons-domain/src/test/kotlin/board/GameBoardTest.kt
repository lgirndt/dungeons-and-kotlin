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

    // Test tokens for different layers
    private data class GroundToken(
        override val id: Id<Token> = Id.generate(),
        override val allowsMovement: Boolean = true,
        override val allowsSight: Boolean = true
    ) : Token {
        override val layer: BoardLayer = BoardLayer.GROUND
    }

    private data class ObjectToken(
        override val id: Id<Token> = Id.generate(),
        override val allowsMovement: Boolean = false,
        override val allowsSight: Boolean = false
    ) : Token {
        override val layer: BoardLayer = BoardLayer.OBJECT
    }

    private data class CreatureToken(
        override val id: Id<Token> = Id.generate(),
        override val allowsMovement: Boolean = false,
        override val allowsSight: Boolean = true
    ) : Token {
        override val layer: BoardLayer = BoardLayer.CREATURE
    }

    @Nested
    inner class MultiLayerTokens {

        @Test
        fun `should allow placing tokens on different layers at same position`() {
            val board = GameBoard(10, 10)
            val position = BoardPosition.from(5, 5)

            val ground = GroundToken()
            val obj = ObjectToken()
            val creature = CreatureToken()

            board.putTokenTo(position, ground)
            board.putTokenTo(position, obj)
            board.putTokenTo(position, creature)

            assertThat(
                board.getTokenAt(BoardLayer.GROUND, position)?.id,
                equalTo(ground.id)
            )
            assertThat(
                board.getTokenAt(BoardLayer.OBJECT, position)?.id,
                equalTo(obj.id)
            )
            assertThat(
                board.getTokenAt(BoardLayer.CREATURE, position)?.id,
                equalTo(creature.id)
            )
        }

        @Test
        fun `should reject duplicate token on same layer`() {
            val board = GameBoard(10, 10)
            val position = BoardPosition.from(5, 5)

            val ground1 = GroundToken()
            val ground2 = GroundToken()

            board.putTokenTo(position, ground1)

            assertThrows<IllegalArgumentException> {
                board.putTokenTo(position, ground2)
            }
        }

        @Test
        fun `should block movement if any layer blocks movement`() {
            // Grid layout:
            //   4 5 6
            // 4 . . .
            // 5 . S .  <- Start position
            // 6 . X .  <- Position with blocking object token
            //
            // Even with passable ground layer, object layer blocks movement

            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)
            val blockedPos = BoardPosition.from(5, 6)

            // Place passable ground token and blocking object token at same position
            board.putTokenTo(blockedPos, GroundToken(allowsMovement = true))
            board.putTokenTo(blockedPos, ObjectToken(allowsMovement = false))

            val reach = board.calculateReach(start, steps = 1)

            assertThat(reach[GridIndex(5, 6)], equalTo(null)) // Blocked by object layer
            assertThat(reach[GridIndex(5, 4)], equalTo(1))    // North is reachable
        }

        @Test
        fun `should allow movement only if all layers allow movement`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)
            val targetPos = BoardPosition.from(5, 6)

            // All layers allow movement
            board.putTokenTo(targetPos, GroundToken(allowsMovement = true))
            board.putTokenTo(targetPos, ObjectToken(allowsMovement = true))
            board.putTokenTo(targetPos, CreatureToken(allowsMovement = true))

            val reach = board.calculateReach(start, steps = 1)

            assertThat(reach[GridIndex(5, 6)], equalTo(1)) // All layers allow movement
        }

        @Test
        fun `should block sight if any layer blocks sight`() {
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(3, 3)
            val to = BoardPosition.from(7, 7)
            val middlePos = BoardPosition.from(5, 5)

            // Place transparent ground but opaque object in the middle
            board.putTokenTo(middlePos, GroundToken(allowsSight = true))
            board.putTokenTo(middlePos, ObjectToken(allowsSight = false))

            assertThat(board.hasLineOfSight(from, to), equalTo(false))
        }

        @Test
        fun `should allow sight only if all layers allow sight`() {
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(0, 0)
            val to = BoardPosition.from(4, 4)
            val middlePos = BoardPosition.from(2, 2)

            // All layers allow sight
            board.putTokenTo(middlePos, GroundToken(allowsSight = true))
            board.putTokenTo(middlePos, ObjectToken(allowsSight = true))
            board.putTokenTo(middlePos, CreatureToken(allowsSight = true))

            assertThat(board.hasLineOfSight(from, to), equalTo(true))
        }

        @Test
        fun `should handle mixed layer properties independently`() {
            // Test that movement and sight blocking are independent
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)
            val targetPos = BoardPosition.from(5, 6)

            // Creature blocks movement but allows sight
            board.putTokenTo(targetPos, CreatureToken(allowsMovement = false, allowsSight = true))

            val reach = board.calculateReach(start, steps = 1)

            // Cannot move to position with creature
            assertThat(reach[GridIndex(5, 6)], equalTo(null))

            // But can see through it
            assertThat(board.hasLineOfSight(start, BoardPosition.from(5, 8)), equalTo(true))
        }

        @Test
        fun `should handle empty layers correctly`() {
            // Position with no tokens should allow both movement and sight
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(5, 5)

            // No tokens placed at emptyPos

            val reach = board.calculateReach(start, steps = 1)

            assertThat(reach[GridIndex(5, 6)], equalTo(1)) // Can move
            assertThat(board.hasLineOfSight(start, BoardPosition.from(5, 7)), equalTo(true)) // Can see
        }
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
            assertThat(reach[GridIndex(6, 4)], equalTo(1)) // NE is reachable
            assertThat(reach[GridIndex(5, 3)], equalTo(2)) // Can reach to the west

            // The blocked positions should not be reachable
            assertThat(reach[GridIndex(4, 4)], equalTo(null))
            assertThat(reach[GridIndex(5, 4)], equalTo(null))
        }

        @Test
        fun `should respect board boundaries`() {
            val board = GameBoard(5, 5)
            val start = BoardPosition.from(0, 0)

            val reach = board.calculateReach(start, steps = 2)

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

        @Test
        fun `should reach diagonal from one corner to the other`() {
            val board = GameBoard(10, 10)
            val start = BoardPosition.from(0, 0)

            val reach = board.calculateReach(start, steps = 10)

            (1..9).forEach { i ->
                assertThat(reach[GridIndex(i, i)], equalTo(i))
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

            assertThat(board.hasLineOfSight(from, to), equalTo(true))
        }

        @Test
        fun `should have line of sight to adjacent position`() {
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(5, 5)
            val to = BoardPosition.from(5, 6)

            assertThat(board.hasLineOfSight(from, to), equalTo(true))
        }

        @Test
        fun `should have line of sight to same position`() {
            val board = GameBoard(10, 10)
            val position = BoardPosition.from(5, 5)

            assertThat(board.hasLineOfSight(position, position), equalTo(true))
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

            assertThat(board.hasLineOfSight(from, to), equalTo(false))
        }

        @Test
        fun `should allow line of sight through transparent token`() {
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(0, 0)
            val to = BoardPosition.from(4, 4)

            board.putTokenTo(BoardPosition.from(2, 2), TransparentToken())

            assertThat(board.hasLineOfSight(from, to), equalTo(true))
        }

        @Test
        fun `should work in all directions horizontally`() {
            val board = GameBoard(10, 10)
            val center = BoardPosition.from(5, 5)

            assertThat(board.hasLineOfSight(center, BoardPosition.from(0, 5)), equalTo(true)) // West
            assertThat(board.hasLineOfSight(center, BoardPosition.from(9, 5)), equalTo(true)) // East
        }


        @Test
        fun `should be symmetric - same result from both directions`() {
            val board = GameBoard(10, 10)
            val pos1 = BoardPosition.from(2, 2)
            val pos2 = BoardPosition.from(7, 7)

            board.putTokenTo(BoardPosition.from(4, 4), OpaqueToken())

            assertThat(board.hasLineOfSight(pos1, pos2), equalTo(false))
            assertThat(board.hasLineOfSight(pos2, pos1), equalTo(false))
        }

        @Test
        fun `should block with multiple opaque tokens in line`() {
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(0, 0)
            val to = BoardPosition.from(6, 6)

            board.putTokenTo(BoardPosition.from(2, 2), OpaqueToken())
            board.putTokenTo(BoardPosition.from(4, 4), OpaqueToken())

            assertThat(board.hasLineOfSight(from, to), equalTo(false))
        }

        @Test
        fun `should have line of sight when obstacle is at endpoint`() {
            // Start and end positions are excluded from blocking checks
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(0, 0)
            val to = BoardPosition.from(5, 5)

            board.putTokenTo(to, OpaqueToken())

            assertThat(board.hasLineOfSight(from, to), equalTo(true))
        }

        @Test
        fun `should block sight with token just off the diagonal`() {
            val board = GameBoard(10, 10)
            val from = BoardPosition.from(0, 0)
            val to = BoardPosition.from(8, 6)

            // Place opaque token along the line
            board.putTokenTo(BoardPosition.from(4, 3), OpaqueToken())

            assertThat(board.hasLineOfSight(from, to), equalTo(false))
        }
    }
}
