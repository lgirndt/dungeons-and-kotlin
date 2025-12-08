package io.dungeons.domain.narrator

import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.core.Id
import io.dungeons.domain.core.User
import io.dungeons.domain.savegame.SaveGame
import io.dungeons.domain.savegame.SaveGameRepository

class NarrateRoomQuery(
    private val saveGameRepository: SaveGameRepository,
    private val adventureRepository: AdventureRepository,
) {
    fun execute(userId: Id<User>, saveGameId: Id<SaveGame>) : NarratedRoom? {
        val saveGame = saveGameRepository.findByUserId(userId, saveGameId)
        require(saveGame != null) { "No save game found for user $userId" }

        val adventure = adventureRepository.findByIdOrNull(saveGame.adventureId)
        require(adventure != null) { "No adventure found for id ${saveGame.adventureId}" }

        return NarratedRoom(
            roomId = saveGame.currentRoomId,
            party = Party(
                heroes = listOf(
                    Hero(name="Aragorn"),
                    Hero(name="Legolas"),
                )
            ),
            readOut =  "You are in a dark room. There is a door to the north.",
        )
    }
}