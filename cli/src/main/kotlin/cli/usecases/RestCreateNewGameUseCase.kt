package io.dungeons.cli.usecases

import io.dungeons.port.SaveGameId
import io.dungeons.port.usecases.CreateNewGameRequest
import io.dungeons.port.usecases.CreateNewGameUseCase
import org.springframework.stereotype.Component

@Component
class RestCreateNewGameUseCase : CreateNewGameUseCase {
    override fun execute(request: CreateNewGameRequest): SaveGameId {
        TODO("Not yet implemented")
    }
}