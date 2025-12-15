package io.dungeons.domain.narrator

import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.savegame.SaveGameRepository
import io.dungeons.port.HeroResponse
import io.dungeons.port.NarrateRoomQuery
import io.dungeons.port.NarratedRoomResponse
import io.dungeons.port.PartyResponse
import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameId
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

private fun Hero.toResponse() = HeroResponse(name = this.name)

private fun Party.toResponse() = PartyResponse(heroes = this.heroes.map(Hero::toResponse))

private fun NarratedRoom.toResponse() = NarratedRoomResponse(
    roomId = this.roomId.toUUID(),
    readOut = this.readOut,
    party = this.party.toResponse(),
)

@Component
class NarrateRoomQueryImpl(
    private val saveGameRepository: SaveGameRepository,
    private val adventureRepository: AdventureRepository,
) : NarrateRoomQuery {
    override fun query(userId: PlayerId, saveGameId: SaveGameId): NarratedRoomResponse? {
        val saveGameIdTyped = saveGameId
        val saveGame = saveGameRepository.findByUserId(userId, saveGameIdTyped).getOrNull()
            ?: error("Cannot find game with id $saveGameId")

        val adventure = adventureRepository.findById(saveGame.adventureId).getOrNull()

        return adventure?.let {
            NarratedRoom(
                roomId = saveGame.currentRoomId,
                party = Party(
                    heroes = listOf(
                        Hero(name = "Aragorn"),
                        Hero(name = "Legolas"),
                    ),
                ),
                readOut = "You are in a dark room. There is a door to the north.",
            ).toResponse()
        }
    }
}
