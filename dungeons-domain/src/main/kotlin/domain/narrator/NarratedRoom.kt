package io.dungeons.domain.narrator

import io.dungeons.port.RoomId

data class Hero(val name: String)

data class Party(val heroes: List<Hero>)

data class NarratedRoom(val roomId: RoomId, val readOut: String, val party: Party)
