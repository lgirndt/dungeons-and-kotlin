package io.dungeons.domain.narrator

import io.dungeons.domain.core.Id
import io.dungeons.domain.world.Room

data class Hero(
    val name: String,
)

data class Party(
    val heroes: List<Hero>,
)

data class NarratedRoom(
    val roomId: Id<Room>,
    val readOut: String,
    val party: Party
) {

}