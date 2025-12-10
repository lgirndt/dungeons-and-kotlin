package io.dungeons.cli

import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import io.dungeons.domain.savegame.SaveGame
import org.springframework.stereotype.Component

data class GameState(val player: Player? = null, val currentGameId: Id<SaveGame>? = null)

@Component
class GameStateHolder {
    var gameState: GameState = GameState()

    fun unpackIdsOrThrow(): Pair<Id<Player>, Id<SaveGame>> {
        val playerId = gameState.player?.id
            ?: throw IllegalStateException("Player ID is not set in game state")
        val gameId = gameState.currentGameId
            ?: throw IllegalStateException("Current Game ID is not set in game state")
        return Pair(playerId, gameId)
    }
}
