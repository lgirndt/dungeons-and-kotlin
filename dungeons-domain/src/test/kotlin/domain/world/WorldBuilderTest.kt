package domain.world

import domain.TestId
import io.dungeons.domain.core.Id
import io.dungeons.domain.world.Direction
import io.dungeons.domain.world.Room
import io.dungeons.domain.world.World
import io.dungeons.domain.world.WorldBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

private val ROOM_ID = TestId<Room>()

private fun World.roomOrFail(id: Id<Room>): Room = this.getRoomById(id)
    ?: fail { "room with id $id should exist in the world" }

private fun Room.connectionSet(): Set<Pair<Direction, Id<Room>>> =
    this.connections.map { (key, value) -> key to value }.toSet()

class WorldBuilderTest {
    @Test
    fun `should associate rooms correctly`() {
        val world = WorldBuilder()
            .room(x = 1, y = 1, Room(id = ROOM_ID[1], name = "Room 1", description = "The first room"))
            .room(x = 1, y = 2, Room(id = ROOM_ID[2], name = "Room 2", description = "The second room"))
            .room(x = 2, y = 1, Room(id = ROOM_ID[3], name = "Room 3", description = "The third room"))
            .build()

        assertEquals(
            setOf(
                Direction.South to ROOM_ID[2],
                Direction.East to ROOM_ID[3],
            ),
            world.roomOrFail(ROOM_ID[1]).connectionSet(),
        )

        assertEquals(
            setOf(
                Direction.North to ROOM_ID[1],
            ),
            world.roomOrFail(ROOM_ID[2]).connectionSet(),
        )

        assertEquals(
            setOf(
                Direction.West to ROOM_ID[1],
            ),
            world.roomOrFail(ROOM_ID[3]).connectionSet(),
        )
    }
}
