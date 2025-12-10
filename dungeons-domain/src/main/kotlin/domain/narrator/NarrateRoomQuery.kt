package io.dungeons.domain.narrator

import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.core.Id
import io.dungeons.domain.core.User
import io.dungeons.domain.savegame.SaveGame
import io.dungeons.domain.savegame.SaveGameRepository
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
class NarrateRoomQuery(
    private val saveGameRepository: SaveGameRepository,
    private val adventureRepository: AdventureRepository,
) {
    fun execute(userId: Id<User>, saveGameId: Id<SaveGame>): NarratedRoom? {
        val saveGame = saveGameRepository.findByUserId(userId, saveGameId) ?: return null
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
