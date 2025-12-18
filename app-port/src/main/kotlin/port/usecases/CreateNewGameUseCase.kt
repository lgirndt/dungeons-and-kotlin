package io.dungeons.port.usecases

import io.dungeons.port.AdventureId
import io.dungeons.port.PlayerId

data class CreateNewGameRequest(val playerId: PlayerId, val adventureId: AdventureId)

interface CreateNewGameUseCase {
    fun execute(request: CreateNewGameRequest): GameCreatedResponse
}
