package io.dungeons.api.rest

import io.dungeons.api.rest.dto.GameIdResponse
import io.dungeons.api.security.PlayerDetails
import io.dungeons.domain.savegame.CreateNewGameRequest
import io.dungeons.domain.savegame.CreateNewGameUseCase
import io.dungeons.port.ListSaveGamesQuery
import io.dungeons.port.SaveGameSummaryResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GameController(
    private val createNewGameUseCase: CreateNewGameUseCase,
    private val listSaveGamesQuery: ListSaveGamesQuery
) {

    @PostMapping("/game")
    fun createGame(@RequestBody request: CreateNewGameRequest): GameIdResponse {
        return GameIdResponse(createNewGameUseCase.execute(request).toUUID())
    }

    @GetMapping("/games")
    fun listAll(@AuthenticationPrincipal player: PlayerDetails?) : List<SaveGameSummaryResponse>
    = listSaveGamesQuery.query(player?.playerId ?: error("Player id is null"))

    @GetMapping("/foo")
    fun foo(auth: Authentication) {
        println("foo")
    }
}
