package io.dungeons.domain.world

import io.dungeons.domain.core.Id

enum class Direction {
    North,
    East,
    South,
    West,
}

data class Room(
    val id: Id<Room> = Id.generate(),
    val name: String,
    val description: String,
    val connections: Map<Direction, Id<Room>> = emptyMap(),
)

class World(val id: Id<World>, val name: String, val description: String, val rooms: List<Room>) {
    fun getRoomById(roomId: Id<Room>): Room? = rooms.find { it.id == roomId }
}

data class WorldState(val id: Id<WorldState>, val worldId: Id<World>, val currentRoom: Id<Room>)

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
            val connectedTo: Map<Direction, Id<Room>> = coord
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
