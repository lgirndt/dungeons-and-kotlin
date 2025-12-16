package io.dungeons.port

data class AdventureSummaryResponse(val id: AdventureId, val name: String)

interface ListAdventuresQuery {
    fun query(): List<AdventureSummaryResponse>
}
