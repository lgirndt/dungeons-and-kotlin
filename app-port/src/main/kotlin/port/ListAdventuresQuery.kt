package io.dungeons.port

import java.util.*

data class AdventureSummaryResponse(val id: UUID, val name: String)

interface ListAdventuresQuery {
    fun query(): List<AdventureSummaryResponse>
}
