package io.dungeons.domain.savegame

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import io.dungeons.domain.world.Room
import org.springframework.data.annotation.Id as MongoId

data class SaveGame(
    @MongoId val id: Id<SaveGame> = Id.generate(),
    val userId: Id<Player>,
    val adventureId: Id<Adventure>,
    val currentRoomId: Id<Room>,
)
