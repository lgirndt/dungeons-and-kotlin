package io.dungeons.domain.savegame

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.core.Id
import io.dungeons.domain.core.User
import io.dungeons.domain.world.Room

data class SaveGame(
    val id: Id<SaveGame> = Id.generate(),
    val userId: Id<User>,
    val adventureId: Id<Adventure>,
    val currentRoomId: Id<Room>,
)