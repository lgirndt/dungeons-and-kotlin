package io.dungeons.domain.savegame

import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MockSaveGameRepository : SaveGameRepository {
    private val storage: MutableMap<Id<Player>, SaveGame> = ConcurrentHashMap()

    override fun save(saveGame: SaveGame) {
        storage[saveGame.userId] = saveGame
    }

    override fun findByUserId(userId: Id<Player>, saveGameId: Id<SaveGame>): Optional<SaveGame> =
        Optional.ofNullable(storage[userId])
}
