package io.dungeons.domain.savegame

import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import org.springframework.stereotype.Component

@Component
class ListSaveGamesQuery(private val saveGameRepository: SaveGameRepository) {
    fun execute(userId: Id<Player>): List<SaveGame> = saveGameRepository.findAllByUserId(userId)
}
