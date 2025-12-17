package io.dungeons.cli.usecases

import io.dungeons.port.usecases.CreateNewGameRequest
import io.dungeons.port.usecases.CreateNewGameUseCase
import io.dungeons.port.usecases.GameIdResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class RestCreateNewGameUseCase(private val restClient: RestClient) : CreateNewGameUseCase {
    override fun execute(request: CreateNewGameRequest): GameIdResponse = restClient
        .post()
        .uri("/game")
        .body(request)
        .retrieve()
        .body<GameIdResponse>()
        ?: error("Game could not be created")
}
