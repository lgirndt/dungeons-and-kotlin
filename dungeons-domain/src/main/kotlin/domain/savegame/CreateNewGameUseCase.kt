package io.dungeons.domain.savegame

import io.dungeons.port.Id
import io.dungeons.port.PlayerId
import java.util.*

data class CreateNewGameRequest(val playerId: PlayerId, val adventureId: UUID)

interface CreateNewGameUseCase {
    fun execute(request: CreateNewGameRequest): Id<SaveGame>
}
