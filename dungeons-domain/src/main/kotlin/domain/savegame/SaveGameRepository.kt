package io.dungeons.domain.savegame

import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import java.util.*

interface SaveGameRepository {
    fun save(saveGame: SaveGame)

    fun findByUserId(userId: Id<Player>, saveGameId: Id<SaveGame>): Optional<SaveGame>

    fun findAllByUserId(userId: Id<Player>): List<SaveGame>
}
