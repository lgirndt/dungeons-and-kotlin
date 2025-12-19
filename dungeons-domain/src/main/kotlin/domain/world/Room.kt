package io.dungeons.domain.world

import io.dungeons.port.DoorId
import io.dungeons.port.Id
import io.dungeons.port.RoomId
import io.dungeons.port.WorldId

enum class Direction {
    North,
    East,
    South,
    West,
}

data class Door(
    val id: DoorId,
    val direction: Direction,
    val leadsTo: RoomId,
)

data class Room(
    val id: RoomId = Id.generate(),
    val name: String,
    val description: String,
    val doors : List<Door> = emptyList(),

)

class World(val id: WorldId, val name: String, val description: String, val rooms: List<Room>) {
    fun getRoomById(roomId: RoomId): Room? = rooms.find { it.id == roomId }
}

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
            val connectedTo: List<Door> = coord
                .toAdjacentDirections()
                .mapNotNull { (direction, coord) ->
                    rooms[coord]?.let {
                        adjacentRoom ->
                        Door(
                            id = Id.generate(),
                            direction = direction,
                            leadsTo = adjacentRoom.id,
                        )
                    }
                }
            room.copy(doors = connectedTo)
        }

        return World(
            id = Id.generate(),
            name = "My World",
            description = "A newly created world",
            rooms = connectedRooms,
        )
    }
}
