package io.dungeons.domain.savegame

import io.dungeons.port.Id
import kotlin.time.Instant

val SOME_SAVE_GAME = SaveGame(
    id = Id.generate(),
    playerId = Id.generate(),
    adventureId = Id.generate(),
    currentRoomId = Id.generate(),
    savedAt = Instant.fromEpochMilliseconds(1704151445000), // 2024-01-02 03:04:05 UTC
)
