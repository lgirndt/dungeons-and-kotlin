package io.dungeons.app.rest

import io.dungeons.app.rest.dto.GameIdResponse
import io.dungeons.domain.savegame.CreateNewGameRequest
import io.dungeons.domain.savegame.CreateNewGameUseCase
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GameController(private val createNewGameUseCase: CreateNewGameUseCase) {

    @PostMapping("/game")
    fun createGame(@RequestBody request: CreateNewGameRequest): GameIdResponse {
        return GameIdResponse(createNewGameUseCase.execute(request).toUUID())
    }
}
