package io.dungeons.domain.savegame

import io.dungeons.domain.core.Id
import io.dungeons.domain.core.User

interface SaveGameRepository {
    fun save(saveGame: SaveGame)
    fun findByUserId(userId: Id<User>): SaveGame?
}