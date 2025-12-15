package io.dungeons.domain.savegame

import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameId
import java.util.*

interface SaveGameRepository {
    fun save(saveGame: SaveGame)

    fun findByUserId(userId: PlayerId, saveGameId: SaveGameId): Optional<SaveGame>

    fun findAllByUserId(userId: PlayerId): List<SaveGame>
}
