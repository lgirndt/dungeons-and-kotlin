package io.dungeons.domain.savegame

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.core.Player
import io.dungeons.domain.world.Room
import io.dungeons.port.Id
import kotlin.time.Instant

data class SaveGame(
    val id: Id<SaveGame> = Id.generate(),
    val playerId: Id<Player>,
    val adventureId: Id<Adventure>,
    val currentRoomId: Id<Room>,
    val savedAt: Instant,
)
