package io.dungeons.cli

import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameId
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

data class GameState(
    val playerId: PlayerId? = null,
    val currentGameId: SaveGameId? = null,
    val authToken: String? = null,
)

@Component
class GameStateHolder(private val objectMapper: JsonMapper) {
    var gameState: GameState = GameState()

    fun unpackIdsOrThrow(): Pair<PlayerId, SaveGameId> {
        val playerId = gameState.playerId
            ?: throw IllegalStateException("Player ID is not set in game state")
        val gameId = gameState.currentGameId
            ?: throw IllegalStateException("Current Game ID is not set in game state")
        return Pair(playerId, gameId)
    }

    /**
     * Save the current game state to a JSON file
     */
    fun saveToFile(filePath: Path) {
        val json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(gameState)
        filePath.writeText(json)
    }

    private fun loadFromFile(filePath: Path): GameState {
        require(filePath.exists()) { "File does not exist: $filePath" }
        val json = filePath.readText()
        return objectMapper.readValue(json)
    }

    /**
     * Sync the current game state from a JSON file
     */
    fun syncFromFile(filePath: Path) {
        gameState = loadFromFile(filePath)
    }
}
