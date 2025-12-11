package io.dungeons.domain.savegame

import io.dungeons.domain.core.Id

val SOME_SAVE_GAME = SaveGame(
    id = Id.generate(),
    playerId = Id.generate(),
    adventureId = Id.generate(),
    currentRoomId = Id.generate(),
    savedAt = kotlinx.datetime.LocalDateTime // .of(2025,1,2,3,4,5).toInstant(ZoneOffset.UTC)
)
