package io.dungeons.domain.savegame

import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.port.usecases.CreateNewGameRequest
import io.dungeons.port.usecases.CreateNewGameUseCase
import io.dungeons.port.usecases.GameCreatedResponse
import org.springframework.stereotype.Component
import java.util.logging.Logger
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Clock

@Component
class CreateNewGameUseCaseImpl(
    private val saveGameRepository: SaveGameRepository,
    private val adventureRepository: AdventureRepository,
    private val clock: Clock,
) : CreateNewGameUseCase {
    private val logger = Logger.getLogger(CreateNewGameUseCase::class.java.name)

    override fun execute(request: CreateNewGameRequest): GameCreatedResponse {
        with(request) {
            logger.info("New game with id $playerId")
            val adventure = adventureRepository.findById(adventureId).getOrNull()
                ?: error("Cannot find adventure with id $adventureId")
            val saveGame = SaveGame(
                playerId = playerId,
                adventureId = adventureId,
                currentRoomId = adventure.initialRoomId,
                savedAt = clock.now(),
            )
            saveGameRepository.save(saveGame)
            return GameCreatedResponse(saveGame.id)
        }
    }
}
