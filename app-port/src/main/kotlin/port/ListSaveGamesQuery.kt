package io.dungeons.port

import java.util.*
import kotlin.time.Instant

data class SaveGameSummaryResponse(
    val id: UUID,
    val savedAt: Instant,
)

interface ListSaveGamesQuery {
    fun query(playerId: UUID): List<SaveGameSummaryResponse>
}
