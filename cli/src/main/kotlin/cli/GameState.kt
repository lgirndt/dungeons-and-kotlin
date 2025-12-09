package io.dungeons.cli

import io.dungeons.domain.core.Id
import io.dungeons.domain.savegame.SaveGame
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference

data class Player(val id: Id<Player>)

data class GameState(val player: Player? = null, val currentGameId: Id<SaveGame>? = null)

@Component
class GameStateHolder {
    private var _gameState: AtomicReference<GameState> = AtomicReference(GameState())

    var gameState: GameState
        get() = _gameState.get()
        set(value) {
            _gameState.set(value)
        }

    fun unpackIdsOrThrow(): Pair<Id<Player>, Id<SaveGame>> {
        val playerId = gameState.player?.id
            ?: throw IllegalStateException("Player ID is not set in game state")
        val gameId = gameState.currentGameId
            ?: throw IllegalStateException("Current Game ID is not set in game state")
        return Pair(playerId, gameId)
    }
}
