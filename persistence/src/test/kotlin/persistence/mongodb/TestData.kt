package io.dungeons.persistence.mongodb

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import io.dungeons.domain.savegame.SaveGame
import io.dungeons.domain.world.Room

val SOME_PLAYER = Player(id = Id.generate())

val SOME_SAVE_GAME = SaveGame(
    id = Id.generate(),
    userId = SOME_PLAYER.id,
    adventureId = Id.generate<Adventure>(),
    currentRoomId = Id.generate<Room>(),
)
