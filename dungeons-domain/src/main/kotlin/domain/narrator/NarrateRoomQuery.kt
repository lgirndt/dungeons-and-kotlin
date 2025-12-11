package io.dungeons.domain.narrator

import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.core.Player
import io.dungeons.domain.savegame.SaveGame
import io.dungeons.domain.savegame.SaveGameRepository
import io.dungeons.port.Id
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
class NarrateRoomQuery(
    private val saveGameRepository: SaveGameRepository,
    private val adventureRepository: AdventureRepository,
) {
    fun execute(userId: Id<Player>, saveGameId: Id<SaveGame>): NarratedRoom? {
        val saveGame = saveGameRepository.findByUserId(userId, saveGameId).getOrNull()
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
            )
        }
    }
}
