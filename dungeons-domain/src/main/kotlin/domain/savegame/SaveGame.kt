package io.dungeons.domain.savegame

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.world.Room
import io.dungeons.port.Id
import io.dungeons.port.PlayerId
import kotlin.time.Instant

data class SaveGame(
    val id: Id<SaveGame> = Id.generate(),
    val playerId: PlayerId,
    val adventureId: Id<Adventure>,
    val currentRoomId: Id<Room>,
    val savedAt: Instant,
)
