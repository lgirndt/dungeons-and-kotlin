package io.dungeons.integration

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.adventure.SOME_ADVENTURE
import io.dungeons.domain.player.Player
import io.dungeons.domain.player.PlayerRepository
import io.dungeons.domain.savegame.SOME_SAVE_GAME
import io.dungeons.domain.savegame.SaveGame
import io.dungeons.domain.savegame.SaveGameRepository
import io.dungeons.port.AdventureId
import io.dungeons.port.Id
import io.dungeons.port.PlayerId
import io.dungeons.port.RoomId
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/**
 * Test data builder for creating and persisting test fixtures.
 * 
 * Provides fluent API for creating domain entities with sensible defaults
 * and optional customization. Automatically persists entities to repositories.
 */
@Component
class TestDataBuilder(
    private val adventureRepository: AdventureRepository,
    private val playerRepository: PlayerRepository,
    private val saveGameRepository: SaveGameRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    /**
     * Creates and persists an adventure with optional customization.
     * 
     * @param name The adventure name (default: "Test Adventure")
     * @param customizer Optional lambda to customize the adventure before persisting
     * @return The persisted adventure with generated ID
     */
    fun adventure(
        name: String = "Test Adventure",
        customizer: Adventure.() -> Adventure = { this },
    ): Adventure {
        val adventure = SOME_ADVENTURE.copy(
            id = Id.generate(),
            name = name,
        ).customizer()

        return adventureRepository.save(adventure)
            ?: error("Failed to save adventure")
    }

    /**
     * Creates and persists a player with optional customization.
     * 
     * @param name The player name (default: "testplayer")
     * @param password The raw password (will be hashed, default: "testpassword")
     * @param customizer Optional lambda to customize the player before persisting
     * @return The persisted player with generated ID
     */
    fun player(
        name: String = "testplayer",
        password: String = "testpassword",
        customizer: Player.() -> Player = { this },
    ): Player {
        val hashedPassword = passwordEncoder.encode(password)
            ?: error("Failed to encode password")

        val player = Player(
            id = Id.generate(),
            name = name,
            hashedPassword = hashedPassword,
        ).customizer()

        return playerRepository.insert(player)
    }

    /**
     * Creates and persists a save game with optional customization.
     * 
     * @param playerId The player ID
     * @param adventureId The adventure ID
     * @param currentRoomId The current room ID (defaults to adventure's initial room if not specified)
     * @param customizer Optional lambda to customize the save game before persisting
     * @return The persisted save game with generated ID
     */
    fun saveGame(
        playerId: PlayerId,
        adventureId: AdventureId,
        currentRoomId: RoomId? = null,
        customizer: SaveGame.() -> SaveGame = { this },
    ): SaveGame {
        val roomId = currentRoomId ?: run {
            // If no room ID provided, use the adventure's initial room
            val adventure = adventureRepository.findById(adventureId)
                .orElseThrow { IllegalStateException("Adventure not found: $adventureId") }
            adventure.initialRoomId
        }

        val saveGame = SOME_SAVE_GAME.copy(
            id = Id.generate(),
            playerId = playerId,
            adventureId = adventureId,
            currentRoomId = roomId,
        ).customizer()

        saveGameRepository.save(saveGame)
        return saveGame
    }
}
