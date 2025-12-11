package io.dungeons.domain.savegame

import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MockSaveGameRepository : SaveGameRepository {
    private val storage: MutableMap<Id<SaveGame>, SaveGame> = ConcurrentHashMap()

    override fun save(saveGame: SaveGame) {
        storage[saveGame.id] = saveGame
    }

    override fun findByUserId(userId: Id<Player>, saveGameId: Id<SaveGame>): Optional<SaveGame> =
        Optional.ofNullable(storage[saveGameId])
            .filter { it.playerId == userId }

    override fun findAllByUserId(userId: Id<Player>): List<SaveGame> =
        storage.values.filter { it.playerId == userId }
}
