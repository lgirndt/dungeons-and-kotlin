package io.dungeons.domain.savegame

import io.dungeons.domain.player.Player
import io.dungeons.port.Id
import java.util.*

interface SaveGameRepository {
    fun save(saveGame: SaveGame)

    fun findByUserId(userId: Id<Player>, saveGameId: Id<SaveGame>): Optional<SaveGame>

    fun findAllByUserId(userId: Id<Player>): List<SaveGame>
}
