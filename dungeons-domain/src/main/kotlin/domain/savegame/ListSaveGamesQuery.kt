package io.dungeons.domain.savegame

import io.dungeons.domain.core.Player
import io.dungeons.port.Id
import org.springframework.stereotype.Component

@Component
class ListSaveGamesQuery(private val saveGameRepository: SaveGameRepository) {
    fun query(userId: Id<Player>): List<SaveGame> = saveGameRepository.findAllByUserId(userId)
}
