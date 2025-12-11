package io.dungeons.domain.savegame

import io.dungeons.port.Id
import org.springframework.stereotype.Component
import java.util.*
import java.util.logging.Logger
import kotlin.time.Clock

@Component
class CreateNewGameUseCaseImpl(private val saveGameRepository: SaveGameRepository, private val clock: Clock) :
    CreateNewGameUseCase {
    private val logger = Logger.getLogger(CreateNewGameUseCase::class.java.name)

    override fun execute(playerId: UUID, adventureId: UUID, initialRoomId: UUID): Id<SaveGame> {
        logger.info("New game with id $playerId")
        val saveGame = SaveGame(
            playerId = Id.fromUUID(playerId),
            adventureId = Id.fromUUID(adventureId),
            currentRoomId = Id.fromUUID(initialRoomId),
            savedAt = clock.now(),
        )
        saveGameRepository.save(saveGame)
        return saveGame.id
    }
}
