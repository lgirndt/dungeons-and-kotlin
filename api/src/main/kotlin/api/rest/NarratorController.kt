package io.dungeons.api.rest

import io.dungeons.api.security.PlayerDetails
import io.dungeons.port.NarrateRoomQuery
import io.dungeons.port.NarratedRoomResponse
import io.dungeons.port.PlayerId
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class NarratorController(private val narrateRoomQuery: NarrateRoomQuery) {
    @GetMapping("/game/{gameId}/narrator/room")
    fun narrateRoom(
        @PathVariable gameId: String,
        @AuthenticationPrincipal player: PlayerDetails,
    ): NarratedRoomResponse = narrateRoomQuery.query(player.playerId, PlayerId.fromString(gameId)).getOrThrow()
}
