package io.dungeons.integration

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.adventure.SOME_ADVENTURE
import io.dungeons.domain.player.Player
import io.dungeons.domain.player.PlayerRepository
import io.dungeons.port.Id
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
}
