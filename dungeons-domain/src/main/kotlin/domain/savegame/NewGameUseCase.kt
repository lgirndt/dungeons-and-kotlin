package io.dungeons.domain.savegame

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import org.springframework.stereotype.Component
import java.util.logging.Logger

@Component
class NewGameUseCase(private val saveGameRepository: SaveGameRepository) {
    private val logger = Logger.getLogger(NewGameUseCase::class.java.name)

    fun execute(userId: Id<Player>, adventure: Adventure): Id<SaveGame> {
        logger.info("New game with id $userId")
        val saveGame = SaveGame(
            playerId = userId,
            adventureId = adventure.id,
            currentRoomId = adventure.initialRoomId,
        )
        saveGameRepository.save(saveGame)
        return saveGame.id
    }
}
