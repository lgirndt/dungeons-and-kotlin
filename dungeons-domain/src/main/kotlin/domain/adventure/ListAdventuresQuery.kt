package io.dungeons.domain.adventure

import org.springframework.stereotype.Component
import java.util.*

data class AdventureSummaryResponse(
    val id: UUID,
    val name: String,
    val initialRoomId: UUID,
)

private fun Adventure.toSummaryResponse() = AdventureSummaryResponse(
    id = this.id.toUUID(),
    name = this.name,
    initialRoomId = this.initialRoomId.toUUID(),
)

interface ListAdventuresQuery{
    fun query() : List<AdventureSummaryResponse>
}


@Component
class ListAdventuresQueryImpl(private val adventureRepository: AdventureRepository) {
    fun query() = adventureRepository.findAll().map(Adventure::toSummaryResponse)
}
