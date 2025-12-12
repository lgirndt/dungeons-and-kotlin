package io.dungeons.port

import java.util.*

data class AdventureSummaryResponse(val id: UUID, val name: String, val initialRoomId: UUID)

interface ListAdventuresQuery {
    fun query(): List<AdventureSummaryResponse>
}
