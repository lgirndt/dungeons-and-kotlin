package io.dungeons.domain.savegame

import io.dungeons.port.ListSaveGamesQuery
import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameSummaryResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

private fun SaveGame.toSummaryResponse() = SaveGameSummaryResponse(
    id = this.id.castTo(),
    savedAt = this.savedAt,
)

@Component
class ListSaveGamesQueryImpl(private val saveGameRepository: SaveGameRepository) : ListSaveGamesQuery {
    override fun query(playerId: PlayerId): List<SaveGameSummaryResponse> = saveGameRepository
        .findAllByUserId(playerId)
        .map(SaveGame::toSummaryResponse)
        .also {
            logger.debug { "Found ${it.size} save games for player $playerId" }
        }
}
