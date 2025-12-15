package io.dungeons.domain.savegame

import io.dungeons.port.AdventureId
import io.dungeons.port.Id
import io.dungeons.port.PlayerId
import io.dungeons.port.RoomId
import io.dungeons.port.SaveGameId
import kotlin.time.Instant

data class SaveGame(
    val id: SaveGameId = Id.generate(),
    val playerId: PlayerId,
    val adventureId: AdventureId,
    val currentRoomId: RoomId,
    val savedAt: Instant,
)
