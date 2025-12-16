package io.dungeons.port.usecases

import io.dungeons.port.AdventureId
import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameId

data class CreateNewGameRequest(val playerId: PlayerId, val adventureId: AdventureId)

interface CreateNewGameUseCase {
    fun execute(request: CreateNewGameRequest): SaveGameId
}
