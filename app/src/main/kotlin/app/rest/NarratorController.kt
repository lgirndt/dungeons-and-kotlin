package io.dungeons.app.rest

import io.dungeons.app.security.PlayerDetails
import io.dungeons.port.NarrateRoomQuery
import io.dungeons.port.NarratedRoomResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class NarratorController(private val narrateRoomQuery: NarrateRoomQuery) {

    @GetMapping("/game/{gameId}/narrator/room")
    fun narrateRoom(
        @PathVariable gameId: String,
        @AuthenticationPrincipal player: PlayerDetails
    ) : NarratedRoomResponse {
        return narrateRoomQuery.query(player.playerId, UUID.fromString(gameId))
            ?: error("Narrated room does not exist")
    }
}