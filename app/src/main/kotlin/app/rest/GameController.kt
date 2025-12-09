package io.dungeons.app.rest

import io.dungeons.app.rest.dto.GameIdResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GameController {
    @PostMapping("/game")
    fun createGame(): GameIdResponse = GameIdResponse("game-id-placeholder")
}
