package io.dungeons.domain.savegame

import io.dungeons.port.Id
import io.dungeons.port.PlayerId
import java.util.*

interface SaveGameRepository {
    fun save(saveGame: SaveGame)

    fun findByUserId(userId: PlayerId, saveGameId: Id<SaveGame>): Optional<SaveGame>

    fun findAllByUserId(userId: PlayerId): List<SaveGame>
}
