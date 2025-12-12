package io.dungeons.domain.savegame

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.port.Id
import org.springframework.stereotype.Component
import java.util.*
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

    override fun execute(playerId: UUID, adventureId: UUID): Id<SaveGame> {
        logger.info("New game with id $playerId")
        val domainAdventureId = Id.fromUUID<Adventure>(adventureId)
        val adventure = adventureRepository.findById(domainAdventureId).getOrNull()
            ?: error("Cannot find adventure with id $adventureId")
        val saveGame = SaveGame(
            playerId = Id.fromUUID(playerId),
            adventureId = domainAdventureId,
            currentRoomId = adventure.initialRoomId,
            savedAt = clock.now(),
        )
        saveGameRepository.save(saveGame)
        return saveGame.id
    }
}
