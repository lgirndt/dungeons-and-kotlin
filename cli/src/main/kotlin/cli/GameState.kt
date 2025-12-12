package io.dungeons.cli

import io.dungeons.domain.savegame.SaveGame
import io.dungeons.port.Id
import org.springframework.stereotype.Component
import java.util.*

data class GameState(val playerId: UUID? = null, val currentGameId: Id<SaveGame>? = null)

@Component
class GameStateHolder {
    var gameState: GameState = GameState()

    fun unpackIdsOrThrow(): Pair<UUID, Id<SaveGame>> {
        val playerId = gameState.playerId
            ?: throw IllegalStateException("Player ID is not set in game state")
        val gameId = gameState.currentGameId
            ?: throw IllegalStateException("Current Game ID is not set in game state")
        return Pair(playerId, gameId)
    }
}
