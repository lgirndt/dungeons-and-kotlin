package io.dungeons.persistence.mongodb

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DataMongoTest
class MongoDBSaveGameRepositoryTest {

    @Autowired
    private lateinit var repository: MongoDBSaveGameRepository

    @Test
    fun `should save and retrieve a save game`() {
        // Given
        val saveGame = SOME_SAVE_GAME.copy()

        // When
        repository.save(saveGame)

        // Then
        val found = repository.findById(saveGame.id)
        assertTrue(found.isPresent)
        assertEquals(saveGame.id, found.get().id)
        assertEquals(saveGame.userId, found.get().userId)
    }

    @Test
    fun `should find save game by userId and saveGameId`() {
        // Given
        val saveGame = SOME_SAVE_GAME.copy()
        repository.save(saveGame)

        // When
        val found = repository.findByUserId(saveGame.userId, saveGame.id)

        // Then
        assertTrue(found.isPresent)
        assertEquals(saveGame.id, found.get().id)
        assertEquals(saveGame.userId, found.get().userId)
    }

    @Test
    fun `should not find save game with wrong userId`() {
        // Given
        val saveGame = SOME_SAVE_GAME.copy()
        repository.save(saveGame)

        val differentPlayerId = io.dungeons.domain.core.Id.generate<io.dungeons.domain.core.Player>()

        // When
        val found = repository.findByUserId(differentPlayerId, saveGame.id)

        // Then
        assertTrue(found.isEmpty)
    }
}
