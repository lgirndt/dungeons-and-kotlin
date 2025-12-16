package io.dungeons.cli.queries

import io.dungeons.port.NarrateRoomQuery
import io.dungeons.port.NarratedRoomResponse
import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameId
import org.springframework.stereotype.Component

@Component
class RestNarrateRoomQuery : NarrateRoomQuery {
    override fun query(
        userId: PlayerId,
        saveGameId: SaveGameId,
    ): NarratedRoomResponse? {
        TODO("Not yet implemented")
    }
}