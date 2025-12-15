package io.dungeons.port

import java.util.*

data class HeroResponse(val name: String)

data class PartyResponse(val heroes: List<HeroResponse>)

data class NarratedRoomResponse(val roomId: UUID, val readOut: String, val party: PartyResponse)

interface NarrateRoomQuery {
    fun query(userId: PlayerId, saveGameId: UUID): NarratedRoomResponse?
}
