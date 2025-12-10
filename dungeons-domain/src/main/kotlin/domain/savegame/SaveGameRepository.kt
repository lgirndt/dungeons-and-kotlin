package io.dungeons.domain.savegame

import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player

interface SaveGameRepository {
    fun save(saveGame: SaveGame)

    fun findByUserId(userId: Id<Player>, saveGameId: Id<SaveGame>): SaveGame?
}
