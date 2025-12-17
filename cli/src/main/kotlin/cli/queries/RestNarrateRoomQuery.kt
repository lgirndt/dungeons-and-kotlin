package io.dungeons.cli.queries

import io.dungeons.port.NarrateRoomQuery
import io.dungeons.port.NarratedRoomResponse
import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameId
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class RestNarrateRoomQuery(private val restClient: RestClient) : NarrateRoomQuery {
    override fun query(playerId: PlayerId, saveGameId: SaveGameId): Result<NarratedRoomResponse> =
        restClient
            .get()
            .uri("/game/{gameId}/narrator/room", saveGameId.value)
            .retrieve()
            .body<NarratedRoomResponse>()
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("Could not narrate room for game $saveGameId"))
}
