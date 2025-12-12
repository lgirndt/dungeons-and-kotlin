package io.dungeons.domain.savegame

import io.dungeons.port.Id
import java.util.*

data class CreateNewGameRequest(val playerId: UUID, val adventureId: UUID)

interface CreateNewGameUseCase {
    fun execute(request: CreateNewGameRequest): Id<SaveGame>
}
