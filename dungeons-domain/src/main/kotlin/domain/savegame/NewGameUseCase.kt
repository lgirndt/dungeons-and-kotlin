package io.dungeons.domain.savegame


import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.core.Id
import java.util.*
import java.util.logging.Logger.getLogger

class NewGameUseCase(
    private val saveGameRepository: SaveGameRepository
) {
    private val logger = getLogger(NewGameUseCase::class.java.name)

    fun execute(userId: UUID, adventure: Adventure) : Id<SaveGame> {
        logger.info("New game with id $userId")
        val saveGame = SaveGame(
            userId = Id.fromUUID(userId),
            adventureId = adventure.id,
            currentRoomId = adventure.initialRoomId
        )
        saveGameRepository.save(saveGame)
        return saveGame.id
    }
}