package io.dungeons.domain.savegame

import io.dungeons.port.Id
import io.dungeons.port.ListSaveGamesQuery
import io.dungeons.port.SaveGameSummaryResponse
import org.springframework.stereotype.Component
import java.util.*

private fun SaveGame.toSummaryResponse() = SaveGameSummaryResponse(
    id = this.id.toUUID(),
    savedAt = this.savedAt,
)

@Component
class ListSaveGamesQueryImpl(private val saveGameRepository: SaveGameRepository) : ListSaveGamesQuery {
    override fun query(playerId: UUID) =
        saveGameRepository.findAllByUserId(Id.fromUUID(playerId)).map(SaveGame::toSummaryResponse)
}
