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


class World(
    val id: Id<World>,
    val name: String,
    val description: String,
    val rooms: List<Room>,
) {
    fun getRoomById(roomId: Id<Room>): Room? = rooms.find { it.id == roomId }
}

data class GameState(
    val id: Id<GameState>,
    val worldId: Id<World>,
    val currentRoom: Id<Room>,
)

fun createWorld() {
    val maxX = 3
    val maxY = 3
    val rooms: Array<Array<Room?>> = Array(maxX) { Array<Room?>(maxY) { null } }

    rooms[0][0] = Room(name = "Entrance Hall", description = "The grand entrance to the dungeon.")
    rooms[0][1] = Room(name = "Armory", description = "A room filled with old weapons and armor.")
    rooms[1][0] = Room(name = "Library", description = "Shelves of ancient books line the walls.")
    rooms[2][0] = Room(name = "Treasure Room", description = "A glittering room filled with gold and jewels.")

    for (x in 0 until maxX) {
        for (y in 0 until maxY) {
            rooms[x][y]?.let {
                println("Room at ($x, $y): ${it.name} - ${it.description}")
            }
        }
    }
}

data class WordCoord(val x: Int, val y: Int) {
    fun toAdjacentDirections(): Map<Direction, WordCoord> = mapOf(
        Direction.West to WordCoord(x - 1, y), // West
        Direction.East to WordCoord(x + 1, y), // East
        Direction.North to WordCoord(x, y - 1), // North
        Direction.South to WordCoord(x, y + 1), // South
    )
}

class WorldBuilder {
    private val rooms: MutableMap<WordCoord, Room> = mutableMapOf()

    fun room(x: Int, y: Int, room: Room): WorldBuilder {
        rooms[WordCoord(x, y)] = room
        return this
    }

    fun build(): World {
        val connectedRooms = rooms.map { (coord, room) ->
            val connectedTo : Map<Direction, Id<Room>> = coord
                .toAdjacentDirections()
                .mapNotNull { (direction, coord) ->
                    rooms[coord]?.let { adjacentRoom -> direction to adjacentRoom.id }
                }
                .associate { it }
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
