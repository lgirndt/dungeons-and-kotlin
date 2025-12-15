package io.dungeons.cli

import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameId
import org.springframework.stereotype.Component

data class GameState(val playerId: PlayerId? = null, val currentGameId: SaveGameId? = null)

@Component
class GameStateHolder {
    var gameState: GameState = GameState()

    fun unpackIdsOrThrow(): Pair<PlayerId, SaveGameId> {
        val playerId = gameState.playerId
            ?: throw IllegalStateException("Player ID is not set in game state")
        val gameId = gameState.currentGameId
            ?: throw IllegalStateException("Current Game ID is not set in game state")
        return Pair(playerId, gameId)
    }
}
