package io.dungeons.cli

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.nio.file.Path
import kotlin.io.path.exists

class GameStateTest {
    private val objectMapper = JsonMapper.builder().addModule(kotlinModule()).build()
    private val gameStateHolder = GameStateHolder(objectMapper)

    @Test
    fun `saveToFile creates a file with game state JSON`(@TempDir tempDir: Path) {
        val testGameState = SOME_GAME_STATE.copy()
        gameStateHolder.gameState = testGameState

        val filePath = tempDir.resolve("gamestate.json")
        gameStateHolder.saveToFile(filePath)

        assert(filePath.exists()) { "File should exist after saving" }
    }

    @Test
    fun `syncFromFile throws exception when file does not exist`(@TempDir tempDir: Path) {
        val nonExistentFile = tempDir.resolve("nonexistent.json")

        assertThrows<IllegalArgumentException> {
            gameStateHolder.syncFromFile(nonExistentFile)
        }
    }

    @Test
    fun `syncFromFile updates game state holder`(@TempDir tempDir: Path) {
        val initialGameState = GameState()
        gameStateHolder.gameState = initialGameState

        val savedGameState = SOME_GAME_STATE.copy()
        val tempHolder = GameStateHolder(objectMapper)
        tempHolder.gameState = savedGameState

        val filePath = tempDir.resolve("gamestate.json")
        tempHolder.saveToFile(filePath)

        gameStateHolder.syncFromFile(filePath)

        assertEquals(savedGameState.playerId, gameStateHolder.gameState.playerId)
        assertEquals(savedGameState.currentGameId, gameStateHolder.gameState.currentGameId)
        assertEquals(savedGameState.authToken, gameStateHolder.gameState.authToken)
    }

    @Test
    fun `saveToFile and syncFromFile handle null values correctly`(@TempDir tempDir: Path) {
        val gameStateWithNulls = GameState(
            playerId = null,
            currentGameId = null,
            authToken = null,
        )
        val tempHolder = GameStateHolder(objectMapper)
        tempHolder.gameState = gameStateWithNulls

        val filePath = tempDir.resolve("gamestate_nulls.json")
        tempHolder.saveToFile(filePath)

        gameStateHolder.syncFromFile(filePath)

        assertEquals(null, gameStateHolder.gameState.playerId)
        assertEquals(null, gameStateHolder.gameState.currentGameId)
        assertEquals(null, gameStateHolder.gameState.authToken)
    }

    @Test
    fun `saveToFile creates pretty printed JSON`(@TempDir tempDir: Path) {
        val testGameState = SOME_GAME_STATE.copy()
        gameStateHolder.gameState = testGameState

        val filePath = tempDir.resolve("gamestate.json")
        gameStateHolder.saveToFile(filePath)

        val fileContent = filePath.toFile().readText()

        // Verify it's pretty printed (contains newlines and indentation)
        assert(fileContent.contains("\n")) { "JSON should be pretty printed with newlines" }
        assert(fileContent.contains("  ")) { "JSON should be pretty printed with indentation" }
    }
}
