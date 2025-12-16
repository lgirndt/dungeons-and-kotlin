package io.dungeons.cli.queries

import io.dungeons.port.ListSaveGamesQuery
import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameSummaryResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class RestListSaveGamesQuery(private val restClient: RestClient) : ListSaveGamesQuery {

    override fun query(playerId: PlayerId): List<SaveGameSummaryResponse> =
        restClient
            .get()
            .uri("/games", playerId)
            .retrieve()
            .body<List<SaveGameSummaryResponse>>()
            .orEmpty()
}