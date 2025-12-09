package io.dungeons.domain.savegame

import io.dungeons.domain.core.Id
import io.dungeons.domain.core.User
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class MockSaveGameRepository : SaveGameRepository {
    private val storage: MutableMap<Id<User>, SaveGame> = ConcurrentHashMap()

    override fun save(saveGame: SaveGame) {
        storage[saveGame.userId] = saveGame
    }

    override fun findByUserId(userId: Id<User>, saveGameId: Id<SaveGame>): SaveGame? = storage[userId]
}
