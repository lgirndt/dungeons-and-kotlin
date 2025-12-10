package io.dungeons.persistence.mongodb

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import io.dungeons.domain.savegame.SaveGame
import io.dungeons.domain.world.Room

val SOME_PLAYER = Player(id = Id.generate())

val SOME_ROOM = Room(
    id = Id.generate(),
    name = "Test Room",
    description = "A test room for unit tests",
    connections = emptyMap(),
)

val SOME_ADVENTURE = Adventure(
    id = Id.generate(),
    name = "Test Adventure",
    initialRoomId = Id.generate(),
    rooms = listOf(),
)

val SOME_SAVE_GAME = SaveGame(
    id = Id.generate(),
    userId = Id.generate(),
    adventureId = Id.generate(),
    currentRoomId = Id.generate(),
)
