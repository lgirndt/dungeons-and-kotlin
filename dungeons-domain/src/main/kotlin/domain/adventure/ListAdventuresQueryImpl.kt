package io.dungeons.domain.adventure

import io.dungeons.port.AdventureSummaryResponse
import io.dungeons.port.ListAdventuresQuery
import org.springframework.stereotype.Component

private fun Adventure.toSummaryResponse() = AdventureSummaryResponse(id, name)

@Component
class ListAdventuresQueryImpl(private val adventureRepository: AdventureRepository) : ListAdventuresQuery {
    override fun query() = adventureRepository.findAll().map(Adventure::toSummaryResponse)
}
