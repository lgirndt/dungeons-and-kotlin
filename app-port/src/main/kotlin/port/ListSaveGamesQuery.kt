package io.dungeons.port

import kotlin.time.Instant

data class SaveGameSummaryResponse(val id: SaveGameId, val savedAt: Instant)

interface ListSaveGamesQuery {
    fun query(playerId: PlayerId): List<SaveGameSummaryResponse>
}
