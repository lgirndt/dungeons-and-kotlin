package io.dungeons.domain.savegame

import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class MockSaveGameRepository : SaveGameRepository {
    private val storage: MutableMap<Id<Player>, SaveGame> = ConcurrentHashMap()

    override fun save(saveGame: SaveGame) {
        storage[saveGame.userId] = saveGame
    }

    override fun findByUserId(userId: Id<Player>, saveGameId: Id<SaveGame>): SaveGame? = storage[userId]
}
