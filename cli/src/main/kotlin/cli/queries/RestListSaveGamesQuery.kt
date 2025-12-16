package io.dungeons.cli.queries

import io.dungeons.port.ListSaveGamesQuery
import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameSummaryResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

private val logger = KotlinLogging.logger {}

@Component
class RestListSaveGamesQuery(private val restClient: RestClient) : ListSaveGamesQuery {

    override fun query(playerId: PlayerId): List<SaveGameSummaryResponse> {
        val result = restClient
            .get()
            .uri("/games", playerId)
            .retrieve()
            .body<List<SaveGameSummaryResponse>>()
            .also {
                logger.debug { "Found ${it?.size} save games for player $playerId" }
            }
        return result ?: error("something went wrong")
    }
}