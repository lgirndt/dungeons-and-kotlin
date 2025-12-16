package io.dungeons.cli.queries

import io.dungeons.port.AdventureSummaryResponse
import io.dungeons.port.ListAdventuresQuery
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class RestListAdventuresQuery(private val restClient: RestClient) : ListAdventuresQuery {

    override fun query(): List<AdventureSummaryResponse> {
        return restClient
            .get()
            .uri("/adventures/summaries")
            .retrieve()
            .body<List<AdventureSummaryResponse>>()
            .orEmpty()
    }
}