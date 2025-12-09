package io.dungeons.domain.savegame

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.core.Id
import io.dungeons.domain.core.User
import org.springframework.stereotype.Component
import java.util.logging.Logger.getLogger

@Component
class NewGameUseCase(private val saveGameRepository: SaveGameRepository) {
    private val logger = getLogger(NewGameUseCase::class.java.name)

    fun execute(userId: Id<User>, adventure: Adventure): Id<SaveGame> {
        logger.info("New game with id $userId")
        val saveGame = SaveGame(
            userId = userId,
            adventureId = adventure.id,
            currentRoomId = adventure.initialRoomId,
        )
        saveGameRepository.save(saveGame)
        return saveGame.id
    }
}
