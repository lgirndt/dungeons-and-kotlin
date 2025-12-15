package io.dungeons.port.usecases

import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameId
import java.util.*

data class CreateNewGameRequest(val playerId: PlayerId, val adventureId: UUID)

interface CreateNewGameUseCase {
    fun execute(request: CreateNewGameRequest): SaveGameId
}
