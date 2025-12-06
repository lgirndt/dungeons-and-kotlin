package io.dungeons.domain.savegame

import io.dungeons.domain.core.Id
import io.dungeons.domain.core.User
import java.util.concurrent.ConcurrentHashMap

class MockSaveGameRepository : SaveGameRepository {

    private val storage: MutableMap<Id<User>, SaveGame> = ConcurrentHashMap()

    override fun save(saveGame: SaveGame) {
        storage[saveGame.userId] = saveGame
    }

    override fun findByUserId(userId: Id<User>): SaveGame? = storage[userId]
}