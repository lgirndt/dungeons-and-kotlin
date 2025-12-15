package io.dungeons.domain.world

import io.dungeons.port.Id
import io.dungeons.port.RoomId
import io.dungeons.port.WorldId
import io.dungeons.port.WorldStateId

enum class Direction {
    North,
    East,
    South,
    West,
}

data class Room(
    val id: RoomId = Id.generate(),
    val name: String,
    val description: String,
    val connections: Map<Direction, RoomId> = emptyMap(),
)

class World(val id: WorldId, val name: String, val description: String, val rooms: List<Room>) {
    fun getRoomById(roomId: RoomId): Room? = rooms.find { it.id == roomId }
}

data class WorldState(val id: WorldStateId, val worldId: WorldId, val currentRoom: RoomId)

data class WorldCoord(val x: Int, val y: Int) {
    fun toAdjacentDirections(): Map<Direction, WorldCoord> = mapOf(
        Direction.West to WorldCoord(x - 1, y), // West
        Direction.East to WorldCoord(x + 1, y), // East
        Direction.North to WorldCoord(x, y - 1), // North
        Direction.South to WorldCoord(x, y + 1), // South
    )
}

class WorldBuilder {
    private val rooms: MutableMap<WorldCoord, Room> = mutableMapOf()

    fun room(x: Int, y: Int, room: Room): WorldBuilder {
        rooms[WorldCoord(x, y)] = room
        return this
    }

    fun build(): World {
        val connectedRooms = rooms.map { (coord, room) ->
            val connectedTo: Map<Direction, RoomId> = coord
                .toAdjacentDirections()
                .mapNotNull { (direction, coord) ->
                    rooms[coord]?.let { adjacentRoom -> direction to adjacentRoom.id }
                }
                .toMap()
            room.copy(connections = connectedTo)
        }

        return World(
            id = Id.generate(),
            name = "My World",
            description = "A newly created world",
            rooms = connectedRooms,
        )
    }
}
